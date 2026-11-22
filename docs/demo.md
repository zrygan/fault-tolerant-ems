# faul-tolerant-ems / demo

## Nodes and Networks are Isolated

```bash
# Run

# only view has host port
docker compose ps

# docker network inspects
docker network inspect fault-tolerant-ems_frontend --format '{{range .Containers}}{{.Name}}: {{.IPv4Address}}{{"\n"}}{{end}}'
docker network inspect fault-tolerant-ems_backend --format '{{range .Containers}}{{.Name}}: {{.IPv4Address}}{{"\n"}}{{end}}'
```

## Demo Walkthrough

### Login Credentials

|**Type**|**email**|
|---|---|
|student|`apolinario_mabini@university.edu`|
|faculty|`ramon.santos@university.edu`|
|admin|`max.admin@university.edu`|

### Demonstrations

1. Enrollment
2. Grading
3. Clearances
4. Failure on **auth node**
    1. Run: `docker compose stop auth`
    2. On already logged-in student: should still work.
    3. On a new browser logging in as student: should fail.
    4. Restore: `docker compose start auth`
5. Failure on **grades node
    1. Run: `docker compose stop grades`
    2. Grades should have partial functionality
    3. Should be able to still enroll.
    4. Restore: `docker compose start grades`
6. Database Fault Tolerance:
    1. Watch **DATABASE WATCHDOG terminal**.
    2. On **CENTRAL terminal**: `docker compose stop db-primary`
    3. Wait 15-20 seconds, promotion should occur.
    4. Enrollment should work even if `db-primary` is stopped.
    5. Watch  **DATABASE WATCHDOG terminal** and **DATABASE LOGS terminal**.
    6. On **CENTRAL terminal**: `docker compose start db-primary`
    7. Run: `docker compose exec db-primary psql -U app -d appdb -c "SELECT pg_is_in_recovery();"`
