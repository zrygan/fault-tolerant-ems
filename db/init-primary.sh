set -e   # stop at the first error instead of carrying on

REPL_PASSWORD="$(cat /run/secrets/repl_password)"


psql -v ON_ERROR_STOP=1 -v repl_pass="$REPL_PASSWORD" \
  -U "$POSTGRES_USER" -d "$POSTGRES_DB" <<-'EOSQL'
  CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD :'repl_pass';
EOSQL

echo "host replication replicator all scram-sha-256" >> "$PGDATA/pg_hba.conf"