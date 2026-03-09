package com.tss.repository;

import com.tss.entity.Course;
import com.tss.entity.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentRepositoryImpl implements EnrollmentRepository {

    private Connection connection;

    public EnrollmentRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void enrollStudent(Course course, Student student) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO enrolled (course_id, student_id) VALUES (?, ?)"
            );
            preparedStatement.setInt(1, course.getCourseId());
            preparedStatement.setInt(2, student.getRollNumber());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Student> getEnrolledStudents(int id) {
        List<Student> students = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT s.* FROM enrolled e JOIN students s ON e.student_id = s.roll_number WHERE e.course_id = ?"
            );
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                students.add(new Student(
                        resultSet.getInt("id"),
                        resultSet.getInt("roll_number"),
                        resultSet.getInt("age"),
                        resultSet.getString("name")
                ));
            }
            return students;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return students;
    }

    @Override
    public List<Course> getCoursesByStudent(int id) {
        List<Course> courses = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT c.* FROM enrolled e JOIN course c ON e.course_id = c.course_id WHERE e.student_id = ?"
            );
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                courses.add(new Course(
                        resultSet.getInt("course_id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("fees")
                ));
            }
            return courses;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return courses;
    }
}