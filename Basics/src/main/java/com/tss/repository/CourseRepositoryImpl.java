package com.tss.repository;

import com.tss.entity.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CourseRepositoryImpl implements CourseRepository {
    private Connection connection;

    public CourseRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM course");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                courses.add(
                        new Course(
                             resultSet.getInt("course_id"),
                             resultSet.getString("name"),
                             resultSet.getDouble("fees")
                        )
                );
            }
            return courses;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return courses;
    }

    @Override
    public Course findById(int id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM course WHERE course_id = ?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Course(resultSet.getInt("course_id"), resultSet.getString("name"), resultSet.getDouble("fees"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public void save(Course course) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO course(name, fees) VALUES (?, ?)");
            preparedStatement.setString(1, course.getCourseName());
            preparedStatement.setDouble(2, course.getFees());
            preparedStatement.executeUpdate();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
