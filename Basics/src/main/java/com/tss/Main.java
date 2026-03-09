package com.tss;


import com.tss.config.DbConnection;
import com.tss.controller.CourseController;
import com.tss.controller.EnrollmentController;
import com.tss.controller.StudentController;
import com.tss.repository.*;
import com.tss.service.*;

import java.sql.Connection;
import java.util.Scanner;

public class Main {
    private static CourseController courseController;
    private static StudentController studentController;
    private static EnrollmentController enrollmentController;

    public static void main(String[] args) {
        initalizer();

        start();
    }

    public static void initalizer() {
        Connection connection = DbConnection.connect();
        CourseRepository courseRepository = new CourseRepositoryImpl(connection);
        StudentRepository studentRepository = new StudentRepositoryImpl(connection);
        EnrollmentRepository enrollmentRepository = new EnrollmentRepositoryImpl(connection);
        AddressRepository addressRepository = new AddressRepositoryImpl(connection);

        StudentService studentService = new StudentServiceImpl(studentRepository, addressRepository);
        CourseService courseService = new CourseServiceImpl(courseRepository);
        AddressService addressService = new AddressServiceImpl(addressRepository);
        EnrollementService enrollmentService = new EnrollmentServiceImpl(enrollmentRepository);

        courseController = new CourseController(courseService);
        studentController = new StudentController(studentService);
        enrollmentController = new EnrollmentController(studentService, courseService, enrollmentService);
    }

    public static void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                System.out.println("\n1. Get all students" +
                        "\n2. Get all courses" +
                        "\n3. Add Student" +
                        "\n4. Add Course" +
                        "\n5. Assign course to student" +
                        "\n6. Get students by course" +
                        "\n7. Get course by student" +
                        "\n8. Exit");
                System.out.print("\nEnter your choice:");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1 -> studentController.displayAllStudents();
                    case 2 -> courseController.getAllCourse();
                    case 3 -> studentController.addStudent();
                    case 4 -> courseController.addCourse();
                    case 5 -> enrollmentController.assignCourse();
                    case 6 -> enrollmentController.getStudentsByCourse();
                    case 7 -> enrollmentController.getCourseByStudentId();
                    case 0 -> System.exit(0);
                    default -> System.out.println("Wrong choice");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}