#!/bin/bash
set -e
shopt -s dotglob   # make * also match hidden files when moving the copy into place

DATA=/var/lib/postgresql/data
COPY="$DATA/.copy.tmp"          # fresh copy lands here first (same disk, so mv is a fast rename)
WAS_PRIMARY="$DATA/was_primary" # marker: "I was primary, then booted read-only"
# wal_keep_size: primary keeps extra WAL so a standby that was down for a bit can catch up without a full copy
PG_ARGS=(-c wal_level=replica -c max_wal_senders=10 -c hot_standby=on -c wal_keep_size=256MB)

export PGPASSWORD="$(cat /run/secrets/db_password)"

# Leftover from a copy that was interrupted. The old data next to it is still intact.
rm -rf "$COPY"

# Ask the partner for its role AND its timeline in one query.
# Output looks like "f|2" (f = primary, t = standby). Empty = couldn't reach it.
# "|| true" stops set -e from killing the script when the partner is down.
PEER_INFO="$(psql -h "$PEER" -U app -d appdb -tAc \
  "select pg_is_in_recovery(), timeline_id from pg_control_checkpoint();" || true)"
PEER_ROLE="${PEER_INFO%%|*}"   # text before the |
PEER_TL="${PEER_INFO##*|}"     # text after the |

# My own timeline, read from the data folder (works while Postgres is stopped).
my_timeline() {
  pg_controldata "$DATA" | awk -F: '/Latest checkpoint.s TimeLineID/ { gsub(/ /, "", $2); print $2 }'
}

start_postgres() {
  exec docker-entrypoint.sh postgres "${PG_ARGS[@]}"
}

# Boot read-only and follow the partner. Never promotes itself:
# the watchdog is the only one that decides who becomes primary.
start_as_standby() {
  echo "Starting as standby (read-only). The watchdog decides who becomes primary." >&2
  if [ ! -f "$DATA/standby.signal" ]; then
    touch "$WAS_PRIMARY"   # remember: this data may have writes the partner never got
  fi
  touch "$DATA/standby.signal"
  sed -i '/^primary_conninfo/d' "$DATA/postgresql.auto.conf"
  echo "primary_conninfo = 'host=$PEER port=5432 user=replicator password=$(cat /run/secrets/repl_password) connect_timeout=3'" \
    >> "$DATA/postgresql.auto.conf"
  start_postgres
}

# Full copy from the partner. Old data is only deleted AFTER the copy succeeds.
copy_from_peer() {
  echo "Copying everything from $PEER." >&2
  PGPASSWORD="$(cat /run/secrets/repl_password)" \
    pg_basebackup -h "$PEER" -U replicator -D "$COPY" -R -X stream -P
  rm -f "$COPY/was_primary"   # the partner's marker (if any) is not ours

  # Copy succeeded: swap. Delete old data (everything except the copy), then move the copy up.
  find "$DATA" -mindepth 1 -maxdepth 1 ! -name "$(basename "$COPY")" -exec rm -rf {} +
  mv "$COPY"/* "$DATA"/
  rmdir "$COPY"
  chmod 0700 "$DATA"
}

# ---------------------------------------------------------------------------
# 1) Partner is primary.
if [ "$PEER_ROLE" = "f" ]; then
  MY_TL="$( [ -s "$DATA/PG_VERSION" ] && my_timeline || echo 0 )"
  echo "Partner $PEER is primary (timeline $PEER_TL). My timeline: $MY_TL." >&2

  # A plain standby that is not ahead of the partner can just reconnect and catch up.
  if [ -f "$DATA/standby.signal" ] && [ ! -f "$WAS_PRIMARY" ] && [ "$MY_TL" -le "$PEER_TL" ]; then
    echo "I'm a standby and not ahead of the partner. Reconnecting without a copy." >&2
    start_postgres
  fi

  # Empty, a former primary, or ahead of the partner: my data can't simply follow it.
  copy_from_peer
  start_postgres
fi

# 2) Empty volume: only the bootstrap node may start fresh,
#    and only when no partner is answering (so it can't hide a partner's data).
if [ ! -s "$DATA/PG_VERSION" ]; then
  if [ "$ALLOW_INIT" = "true" ] && [ -z "$PEER_INFO" ]; then
    echo "Empty volume and no partner. Starting a fresh database." >&2
    start_postgres
  fi
  echo "No data here, and this node may not start fresh. Exiting so Docker retries." >&2
  exit 1
fi

# 3) I have data. Partner is a standby or unreachable.
MY_TL="$(my_timeline)"
echo "My timeline: $MY_TL. Partner: role='${PEER_ROLE:-unreachable}' timeline='${PEER_TL:-?}'." >&2

# Only stay primary if I WAS primary, the partner is a standby,
# and the partner is not on a newer timeline than me.
if [ "$PEER_ROLE" = "t" ] && [ ! -f "$DATA/standby.signal" ] && [ "$MY_TL" -ge "$PEER_TL" ]; then
  echo "I was primary and I'm not behind the partner. Starting as primary." >&2
  rm -f "$WAS_PRIMARY"
  start_postgres
fi

# Anything else means I can't be sure I have the newest data.
start_as_standby
