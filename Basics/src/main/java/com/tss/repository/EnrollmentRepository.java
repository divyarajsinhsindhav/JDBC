package com.tss.repository;

import com.tss.entity.Course;
import com.tss.entity.Student;

import java.util.List;

public interface EnrollmentRepository {
    void enrollStudent(Course course, Student student);
    List<Student> getEnrolledStudents(int id);
    List<Course> getCoursesByStudent(int id);
}
