package com.tss.controller;

import com.tss.entity.Course;
import com.tss.entity.Student;
import com.tss.service.CourseService;
import com.tss.service.EnrollementService;
import com.tss.service.StudentService;

import java.util.Scanner;

public class EnrollmentController {
    private StudentService studentService;
    private CourseService courseService;
    private EnrollementService enrollementService;

    Scanner scanner = new Scanner(System.in);

    public EnrollmentController(StudentService studentService, CourseService courseService, EnrollementService enrollementService) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.enrollementService = enrollementService;
    }

    public void assignCourse() {
        System.out.println("\nList of students: ");
        studentService.getStudents().forEach(System.out::println);
        System.out.println("\nEnter Student Roll Number: ");
        int studentId = scanner.nextInt();

        System.out.println("\nList of courses:");
        courseService.getCourses().forEach(System.out::println);
        System.out.println("\nEnter course id:");
        int id = scanner.nextInt();

        Course course = courseService.getCourseById(id);
        Student student = studentService.getStudent(studentId);

        enrollementService.enrollStudent(course, student);
    }

    public void getStudentsByCourse() {
        System.out.println("\nEnter course id: ");
        int courseId = scanner.nextInt();

        System.out.println("\nList of students: ");
        enrollementService.getEnrolledStudents(courseId).forEach(System.out::println);
    }

    public void getCourseByStudentId() {
        System.out.println("\nEnter Roll Number: ");
        int studentId = scanner.nextInt();

        System.out.println("\nList of courses: ");
        enrollementService.getCoursesByStudent(studentId).forEach(System.out::println);
    }
}