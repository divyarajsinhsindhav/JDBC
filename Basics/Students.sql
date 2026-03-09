CREATE TABLE address (
     id SERIAL PRIMARY KEY,
     city VARCHAR(50) NOT NULL,
     state VARCHAR(50) NOT NULL,
     pincode CHAR(6) NOT NULL,

     CONSTRAINT chk_pincode
         CHECK (pincode ~ '^[0-9]{6}$')
);

CREATE TABLE students (
      id SERIAL PRIMARY KEY,
      roll_number INT NOT NULL UNIQUE,
      age INT NOT NULL,
      name VARCHAR(100) NOT NULL,
      address_id INT NOT NULL,

      CONSTRAINT chk_age
          CHECK (age BETWEEN 15 AND 100),

      CONSTRAINT fk_student_address
          FOREIGN KEY (address_id)
              REFERENCES address(id)
              ON DELETE CASCADE
);

CREATE TABLE course (
    course_id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    fees NUMERIC(10,2) NOT NULL,

    CONSTRAINT chk_fees
        CHECK (fees > 0)
);

CREATE TABLE enrolled (
      enrolled_id SERIAL PRIMARY KEY,
      student_id INT NOT NULL,
      course_id INT NOT NULL,

      CONSTRAINT fk_enroll_student
          FOREIGN KEY (student_id)
              REFERENCES students(roll_number)
              ON DELETE CASCADE,

      CONSTRAINT fk_enroll_course
          FOREIGN KEY (course_id)
              REFERENCES course(course_id)
              ON DELETE CASCADE,

      CONSTRAINT unique_enrollment
          UNIQUE (student_id, course_id)
);

INSERT INTO address (city, state, pincode) VALUES
       ('Rajkot',     'Gujarat',     '360001'),
       ('Surat',      'Gujarat',     '395001'),
       ('Ahmedabad',  'Gujarat',     '380001'),
       ('Vadodara',   'Gujarat',     '390001'),
       ('Bhavnagar',  'Gujarat',     '364001');

INSERT INTO students (roll_number, age, name, address_id) VALUES
      (101, 20, 'Aarav Shah',    1),
      (102, 22, 'Priya Patel',   2),
      (103, 21, 'Rohan Mehta',   3),
      (104, 19, 'Sneha Joshi',   4),
      (105, 23, 'Kiran Desai',   5);

INSERT INTO course (name, fees) VALUES
    ('Java Basics',        9999.00),
    ('Database Design',    7499.00),
    ('Web Development',    12999.00),
    ('Data Structures',    8999.00),
    ('Cloud Computing',    14999.00);

INSERT INTO enrolled (student_id, course_id) VALUES
     (101, 1),
     (101, 2),
     (102, 1),
     (102, 3),
     (103, 4),
     (104, 2),
     (104, 5),
     (105, 3),
     (105, 4),
     (105, 5);