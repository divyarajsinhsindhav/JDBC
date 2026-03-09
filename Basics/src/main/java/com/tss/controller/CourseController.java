package com.tss.controller;

import com.tss.entity.Course;
import com.tss.service.CourseService;
import com.tss.service.StudentService;

import java.util.Scanner;

public class CourseController {
    private CourseService courseService;
    Scanner scanner = new Scanner(System.in);

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    public void addCourse() {
        System.out.print("\nEnter course name: ");
        String courseName = scanner.nextLine();

        System.out.print("Enter course fees: ");
        double fees = scanner.nextDouble();

        Course course = new Course(courseName, fees);
        courseService.addCourse(course);
    }

    public void getAllCourse() {
        System.out.println("\nList of courses: ");
        courseService.getCourses()
                .forEach(System.out::println);
    }

}
