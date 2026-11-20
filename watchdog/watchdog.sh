set -u                       

PRIMARY=172.28.1.14
REPLICA=172.28.1.15
FAILS=0
LIMIT=3

export PGPASSWORD="$(cat /run/secrets/db_password)"

while true; do
  if pg_isready -h $PRIMARY -p 5432 -U app -d appdb > /dev/null; then
    FAILS=0
  else
    FAILS=$((FAILS + 1))
    echo "check failed ($FAILS/$LIMIT)"
  fi

  if [ "$FAILS" -ge "$LIMIT" ] && pg_isready -h $REPLICA -p 5432 -U app -d appdb > /dev/null; then
    echo "Primary @ $PRIMARY is down, promoting replica @ $REPLICA"
    psql -h $REPLICA -U app -d appdb -c "select pg_promote();"
    
    TMP=$PRIMARY
    PRIMARY=$REPLICA
    REPLICA=$TMP

    FAILS=0
    echo "Succesfully promoted $PRIMARY to primary"
  fi

  sleep 5
done

