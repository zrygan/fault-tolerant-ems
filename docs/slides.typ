// Fault-Tolerant Enrollment System: STDISCM slides
// Layout follows the MO2 deck: white 16:9, light title top-left,
// dark code panel on the left, explanation on the right, page number bottom-right.
//
// Build (Typst 0.12 or newer), from the repo root:
//   typst compile docs/slides.typ docs/slides.pdf

#let deck-title = "Fault-Tolerant Enrollment System"
#let authors = ("Enrique Aragon", "Stephen Borja", "Erin Chua", "Zhean Ganituen")

#let title-font = "Calibri"
#let body-font = "Calibri"
#let code-font = ("Consolas", "Cascadia Mono", "DejaVu Sans Mono")
#let code-bg = rgb("#1e1e1e")
#let code-fg = rgb("#d4d4d4")

#set document(title: deck-title, author: authors)
#set page(
  width: 13.333in,
  height: 7.5in,
  margin: (top: 0.3in, bottom: 0.75in, left: 0.45in, right: 0.45in),
  foreground: context {
    place(bottom + right, dx: -1.7in, dy: -0.3in,
      text(font: body-font, size: 12pt, fill: luma(140), counter(page).display()))
  },
)
#set text(font: body-font, size: 17pt, fill: black, lang: "en")
#set par(leading: 0.5em, spacing: 1.1em, justify: false)
#set list(marker: [-], indent: 0pt, body-indent: 0.7em, spacing: 0.6em)
#set raw(theme: "assets/dark.tmTheme")

// ---------- building blocks ----------

#let code-panel(code, size: 10.5pt) = block(
  fill: code-bg,
  inset: (x: 16pt, y: 14pt),
  width: 100%,
  breakable: false,
  {
    show raw: set text(font: code-font, size: size, fill: code-fg)
    set par(leading: 0.55em, spacing: 0.9em)
    code
  },
)

#let slide-title(title) = text(font: title-font, weight: "light", size: 44pt, title)

// Code on the left, explanation on the right, both centered vertically.
#let slide(title, code, body, split: (52%, 1fr), code-size: 10.5pt) = {
  pagebreak(weak: true)
  grid(
    rows: (auto, 1fr),
    row-gutter: 0.2in,
    align: (x, y) => if y == 0 { left + top } else { left + horizon },
    slide-title(title),
    grid(
      columns: split,
      column-gutter: 0.5in,
      align: left + horizon,
      code-panel(code, size: code-size),
      body,
    ),
  )
}

// Full-width content under the title.
#let wide-slide(title, body) = {
  pagebreak(weak: true)
  grid(
    rows: (auto, 1fr),
    row-gutter: 0.25in,
    align: (x, y) => if y == 0 { left + top } else { left + horizon },
    slide-title(title),
    body,
  )
}

// ---------- Title ----------

#page[
  #place(top + center, dy: 1.3in,
    text(font: title-font, weight: "light", size: 54pt, deck-title))
  #place(top + center, dy: 5.75in,
    text(size: 22pt, authors.join(", ")))
]

// ---------- Node separation: view node ----------

#slide(
  [Node Separation: View Node],
  [
    ```yaml
    # docker-compose.yaml
    view:
      build:
        context: .
        dockerfile: view/Dockerfile
      environment:
        AUTH_ADDRESS: "172.28.0.11:9090"
        GRADES_ADDRESS: "172.28.0.12:9090"
        COURSES_ADDRESS: "172.28.0.13:9090"
      networks:
        frontend:
          ipv4_address: 172.28.0.10
      ports:
        - "3000:8080"
      # no secrets, no backend network
    ```
    ```properties
    # view/src/main/resources/application.properties
    spring.grpc.client.channels.auth.address=${AUTH_ADDRESS}
    spring.grpc.client.channels.courses.address=${COURSES_ADDRESS}
    spring.grpc.client.channels.grades.address=${GRADES_ADDRESS}
    ```
  ],
  code-size: 10pt,
  [
    The view is the MVC front end. Controllers handle each click, Thymeleaf templates draw the pages, and every piece of data comes from another node over gRPC.

    - _only public port_: port 3000 on the host maps to 8080 in the container; no other node is published
    - _frontend network only_: it has no address on the backend network, so it cannot open a database connection at all
    - _no secrets_: it gets neither `db_password` nor `jwt_secret`; it stores tokens but never signs or checks them
    - _address variables_: each feature node is found through an environment variable, so a node can move to another machine with only a config change

    If the view goes down, the website is gone, but every feature node keeps running behind it.
  ],
)

// ---------- Node separation: feature nodes ----------

#slide(
  [Node Separation: Feature Nodes],
  ```yaml
  # docker-compose.yaml (condensed)
  auth:
    build: { context: ., dockerfile: auth/Dockerfile }
    secrets: [db_password, jwt_secret]
    networks:
      frontend: { ipv4_address: 172.28.0.11 }
      backend:  { ipv4_address: 172.28.1.11 }

  grades:
    build: { context: ., dockerfile: grades/Dockerfile }
    secrets: [db_password, jwt_secret]
    networks:
      frontend: { ipv4_address: 172.28.0.12 }
      backend:  { ipv4_address: 172.28.1.12 }

  courses:
    build: { context: ., dockerfile: courses/Dockerfile }
    secrets: [db_password, jwt_secret]
    environment:
      SPRING_CONFIG_IMPORT: configtree:/run/secrets/
      SPRING_GRPC_SERVER_PORT: "9090"
      SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "10"
    networks:
      frontend: { ipv4_address: 172.28.0.13 }
      backend:  { ipv4_address: 172.28.1.13 }
    restart: on-failure
  ```,
  code-size: 10pt,
  [
    Auth, courses and grades are three separate Spring Boot apps. Each has its own Gradle project, Dockerfile and container, and they share nothing except the `.proto` files.

    - _two networks each_: a frontend address to answer the view, and a backend address to reach the database
    - _own secrets_: each one receives `db_password` and `jwt_secret` as Docker secrets, read through Spring's configtree
    - _own connection pool_: up to 10 database connections per node, so one busy node cannot use up another's
    - _own token check_: each has its own copy of `JwtService`, so none of them needs auth to be up

    Stopping one container removes exactly one feature area.
  ],
)

// ---------- Node separation: networks ----------

#slide(
  [Node Separation: Networks],
  ```yaml
  # docker-compose.yaml
  networks:
    frontend:
      ipam:
        config:
          - subnet: 172.28.0.0/24
    backend:
      ipam:
        config:
          - subnet: 172.28.1.0/24

  db-primary:
    networks:
      backend: { ipv4_address: 172.28.1.14 }
  db-replica:
    networks:
      backend: { ipv4_address: 172.28.1.15 }
  db-watchdog:
    networks:
      backend: { ipv4_address: 172.28.1.16 }
  ```,
  code-size: 10pt,
  split: (42%, 1fr),
  [
    Docker only routes traffic between containers that share a network. The two networks overlap only at the three feature nodes.

    #v(0.05in)
    #{
      set text(size: 14pt)
      show table.cell.where(y: 0): set text(fill: white, weight: "bold")
      table(
        columns: (1fr, 1fr, 1fr),
        inset: (x: 10pt, y: 6pt),
        align: left + horizon,
        stroke: (bottom: 0.6pt + luma(200)),
        fill: (x, y) => if y == 0 { code-bg } else { none },
        table.header([Node], [frontend], [backend]),
        [view], [172.28.0.10], [none],
        [auth], [172.28.0.11], [172.28.1.11],
        [grades], [172.28.0.12], [172.28.1.12],
        [courses], [172.28.0.13], [172.28.1.13],
        [db-primary], [none], [172.28.1.14],
        [db-replica], [none], [172.28.1.15],
        [db-watchdog], [none], [172.28.1.16],
      )
    }
    #v(0.05in)
    Fixed addresses mean the view's settings and the database URL never change between runs.
  ],
)

// ---------- Node separation: data ownership ----------

#slide(
  [Node Separation: Data Ownership],
  ```sql
  -- db/01_schema.sql
  -- auth service owns: users
  CREATE TABLE users (
      email         TEXT NOT NULL UNIQUE,
      password_hash TEXT NOT NULL,   -- bcrypt hash
      role          TEXT NOT NULL
                    CHECK (role IN ('student', 'faculty')),
      ...
  );

  -- course service owns: courses, sections, enrollments
  CREATE TABLE sections (
      course_id      BIGINT   NOT NULL REFERENCES courses (id),
      faculty_id     BIGINT   REFERENCES users (id),
      capacity       SMALLINT NOT NULL CHECK (capacity > 0),
      enrolled_count SMALLINT NOT NULL DEFAULT 0,
      CHECK (enrolled_count BETWEEN 0 AND capacity),
      ...
  );

  -- grades service owns: grades
  CREATE TABLE grades (
      enrollment_id BIGINT PRIMARY KEY REFERENCES enrollments (id),
      grade         NUMERIC(2,1) NOT NULL
          CHECK (grade IN (0.0, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0)),
      uploaded_by   BIGINT NOT NULL REFERENCES users (id),
      ...
  );
  ```,
  code-size: 10pt,
  [
    All feature nodes share one PostgreSQL database, but each table has exactly one node that writes to it.

    - _auth_: writes `users`, and is the only node that checks passwords
    - _courses_: writes `courses`, `sections` and `enrollments`; reads `users` for faculty names
    - _grades_: writes `grades`; reads enrollments, sections and courses to label each grade
    - _foreign keys and CHECKs_: the database enforces the rules itself, whichever node writes

    One shared database keeps joins simple, but every feature node depends on it. That is why it is replicated.
  ],
)

// ---------- gRPC contracts: auth ----------

#slide(
  [gRPC Contracts: Auth],
  ```proto
  // proto/auth.proto
  service AuthService {
    rpc Login (LoginRequest) returns (LoginResponse);
    rpc Logout (LogoutRequest) returns (LogoutResponse);
    rpc GetUser (GetCurrentUserRequest) returns (UserInfo);
  }

  enum Role {
    ROLE_NONE = 0;
    ROLE_STUDENT = 1;
    ROLE_FACULTY = 2;
  }

  message LoginRequest {
    string email = 1;
    string password = 2;
  }

  message LoginResponse {
    string token = 1;
    UserInfo user = 2;
  }

  message UserInfo {
    int64 id = 1;
    string email = 2;
    string full_name = 3;
    Role role = 4;
  }
  ```,
  code-size: 10pt,
  [
    The auth node owns the `users` table and is the only node that ever sees a password. Its contract has three calls:

    - _Login()_: takes an email and password, returns a signed JWT plus the user's id, name and role; a wrong password returns `UNAUTHENTICATED`
    - _Logout()_: an empty call; the view clears its own session, so logging out does not depend on auth being up
    - _GetUser()_: returns the caller's profile, read from the token in the request metadata
    - _Role_: the view uses it right after login to send students and faculty to different dashboards

    No request in any contract carries a user id. Identity always comes from the token.
  ],
)

// ---------- gRPC contracts: courses ----------

#slide(
  [gRPC Contracts: Courses],
  ```proto
  // proto/courses.proto
  service CourseService {
    rpc ListSection (ListSectionRequest) returns (ListSectionResponse);
    rpc EnrollCourse (EnrollCourseRequest)
        returns (EnrollCourseResponse);
    rpc ListEnrollments (ListEnrollmentRequest)
        returns (ListEnrollmentResponse);
    rpc DropCourse (DropCourseRequest) returns (DropCourseResponse);
  }

  message ListSectionRequest { string term = 1; bool mine_only = 2; }
  message EnrollCourseRequest { int64 section_id = 1; }
  message DropCourseRequest { int64 section_id = 1; }

  // every response carries one or more SectionInfo
  message SectionInfo {
    int64 id = 1;
    string course_code = 2;
    string course_title = 3;
    int32 units = 4;
    string section_code = 5;
    string faculty_name = 6;
    int32 capacity = 7;
    int32 enrolled_count = 8;
    bool is_open = 9;
    string term = 10;
  }
  ```,
  code-size: 10pt,
  [
    The courses node owns `courses`, `sections` and `enrollments`. Every reply describes a section with the same `SectionInfo` message, so every page shows sections the same way.

    - _ListSection()_: lists sections for a term; with `mine_only`, only the caller's own sections, which feeds the faculty grade page
    - _EnrollCourse()_: adds the caller to a section and returns the new seat count
    - _ListEnrollments()_: the sections the caller is enrolled in
    - _DropCourse()_: removes the caller from a section and returns the updated section
    - _is\_open_: worked out as `enrolled_count < capacity` on every reply, so it is never out of date

    Requests only name a section. Who is enrolling comes from the token.
  ],
)

// ---------- gRPC contracts: grades ----------

#slide(
  [gRPC Contracts: Grades],
  ```proto
  // proto/grades.proto
  service GradesService {
    rpc GetGrades (GetGradesRequest) returns (GetGradesResponse);
    rpc UploadGrades (UploadGradesRequest)
        returns (UploadGradesResponse);
    rpc ListGradableStudents (ListGradableStudentsRequest)
        returns (ListGradableStudentsResponse);
  }

  message UploadGradesRequest {
    int64 enrollment_id = 1;
    double grade = 2;
  }

  message GradeRecord {
    int64 enrollment_id = 1;
    string course_code = 2;
    string course_title = 3;
    string section_code = 4;
    optional double grade = 5;
    string term = 6;
  }

  message GradableStudent {
    int64 enrollment_id = 1;
    string student_name = 2;
    optional double current_grade = 3;
  }
  ```,
  code-size: 10pt,
  [
    The grades node owns the `grades` table. A grade is attached to an enrollment, so one id links a student, a section and a grade.

    - _GetGrades()_: every course the caller has taken, with the grade where one exists
    - _ListGradableStudents()_: the students in one section the caller teaches, with any grade already given
    - _UploadGrades()_: faculty only; accepts 0.0, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5 or 4.0, then inserts or updates the grade
    - _optional double_: tells "no grade yet" apart from a real 0.0

    The course details in `GradeRecord` are copied into the reply, so the grades page never needs to call the courses node.
  ],
)

// ---------- gRPC wiring ----------

#slide(
  [gRPC Wiring],
  [
    ```groovy
    // auth, courses, grades, view: build.gradle
    protobuf {
        plugins { grpc { } }
    }
    sourceSets {
        main { proto { srcDir('../proto') } }
    }
    ```
    ```java
    // server side: one implementation per node
    @Service
    public class CoursesGrpcService
            extends CourseServiceGrpc.CourseServiceImplBase {
        @Override
        public void enrollCourse(EnrollCourseRequest req,
                StreamObserver<EnrollCourseResponse> out) { ... }
    }

    // client side: view/GrpcClientConfig.java
    @Bean
    CourseServiceGrpc.CourseServiceBlockingStub coursesStub(
            GrpcChannelFactory channels,
            @Value("${COURSES_ADDRESS}") String address) {
        return CourseServiceGrpc.newBlockingStub(
                channels.createChannel(address));
    }
    ```
  ],
  code-size: 10pt,
  [
    All three contracts live in one shared `proto/` folder. Every node compiles them at build time, so client and server always agree on the message format.

    - _protobuf plugin_: generates the Java message classes and gRPC stubs from `../proto` during the Gradle build
    - _ImplBase_: each service extends the generated base class; Spring gRPC serves it on port 9090
    - _GrpcClientConfig_: the view builds one blocking stub per node from its address; controllers depend on stubs, never on another node's code

    A failed call arrives as a `StatusRuntimeException` with a code such as `UNAVAILABLE`, which the view can catch and handle.
  ],
)

// ---------- Sessions across nodes ----------

#slide(
  [Sessions Across Nodes],
  [
    ```java
    // auth: JwtService.create()
    var claims = new JWTClaimsSet.Builder()
            .subject(String.valueOf(u.getId()))
            .claim("role", u.getRole())
            .expirationTime(new Date(now.getTime() + 60 * 60 * 1000))
            .build();
    var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    jwt.sign(new MACSigner(secret));

    // view: GrpcAuth.withToken()
    md.put(AUTH, "Bearer " + session.getAttribute("token"));
    return stub.withInterceptors(
            MetadataUtils.newAttachHeadersInterceptor(md));

    // courses + grades: JwtServerInterceptor
    var caller = jwt.verify(header.substring(7));
    if (caller != null) {
        Context ctx = Context.current()
                .withValue(USER_ID, caller.id())
                .withValue(ROLE, caller.role());
        return Contexts.interceptCall(ctx, call, headers, next);
    }
    call.close(Status.UNAUTHENTICATED, new Metadata());
    ```
  ],
  code-size: 10pt,
  [
    Login happens once, on the auth node. After that, every node checks the session by itself. Sessions are tracked through:

    - _AuthGrpcService.login()_: checks the bcrypt hash, then signs an HS256 JWT holding the user id and role, valid for one hour
    - _GrpcAuth.withToken()_: the view keeps the token in its `HttpSession` and attaches it as `authorization: Bearer` metadata on every call
    - _JwtServerInterceptor_: runs before every RPC on courses and grades, verifies the signature and expiry with the shared `jwt_secret`, and puts the caller's id and role in the gRPC context

    No node calls auth to validate a token, so signed-in users keep working while auth is down.
  ],
)

// ---------- Enrollment ----------

#slide(
  [Enrollment Transaction],
  ```java
  @Transactional
  public Enrollment enrollStudent(Long sectionId, Long studentId) {
      if (enrollmentRepo.existsByStudentIdAndSectionId(
              studentId, sectionId))
          throw Status.ALREADY_EXISTS.withDescription(...);

      Section section = sectionRepo.findById(sectionId)
              .orElseThrow(...);
      if (enrollmentRepo.existsByStudentAndCourseCode(
              studentId, section.getCourseCode()))
          throw Status.ALREADY_EXISTS.withDescription(...);

      if (section.getEnrolledCount() >= section.getCapacity())
          throw Status.FAILED_PRECONDITION
                  .withDescription("Section is full")...;

      // ... build the enrollment row ...
      int newCount = section.getEnrolledCount() + 1;
      section.setEnrolledCount(newCount);
      if (newCount >= section.getCapacity())
          section.setIsOpen(false);

      sectionRepo.save(section);
      return enrollmentRepo.save(enrollment);
  }
  ```,
  code-size: 10pt,
  [
    Enrollment must never overfill a section, so it runs as one database transaction on the courses node. The rules live in:

    - _enrollStudent()_: rejects a section the student is already in, or a second section of the same course (`ALREADY_EXISTS`); rejects a full section (`FAILED_PRECONDITION`); then saves the enrollment and raises the count
    - _dropStudent()_: refuses once a grade exists, lowers the count, and reopens a section that was full
    - _CHECK constraint_: the schema itself refuses an `enrolled_count` above `capacity`

    The student id comes from the verified token, never from the form, so a student can only enroll themselves.
  ],
)

// ---------- Graceful degradation ----------

#slide(
  [Graceful Degradation],
  ```java
  // StudentController: depends only on the grades node
  @GetMapping("/grades")
  public String gradesPage(Model model, HttpSession s) {
      try {
          model.addAttribute("grades", grpcAuth.withToken(grades, s)
                  .getGrades(GetGradesRequest.getDefaultInstance())
                  .getGradesList());
      } catch (StatusRuntimeException e) {
          model.addAttribute("error",
                  "Grades are temporarily unavailable.");
      }
      return "pages/student/grades";   // page still renders
  }

  // AuthController: logout works even if auth is unreachable
  @PostMapping("/logout")
  public String logout(HttpSession session) {
      try {
          grpcAuth.withToken(auth, session)
                  .logout(LogoutRequest.getDefaultInstance());
      } catch (StatusRuntimeException ignored) { }
      session.invalidate();
      return "redirect:/login";
  }
  ```,
  code-size: 10pt,
  [
    The view never assumes a node is up. A failed call removes one part of a page instead of crashing it. This is handled through:

    - _try / catch per call_: each gRPC call catches `StatusRuntimeException` and puts a message on the model, so the template still renders instead of a 500 error
    - _one node per student page_: enroll and drop call only courses; the grades page calls only grades
    - _logout()_: clears the local session even when auth cannot be reached
    - _LoginInterceptor_: guards `/student/**` and `/faculty/**` using only the local session, so moving between pages needs no remote call

    When a node is down, only the features on that node stop working.
  ],
)

// ---------- Redundant database ----------

#slide(
  [Redundant Database],
  [
    ```yaml
    # docker-compose.yaml (condensed)
    db-primary:
      image: postgres:17
      user: postgres
      # first boot: docker-entrypoint.sh postgres
      #   -c wal_level=replica -c max_wal_senders=10 -c hot_standby=on
      volumes: [primary-data:/var/lib/postgresql/data]
      networks: { backend: { ipv4_address: 172.28.1.14 } }

    db-replica:
      image: postgres:17
      user: postgres
      # first boot: pg_basebackup -h 172.28.1.14 -U replicator
      #   -D /var/lib/postgresql/data -R -X stream -P
      volumes: [replica-data:/var/lib/postgresql/data]
      networks: { backend: { ipv4_address: 172.28.1.15 } }
    ```
    ```bash
    # db/init-primary.sh (runs once, on the first boot of .14)
    CREATE ROLE replicator WITH REPLICATION LOGIN ...;
    echo "host replication replicator 172.28.1.15/32 scram-sha-256" \
      >> "$PGDATA/pg_hba.conf"
    echo "host replication replicator 172.28.1.14/32 scram-sha-256" \
      >> "$PGDATA/pg_hba.conf"
    ```
  ],
  code-size: 10pt,
  [
    PostgreSQL runs on two nodes, both on the backend network. One is the primary and the other is a standby. Every change on the primary is copied to the standby as it happens. Replication is set up through:

    - _wal\_level=replica_: the primary streams its write-ahead log (WAL), the record of every change, to the standby
    - _init-primary.sh_: creates the `replicator` role and allows replication logins from both database addresses, so either node can copy from the other
    - _pg\_basebackup -R_: clones the primary, then writes `standby.signal` and the primary's address, so the copy starts as a standby and follows the stream
    - _named volumes_: each copy lives on its own volume, so a lost container does not lose its data

    The two nodes are equal. Either one can be the primary.
  ],
)

// ---------- Automatic failover ----------

#slide(
  [Automatic Failover],
  [
    ```bash
    # watchdog/watchdog.sh (condensed)
    NODES=(172.28.1.14 172.28.1.15)
    while true; do
      PRIMARIES=() BEST=""
      for node in "${NODES[@]}"; do
        # "role|timeline|wal position", empty if down
        IFS='|' read -r role tl lsn <<< "$(node_info "$node")"
        [ "$role" = f ] && PRIMARIES+=("$node")
        # BEST = standby with the highest timeline, then lsn
      done

      if [ "${#PRIMARIES[@]}" -eq 0 ]; then
        NO_PRIMARY=$((NO_PRIMARY + 1))
        if [ "$NO_PRIMARY" -ge 3 ] && [ -n "$BEST" ]; then
          RESULT="$(psql -h "$BEST" -U app -d appdb \
            -tAc "select pg_promote(true, 30);")"
          [ "$RESULT" = t ] && NO_PRIMARY=0
        fi
      else
        NO_PRIMARY=0
      fi
      sleep 5
    done
    ```
    ```text
    # JDBC URL used by auth, courses and grades
    jdbc:postgresql://172.28.1.14:5432,172.28.1.15:5432/appdb
        ?targetServerType=preferPrimary
    ```
  ],
  code-size: 10pt,
  [
    A watchdog container is the only thing that promotes a node. The services then follow on their own:

    - _real roles_: every 5 seconds it asks both nodes for their role, timeline and WAL position, so it remembers nothing and can restart at any time
    - _newest data wins_: after 3 checks in a row with no primary (about 20 s) it promotes the standby with the highest timeline, a counter that goes up on every promotion, then the furthest WAL position
    - _checked promotion_: `pg_promote(true, 30)` returns `t` only once the node really is primary; otherwise it retries on the next loop
    - _preferPrimary_: each service's JDBC URL lists both hosts, so new connections go to whichever one is the primary
    - _Hikari pool_: connections are replaced every 60 s, so none stay on a standby after a promotion

    One decider means two nodes never both promote themselves.
  ],
)

// ---------- Rejoining after a failure ----------

#slide(
  [Rejoining After a Failure],
  ```yaml
  # db-start/start.sh (condensed; both nodes run it,
  # PEER is the other node's address)
  PEER_INFO="$(psql -h "$PEER" -U app -d appdb -tAc \
    "select pg_is_in_recovery(), timeline_id
     from pg_control_checkpoint();" || true)"  # "f|2", empty = down

  if [ "$PEER_ROLE" = f ]; then                  # peer is primary
    [ -f "$DATA/standby.signal" ] && [ ! -f "$WAS_PRIMARY" ] \
      && [ "$MY_TL" -le "$PEER_TL" ] && start_postgres  # catch up
    copy_from_peer     # pg_basebackup to a temp folder, then swap
    start_postgres
  fi

  if [ ! -s "$DATA/PG_VERSION" ]; then           # empty disk
    [ "$ALLOW_INIT" = true ] && [ -z "$PEER_INFO" ] && start_postgres
    exit 1                                       # Docker retries
  fi

  if [ "$PEER_ROLE" = t ] && [ ! -f "$DATA/standby.signal" ] \
     && [ "$MY_TL" -ge "$PEER_TL" ]; then
    start_postgres                     # I was primary, still newest
  fi
  start_as_standby          # unsure: read-only, the watchdog decides
  ```,
  code-size: 10pt,
  [
    A node that comes back after a failover still holds its old files. Without a check, it would start as a second primary with stale data, and `preferPrimary` could send writes to it. This is called split brain. Every database node prevents it at startup:

    - _peer's role and timeline_: one query; a dead peer makes it give up after 3 seconds
    - _peer is primary_: a standby that is not ahead just reconnects and catches up; anything else copies the peer into a temp folder and only then replaces its old data
    - _empty disk_: only .14 (`ALLOW_INIT`) may create a new database, and only when no peer answers
    - _I was primary_: stay primary only if the peer is a standby on the same or an older timeline
    - _anything else_: start read-only and let the watchdog choose

    A node only becomes primary on its own when it is sure it has the newest data.
  ],
)

// ---------- Failover cycle ----------

#wide-slide(
  [One Full Failover Cycle],
  [
    #set text(size: 15pt)
    #show table.cell.where(y: 0): set text(fill: white, weight: "bold")
    #table(
      columns: (0.6in, 1.5in, 1.3in, 1.3in, 1fr),
      inset: (x: 12pt, y: 9pt),
      align: left + horizon,
      stroke: (bottom: 0.6pt + luma(200)),
      fill: (x, y) => if y == 0 { code-bg } else { none },
      table.header([Step], [Event], [.14], [.15], [What happens]),
      [0], [Start], [primary], [standby], [.15 streams every change from .14],
      [1], [.14 stops], [down], [*primary*], [The watchdog sees no primary for 3 checks and promotes .15],
      [2], [.14 starts], [*standby*], [primary], [.14's startup check sees .15 is primary and re-copies from it],
      [3], [.15 stops], [*primary*], [down], [The watchdog sees no primary for 3 checks and promotes .14],
      [4], [.15 starts], [primary], [*standby*], [.15 re-copies from .14. The roles are back to step 0],
    )
    #v(0.2in)
    We ran this cycle end to end, writing a row after steps 1 and 3. We also stopped both nodes after step 3 and started them together, before .15 had rejoined. .14 had the higher timeline, so it became primary, and every row was on both nodes.
  ],
)

// ---------- Design choices and limits ----------

#wide-slide(
  [Design Choices and Limits],
  [
    #set text(size: 14pt)
    #show table.cell.where(y: 0): set text(fill: white, weight: "bold")
    #table(
      columns: (2.3in, 1fr, 1fr),
      inset: (x: 12pt, y: 9pt),
      align: left + horizon,
      stroke: (bottom: 0.6pt + luma(200)),
      fill: (x, y) => if y == 0 { code-bg } else { none },
      table.header([Choice], [Why], [Cost]),
      [No automatic switch back], [Each switch means a few seconds of failed writes, and a node that failed once may fail again and keep taking the role back], [The roles stay swapped until a full reset with `docker compose down -v`],
      [Asynchronous replication], [Writes stay fast and keep working while the standby is down], [Writes in the last moment before a crash may not reach the standby],
      [One watchdog decides], [One short script that is easy to follow and test], [If the watchdog stops, there is no failover, but the database keeps serving],
      [Newest timeline wins], [Keeps the setup to plain PostgreSQL containers; an unsure node starts read-only instead of guessing], [If only one node answers, it is promoted even if the missing one had newer data. Patroni (with etcd) or pg_auto_failover avoid this by keeping a record outside the databases],
    )
  ],
)

// ---------- Summary ----------

#wide-slide(
  [Fault Tolerance Summary],
  [
    #set text(size: 15pt)
    #show table.cell.where(y: 0): set text(fill: white, weight: "bold")
    #table(
      columns: (1.6in, 1fr, 1fr),
      inset: (x: 12pt, y: 9pt),
      align: left + horizon,
      stroke: (bottom: 0.6pt + luma(200)),
      fill: (x, y) => if y == 0 { code-bg } else { none },
      table.header([Node down], [What stops], [What keeps working]),
      [view], [The website, since it is the only entry point], [Every feature node and all data; the site returns when the view restarts],
      [auth], [New logins], [Signed-in users: courses and grades verify tokens themselves],
      [courses], [Course list, enroll, drop, and the faculty section picker], [Student grades, login, logout],
      [grades], [Viewing and uploading grades], [Course list, enroll, drop, login, logout],
      [database primary], [Database writes, for about 20 s], [Everything, once the watchdog promotes the standby; the old primary rejoins as a standby when it restarts],
      [database standby], [Nothing visible; only the backup is lost], [Everything; the node catches up and rejoins when it restarts],
      [db-watchdog], [Automatic failover], [Everything; both databases keep running],
    )
    #v(0.2in)
    Each feature lives on exactly one node, every call between nodes is guarded, and either database node can take over from the other.
  ],
)
