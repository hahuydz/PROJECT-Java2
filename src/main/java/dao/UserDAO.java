package dao;

import Utils.DB_Utils;
import entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static UserDAO instance;
    public static synchronized UserDAO getInstance() {
        if(instance == null) instance = new UserDAO();
        return instance;
    }

    public List<User> getUser() {
        List<User> userList = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DB_Utils.getInstance().getConnection();
            preparedStatement = connection.prepareStatement("select * from users");
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                User user = new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        resultSet.getString("phone"),
                        resultSet.getString("address"),
                        resultSet.getString("role"),
                        new java.util.Date(resultSet.getTimestamp("created_at").getTime())
                );
                userList.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DB_Utils.closeAll(connection, preparedStatement, resultSet);
        }
        return userList;
    }
    public boolean register(String name, String email, String password) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DB_Utils.getInstance().getConnection();

            String sql = "INSERT INTO users(name, email, password, role) VALUES (?, ?, ?, 'CUSTOMER')";
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);

            int rows = preparedStatement.executeUpdate();

            return rows > 0; // true nếu insert thành công

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DB_Utils.closeAll(connection, preparedStatement, null);
        }
    }
    public User login(String name, String password) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DB_Utils.getInstance().getConnection();

            String sql = "SELECT * FROM users WHERE name = ? AND password = ?";
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, password);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        resultSet.getString("phone"),
                        resultSet.getString("address"),
                        resultSet.getString("role"),
                        new java.util.Date(resultSet.getTimestamp("created_at").getTime())
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DB_Utils.closeAll(connection, preparedStatement, resultSet);
        }

        return null; // không tìm thấy user
    }
}
