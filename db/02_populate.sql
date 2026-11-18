-- *Note: made with gemini

-- ============================================================
-- Seed Data for PostgreSQL Database (Users, Courses, Sections, Enrollments, Grades)
-- Matches constraints in schema.sql
-- ============================================================

BEGIN;

-- 1. Insert 10 Faculty (IDs 1-10) & 20 Students (IDs 11-30)
INSERT INTO users (email, password_hash, full_name, role) VALUES
('ramon.santos@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Dr. Ramon Santos', 'faculty'),
('elena.reyes@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Prof. Elena Reyes', 'faculty'),
('miguel.cruz@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Dr. Miguel Cruz', 'faculty'),
('sofia.bautista@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Prof. Sofia Bautista', 'faculty'),
('antonio.mendoza@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Dr. Antonio Mendoza', 'faculty'),
('carmela.garcia@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Prof. Carmela Garcia', 'faculty'),
('rafael.torres@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Dr. Rafael Torres', 'faculty'),
('isabella.flores@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Prof. Isabella Flores', 'faculty'),
('gabriel.lim@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Dr. Gabriel Lim', 'faculty'),
('victoria.tan@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Prof. Victoria Tan', 'faculty'),
('juan_dela_cruz@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Juan Dela Cruz', 'student'),
('maria_clara@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Maria Clara', 'student'),
('jose_rizal@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Jose Rizal', 'student'),
('andres_bonifacio@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Andres Bonifacio', 'student'),
('gabriela_silang@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Gabriela Silang', 'student'),
('emilio_aguinaldo@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Emilio Aguinaldo', 'student'),
('apolinario_mabini@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Apolinario Mabini', 'student'),
('melchora_aquino@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Melchora Aquino', 'student'),
('antonio_luna@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Antonio Luna', 'student'),
('gregorio_del_pilar@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Gregorio Del Pilar', 'student'),
('marcelo_del_pilar@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Marcelo Del Pilar', 'student'),
('graciano_lopez_jaena@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Graciano Lopez Jaena', 'student'),
('teresa_magbanua@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Teresa Magbanua', 'student'),
('diego_silang@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Diego Silang', 'student'),
('lapu_lapu@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Lapu-Lapu', 'student'),
('rajah_sulayman@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Rajah Sulayman', 'student'),
('tandang_sora@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Tandang Sora', 'student'),
('leona_florentino@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Leona Florentino', 'student'),
('francisco_balagtas@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Francisco Balagtas', 'student'),
('mister_dog@university.edu', '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Mister Dog', 'student');

-- 2. Insert 20 Courses (IDs 1-20)
INSERT INTO courses (code, title, units) VALUES
('CCPROG1', 'Logic Formulation and Introductory Programming', 3),
('CCPROG2', 'Programming with Structured Data Types', 3),
('CCPROG3', 'Object-Oriented Programming', 3),
('CCDSALG', 'Data Structures and Algorithms', 3),
('CSARCH1', 'Introduction to Computer Organization and Architecture 1', 3),
('CSARCH2', 'Introduction to Computer Organization and Architecture 2', 3),
('CSOPESY', 'Introduction to Operating Systems', 3),
('CSNETWK', 'Introduction to Computer Networks', 3),
('CCINFOM', 'Information Management', 3),
('CSSWENG', 'Software Engineering', 3),
('CSALGCM', 'Algorithms and Complexity', 3),
('CSINTSY', 'Intelligent Systems', 3),
('STALGCM', 'Advanced Algorithms and Complexity', 3),
('STSWENG', 'Advanced Software Engineering', 3),
('STDISCM', 'Distributed Computing', 3),
('CSMATH1', 'Differential Calculus', 3),
('CSMATH2', 'Integral Calculus', 3),
('CSADPRG', 'Advanced Programming and Theory of Programming Languages', 3),
('THS-ST1', 'Computer Science Thesis 1', 2),
('THS-ST2', 'Computer Science Thesis 2', 2);

-- 3. Insert 40 Sections (IDs 1-40)
INSERT INTO sections (course_id, term, section_code, faculty_id, capacity, enrolled_count, is_open) VALUES
(1, '2026-2027 T1', 'S11', 2, 30, 0, TRUE),
(1, '2026-2027 T1', 'S12', 5, 30, 0, TRUE),
(2, '2026-2027 T1', 'S11', 4, 30, 0, TRUE),
(2, '2026-2027 T1', 'S12', 2, 40, 0, TRUE),
(3, '2026-2027 T1', 'S11', 9, 30, 0, TRUE),
(3, '2026-2027 T1', 'S12', 10, 35, 0, TRUE),
(4, '2026-2027 T1', 'S11', 1, 30, 0, TRUE),
(4, '2026-2027 T1', 'S12', 2, 30, 0, TRUE),
(5, '2026-2027 T1', 'S11', 4, 40, 0, TRUE),
(5, '2026-2027 T1', 'S12', 10, 30, 0, TRUE),
(6, '2026-2027 T1', 'S11', 9, 30, 0, TRUE),
(6, '2026-2027 T1', 'S12', 9, 35, 0, TRUE),
(7, '2026-2027 T1', 'S11', 4, 35, 0, TRUE),
(7, '2026-2027 T1', 'S12', 10, 35, 0, TRUE),
(8, '2026-2027 T1', 'S11', 1, 30, 0, TRUE),
(8, '2026-2027 T1', 'S12', 7, 35, 0, TRUE),
(9, '2026-2027 T1', 'S11', 5, 30, 0, TRUE),
(9, '2026-2027 T1', 'S12', 4, 35, 0, TRUE),
(10, '2026-2027 T1', 'S11', 2, 30, 0, TRUE),
(10, '2026-2027 T1', 'S12', 7, 30, 0, TRUE),
(11, '2026-2027 T1', 'S11', 6, 35, 0, TRUE),
(11, '2026-2027 T1', 'S12', 10, 35, 0, TRUE),
(12, '2026-2027 T1', 'S11', 1, 40, 0, TRUE),
(12, '2026-2027 T1', 'S12', 8, 40, 0, TRUE),
(13, '2026-2027 T1', 'S11', 2, 35, 0, TRUE),
(13, '2026-2027 T1', 'S12', 2, 40, 0, TRUE),
(14, '2026-2027 T1', 'S11', 5, 40, 0, TRUE),
(14, '2026-2027 T1', 'S12', 10, 35, 0, TRUE),
(15, '2026-2027 T1', 'S11', 10, 30, 0, TRUE),
(15, '2026-2027 T1', 'S12', 2, 30, 0, TRUE),
(16, '2026-2027 T1', 'S11', 4, 35, 0, TRUE),
(16, '2026-2027 T1', 'S12', 2, 30, 0, TRUE),
(17, '2026-2027 T1', 'S11', 2, 35, 0, TRUE),
(17, '2026-2027 T1', 'S12', 5, 35, 0, TRUE),
(18, '2026-2027 T1', 'S11', 6, 30, 0, TRUE),
(18, '2026-2027 T1', 'S12', 6, 35, 0, TRUE),
(19, '2026-2027 T1', 'S11', 4, 40, 0, TRUE),
(19, '2026-2027 T1', 'S12', 5, 40, 0, TRUE),
(20, '2026-2027 T1', 'S11', 2, 40, 0, TRUE),
(20, '2026-2027 T1', 'S12', 3, 40, 0, TRUE);

-- 4. Insert Enrollments
INSERT INTO enrollments (student_id, section_id) VALUES
(11, 8),
(11, 2),
(11, 18),
(11, 16),
(11, 15),
(12, 7),
(12, 35),
(12, 6),
(13, 28),
(13, 3),
(13, 2),
(13, 6),
(13, 14),
(14, 33),
(14, 39),
(14, 2),
(15, 13),
(15, 35),
(15, 27),
(15, 15),
(15, 29),
(16, 18),
(16, 1),
(16, 11),
(16, 28),
(16, 22),
(17, 10),
(17, 14),
(17, 22),
(17, 7),
(18, 25),
(18, 7),
(18, 23),
(19, 39),
(19, 17),
(19, 3),
(19, 30),
(20, 8),
(20, 25),
(20, 6),
(20, 36),
(20, 19),
(21, 40),
(21, 24),
(21, 37),
(21, 13),
(21, 5),
(22, 15),
(22, 19),
(22, 6),
(23, 7),
(23, 25),
(23, 18),
(24, 24),
(24, 11),
(24, 23),
(24, 14),
(25, 18),
(25, 5),
(25, 39),
(25, 11),
(25, 35),
(26, 16),
(26, 11),
(26, 30),
(26, 25),
(26, 18),
(27, 36),
(27, 15),
(27, 21),
(27, 4),
(27, 3),
(28, 26),
(28, 18),
(28, 5),
(28, 14),
(29, 21),
(29, 14),
(29, 32),
(29, 26),
(29, 30),
(30, 17),
(30, 9),
(30, 16);

-- 5. Update section enrolled counts to match actual enrollments
UPDATE sections SET enrolled_count = 1 WHERE id = 1;
UPDATE sections SET enrolled_count = 3 WHERE id = 2;
UPDATE sections SET enrolled_count = 3 WHERE id = 3;
UPDATE sections SET enrolled_count = 1 WHERE id = 4;
UPDATE sections SET enrolled_count = 3 WHERE id = 5;
UPDATE sections SET enrolled_count = 4 WHERE id = 6;
UPDATE sections SET enrolled_count = 4 WHERE id = 7;
UPDATE sections SET enrolled_count = 2 WHERE id = 8;
UPDATE sections SET enrolled_count = 1 WHERE id = 9;
UPDATE sections SET enrolled_count = 1 WHERE id = 10;
UPDATE sections SET enrolled_count = 4 WHERE id = 11;
UPDATE sections SET enrolled_count = 0 WHERE id = 12;
UPDATE sections SET enrolled_count = 2 WHERE id = 13;
UPDATE sections SET enrolled_count = 5 WHERE id = 14;
UPDATE sections SET enrolled_count = 4 WHERE id = 15;
UPDATE sections SET enrolled_count = 3 WHERE id = 16;
UPDATE sections SET enrolled_count = 2 WHERE id = 17;
UPDATE sections SET enrolled_count = 6 WHERE id = 18;
UPDATE sections SET enrolled_count = 2 WHERE id = 19;
UPDATE sections SET enrolled_count = 0 WHERE id = 20;
UPDATE sections SET enrolled_count = 2 WHERE id = 21;
UPDATE sections SET enrolled_count = 2 WHERE id = 22;
UPDATE sections SET enrolled_count = 2 WHERE id = 23;
UPDATE sections SET enrolled_count = 2 WHERE id = 24;
UPDATE sections SET enrolled_count = 4 WHERE id = 25;
UPDATE sections SET enrolled_count = 2 WHERE id = 26;
UPDATE sections SET enrolled_count = 1 WHERE id = 27;
UPDATE sections SET enrolled_count = 2 WHERE id = 28;
UPDATE sections SET enrolled_count = 1 WHERE id = 29;
UPDATE sections SET enrolled_count = 3 WHERE id = 30;
UPDATE sections SET enrolled_count = 0 WHERE id = 31;
UPDATE sections SET enrolled_count = 1 WHERE id = 32;
UPDATE sections SET enrolled_count = 1 WHERE id = 33;
UPDATE sections SET enrolled_count = 0 WHERE id = 34;
UPDATE sections SET enrolled_count = 3 WHERE id = 35;
UPDATE sections SET enrolled_count = 2 WHERE id = 36;
UPDATE sections SET enrolled_count = 1 WHERE id = 37;
UPDATE sections SET enrolled_count = 0 WHERE id = 38;
UPDATE sections SET enrolled_count = 3 WHERE id = 39;
UPDATE sections SET enrolled_count = 1 WHERE id = 40;

-- 6. Insert Grades for each enrollment (matching allowed numeric scale)
INSERT INTO grades (enrollment_id, grade, uploaded_by) VALUES
(1, 2.5, 10),
(2, 3.5, 10),
(3, 3.5, 6),
(4, 2.0, 3),
(5, 4.0, 2),
(6, 0.0, 2),
(7, 1.5, 3),
(8, 3.5, 10),
(9, 1.0, 7),
(10, 3.5, 10),
(11, 4.0, 9),
(12, 2.5, 9),
(13, 0.0, 2),
(14, 2.5, 6),
(15, 1.0, 5),
(16, 3.5, 3),
(17, 4.0, 1),
(18, 2.5, 9),
(19, 1.5, 9),
(20, 1.0, 5),
(21, 2.0, 3),
(22, 3.0, 3),
(23, 0.0, 10),
(24, 3.0, 8),
(25, 0.0, 2),
(26, 3.0, 5),
(27, 2.0, 1),
(28, 2.0, 10),
(29, 1.0, 2),
(30, 4.0, 2),
(31, 1.5, 3),
(32, 4.0, 9),
(33, 1.5, 5),
(34, 3.5, 4),
(35, 2.0, 5),
(36, 3.5, 6),
(37, 4.0, 9),
(38, 4.0, 2),
(39, 2.0, 4),
(40, 1.0, 6),
(41, 0.0, 10),
(42, 2.0, 10),
(43, 2.0, 1),
(44, 1.0, 1),
(45, 2.0, 2),
(46, 0.0, 6),
(47, 1.0, 9),
(48, 2.0, 5),
(49, 4.0, 4),
(50, 1.5, 10),
(51, 4.0, 4),
(52, 4.0, 7),
(53, 2.0, 2),
(54, 1.0, 7),
(55, 3.0, 7),
(56, 3.5, 8),
(57, 0.0, 2),
(58, 0.0, 7),
(59, 3.0, 2),
(60, 2.0, 4),
(61, 2.0, 9),
(62, 4.0, 3),
(63, 3.5, 3),
(64, 2.5, 8),
(65, 2.0, 2),
(66, 4.0, 9),
(67, 1.0, 1),
(68, 0.0, 2),
(69, 2.0, 3),
(70, 3.5, 8),
(71, 4.0, 4),
(72, 3.5, 1),
(73, 1.5, 7),
(74, 0.0, 7),
(75, 2.5, 8),
(76, 2.5, 7),
(77, 4.0, 3),
(78, 2.0, 5),
(79, 2.0, 1),
(80, 0.0, 6),
(81, 0.0, 1),
(82, 4.0, 9),
(83, 1.5, 1),
(84, 1.0, 3);

COMMIT;