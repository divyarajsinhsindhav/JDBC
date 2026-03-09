package com.tss.controller;

import com.tss.entity.Address;
import com.tss.entity.Student;
import com.tss.service.StudentService;

import java.util.List;
import java.util.Scanner;

public class StudentController {
    private StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    public void displayAllStudents() {
        List<Student> students = studentService.getStudents();
        for (Student student : students) {
            System.out.println(student);
        }
    }

    public void addStudent() {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Roll Number: ");
        int rollNumber = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Age: ");
        int age = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter city: ");
        String city = scanner.nextLine();

        System.out.print("Enter state: ");
        String state = scanner.nextLine();

        System.out.print("Enter pincode: ");
        String pincode = scanner.nextLine();

        Student student = new Student(rollNumber, age, name);
        Address address = new Address(city, state, pincode);

        studentService.addStudent(student, address);
    }


}
