set -u                       

FAILS=0
LIMIT=3

export PGPASSWORD="$(cat /run/secrets/db_password)"

while true; do
  if pg_isready -h 172.28.1.14 -p 5432 -U app -d appdb > /dev/null; then
    FAILS=0
  else
    FAILS=$((FAILS + 1))
    echo "check failed ($FAILS/$LIMIT)"
  fi

  if [ "$FAILS" -ge "$LIMIT" ]; then
    echo "server is down, switching to backup"
    psql -h 172.28.1.15 -U app -d appdb -c "select pg_promote();"
    break                    
  fi

  sleep 5
done

sleep infinity               # keep the container alive after taking over
