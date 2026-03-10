package org.example;

import java.sql.*;

public class Main {

    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/accounts";
        String user = "postgres";
        String password = "Temp@123456";

        Connection con = null;

        try {
            con = DriverManager.getConnection(url, user, password);

            // Start transaction
            con.setAutoCommit(false);

            PreparedStatement credit = con.prepareStatement(
                    "UPDATE accounts SET balance = balance + 980 WHERE account_id = ?");

            PreparedStatement debit = con.prepareStatement(
                    "UPDATE accounts SET balance = balance - 980 WHERE account_id = ?");


            // Debit from Account 1
            debit.setInt(1, 1);
            debit.executeUpdate();

            // Credit to Account 2
            credit.setInt(1, 2);
            credit.executeUpdate();

            // If both succeed
            con.commit();
            System.out.println("Transaction committed successfully");

        } catch (Exception e) {

            try {
                if (con != null) {
                    con.rollback();
                    System.out.println("Transaction rolled back");
                }
            } catch (SQLException rollbackEx) {
                System.out.println(e.getMessage());
            }

            System.out.println(e.getMessage());
        }

        finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}