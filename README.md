# fault-tolerant-ems

> Also see: github.com/zrygan/media-upload-service/

Fault tolerant enrollment management system.

Docker guide:
1. Make sure you have the updated .env file
2. From the repo root:
```sh
docker compose build
```
3. To set up the databases:
```sh
docker compose up -d db-primary db-replica db-watchdog
```
4. Verify the database:
```sh
docker compose exec db-primary psql -U app -d appdb -c "\dt"
```
5. Start the app nodes:
```sh
docker compose up -d
```
6. You can verify it with
```sh
docker compose ps
```

Once docker is running, go to `https://localhost:3443` to access the view