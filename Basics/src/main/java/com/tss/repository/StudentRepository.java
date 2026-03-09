package com.tss.repository;

import com.tss.entity.Student;

import java.util.List;

public interface StudentRepository {
    List<Student> getStudents();
    void addStudent(Student student, int address_id);
    Student getStudentById(int id);
}
