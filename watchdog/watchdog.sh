#!/bin/bash
# Watches both DB nodes. Every loop it asks each node for its REAL role,
# so it never trusts remembered state and is safe to restart at any time.
# It is the only thing that promotes a node.
set -u

NODES=(172.28.1.14 172.28.1.15)
LIMIT=3        # "no primary" checks in a row before promoting
INTERVAL=5     # seconds between checks
NO_PRIMARY=0
LAST_STATE=""

export PGPASSWORD="$(cat /run/secrets/db_password)"
export PGCONNECT_TIMEOUT=3   # give up on a dead node after 3s instead of hanging

# Prints "role|timeline|lsn", e.g. "t|2|0/7000060". Prints nothing if unreachable.
#   role:     f = primary, t = standby
#   timeline: goes up by 1 on every promotion (higher = more recent history)
#   lsn:      position in the change log (higher = more changes applied)
node_info() {
  PGOPTIONS="-c statement_timeout=5s" psql -h "$1" -U app -d appdb -tAc "
    select pg_is_in_recovery(),
           timeline_id,
           case when pg_is_in_recovery() then pg_last_wal_replay_lsn()
                else pg_current_wal_lsn() end
    from pg_control_checkpoint();" 2>/dev/null
}

# "1/A0" -> one number, so two LSNs can be compared with -gt.
lsn_to_num() {
  [ -n "$1" ] || { echo 0; return; }
  local hi="${1%/*}" lo="${1#*/}"
  echo $(( (16#$hi << 32) + 16#$lo ))
}

while true; do
  PRIMARIES=()
  BEST="" BEST_TL=-1 BEST_LSN=-1   # best standby to promote, if needed
  STATE=""

  for node in "${NODES[@]}"; do
    info="$(node_info "$node")"
    if [ -z "$info" ]; then
      STATE+="$node=down "
      continue
    fi
    IFS='|' read -r role tl lsn <<< "$info"

    if [ "$role" = "f" ]; then
      PRIMARIES+=("$node")
      STATE+="$node=primary(tl$tl) "
    else
      STATE+="$node=standby(tl$tl) "
      num="$(lsn_to_num "$lsn")"
      # Newest data wins: higher timeline first, then higher LSN.
      if [ "$tl" -gt "$BEST_TL" ] || { [ "$tl" -eq "$BEST_TL" ] && [ "$num" -gt "$BEST_LSN" ]; }; then
        BEST="$node" BEST_TL="$tl" BEST_LSN="$num"
      fi
    fi
  done

  # Only log when something changed, so the log stays readable.
  if [ "$STATE" != "$LAST_STATE" ]; then
    echo "state: $STATE"
    LAST_STATE="$STATE"
  fi

  if [ "${#PRIMARIES[@]}" -gt 1 ]; then
    echo "WARNING: more than one primary (${PRIMARIES[*]}). Not touching anything; needs a human."
    NO_PRIMARY=0
  elif [ "${#PRIMARIES[@]}" -eq 1 ]; then
    NO_PRIMARY=0
  else
    NO_PRIMARY=$((NO_PRIMARY + 1))
    echo "no primary ($NO_PRIMARY/$LIMIT)"

    if [ "$NO_PRIMARY" -ge "$LIMIT" ]; then
      if [ -z "$BEST" ]; then
        echo "no node reachable; nothing to promote"
      else
        echo "promoting $BEST (timeline $BEST_TL, newest data among reachable nodes)"
        # pg_promote(wait, seconds): returns t only once the node really is primary.
        RESULT="$(psql -h "$BEST" -U app -d appdb -tAc "select pg_promote(true, 30);" 2>&1)"
        if [ "$RESULT" = "t" ]; then
          echo "promoted $BEST"
          NO_PRIMARY=0
        else
          echo "promotion of $BEST FAILED ($RESULT); will retry"
        fi
      fi
    fi
  fi

  sleep "$INTERVAL"
done
