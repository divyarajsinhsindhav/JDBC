package com.tss.repository;

import com.tss.entity.Address;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AddressRepositoryImpl implements AddressRepository {
    private Connection connection;

    public AddressRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Address> findAll() {
        List<Address> addresses = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM address");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                addresses.add(
                        new Address(resultSet.getInt("id"),
                                resultSet.getString("city"),
                                resultSet.getString("state"),
                                resultSet.getString("pincode"))
                );
            }
            return addresses;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return addresses;
    }

    @Override
    public Address findById(int id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM address WHERE id = ?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Address(resultSet.getInt("id"), resultSet.getString("city"), resultSet.getString("state"), resultSet.getString("pincode"));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    @Override
    public int save(Address address) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO address (city, state, pincode) VALUES (?, ?, ?) RETURNING id"
            );
            preparedStatement.setString(1, address.getCity());
            preparedStatement.setString(2, address.getState());
            preparedStatement.setString(3, address.getPincode());
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("id");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }
}
