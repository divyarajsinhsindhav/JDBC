package com.tss.service;

import com.tss.entity.Address;
import com.tss.entity.Student;
import com.tss.repository.AddressRepository;
import com.tss.repository.StudentRepository;

import java.util.ArrayList;
import java.util.List;

public class StudentServiceImpl implements StudentService {
    private StudentRepository studentRepository;
    private AddressRepository addressRepository;

    public StudentServiceImpl(StudentRepository studentRepository, AddressRepository addressRepository) {
        this.studentRepository = studentRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public List<Student> getStudents() {
        return studentRepository.getStudents();
    }

    @Override
    public void addStudent(Student student, Address address) {
        int address_id = addressRepository.save(address);
        studentRepository.addStudent(student, address_id);
    }

    @Override
    public Student getStudent(int id) {
        return studentRepository.getStudentById(id);
    }

}
