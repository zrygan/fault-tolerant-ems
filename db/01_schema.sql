-- ============================================================
-- auth service owns: users
-- ============================================================
CREATE TABLE
    users (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        email TEXT NOT NULL UNIQUE,
        password_hash TEXT NOT NULL, -- bcrypt hash, never the plain password
        full_name TEXT NOT NULL,
        role TEXT NOT NULL CHECK (role IN ('student', 'faculty', 'admin')),
        created_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

-- ============================================================
-- course service owns: courses, sections, enrollments
-- ============================================================
-- The catalog: one row per subject, no matter how often it is offered.
CREATE TABLE
    courses (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        code TEXT NOT NULL UNIQUE, -- e.g. 'CSOPESY'
        title TEXT NOT NULL,
        units SMALLINT NOT NULL CHECK (units > 0)
    );

-- One offering of a course in one term, taught by one faculty member.
CREATE TABLE
    sections (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        course_id BIGINT NOT NULL REFERENCES courses (id),
        term TEXT NOT NULL, -- e.g. '2026-2027 T1'
        section_code TEXT NOT NULL, -- e.g. 'S11'
        faculty_id BIGINT REFERENCES users (id), -- may be empty until assigned
        capacity SMALLINT NOT NULL CHECK (capacity > 0),
        enrolled_count SMALLINT NOT NULL DEFAULT 0,
        is_open BOOLEAN NOT NULL DEFAULT TRUE, -- open for enrollment?
        UNIQUE (course_id, term, section_code),
        CHECK (enrolled_count BETWEEN 0 AND capacity) -- the database itself refuses overfilling
    );

CREATE TABLE
    enrollments (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        student_id BIGINT NOT NULL REFERENCES users (id),
        section_id BIGINT NOT NULL REFERENCES sections (id),
        enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now (),
        UNIQUE (student_id, section_id) -- no enrolling twice in one section
    );

-- ============================================================
-- grades service owns: grades
-- ============================================================
-- One grade per enrollment, so faculty can only grade students who enrolled.
CREATE TABLE
    grades (
        enrollment_id BIGINT PRIMARY KEY REFERENCES enrollments (id),
        grade NUMERIC(2, 1) NOT NULL CHECK (grade IN (0.0, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0)),
        uploaded_by BIGINT NOT NULL REFERENCES users (id),
        uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

-- ============================================================
-- clearance service owns: clearances
-- ============================================================
-- One row per hold placed on a student; active while resolved_on IS NULL.
CREATE TABLE
    clearances (
        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
        student_id BIGINT NOT NULL REFERENCES users (id),
        hold_type TEXT NOT NULL CHECK (
            hold_type IN (
                'incomplete_records',
                'disciplinary_hold',
                'library_hold',
                'financial_hold'
            )
        ),
        remarks TEXT,
        placed_by BIGINT NOT NULL REFERENCES users (id),
        placed_on TIMESTAMPTZ NOT NULL DEFAULT now (),
        resolved_by BIGINT REFERENCES users (id),
        resolved_on TIMESTAMPTZ -- NULL while active; set to now() when lifted
    );

-- ============================================================
-- Indexes for the lookups the app does most
-- ============================================================
CREATE INDEX idx_sections_term ON sections (term);

CREATE INDEX idx_sections_faculty ON sections (faculty_id);

CREATE INDEX idx_enrollments_student ON enrollments (student_id);

CREATE INDEX idx_enrollments_section ON enrollments (section_id);

CREATE UNIQUE INDEX idx_clearances_active_unique ON clearances (student_id, hold_type)
WHERE
    resolved_on IS NULL;
