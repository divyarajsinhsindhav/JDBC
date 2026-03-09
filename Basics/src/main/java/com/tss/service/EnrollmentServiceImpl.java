package com.tss.service;

import com.tss.entity.Course;
import com.tss.entity.Student;
import com.tss.repository.EnrollmentRepository;

import java.util.List;

public class EnrollmentServiceImpl implements EnrollementService {

    private EnrollmentRepository enrollmentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public void enrollStudent(Course course, Student student) {
        enrollmentRepository.enrollStudent(course, student);
    }

    @Override
    public List<Student> getEnrolledStudents(int id) {
        return enrollmentRepository.getEnrolledStudents(id);
    }

    @Override
    public List<Course> getCoursesByStudent(int id) {
        return enrollmentRepository.getCoursesByStudent(id);
    }
}