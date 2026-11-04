CREATE DATABASE IF NOT EXISTS smart_campus
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_campus;

CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    branch VARCHAR(255) NOT NULL,
    section VARCHAR(10) NOT NULL,
    semester INT NOT NULL,
    PRIMARY KEY (student_id)
);

CREATE TABLE IF NOT EXISTS faculty (
    faculty_id VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    PRIMARY KEY (faculty_id)
);

CREATE TABLE IF NOT EXISTS courses (
    course_id VARCHAR(100) NOT NULL,
    course_name VARCHAR(255) NOT NULL,
    course_code VARCHAR(100) NOT NULL,
    credits INT NOT NULL,
    department VARCHAR(255) NOT NULL,
    PRIMARY KEY (course_id),
    CONSTRAINT uq_courses_course_code UNIQUE (course_code)
);
