package Utils;

import com.mysql.cj.jdbc.Driver;

import java.sql.*;

public class DB_Utils {
    private static DB_Utils instance;
    private Connection connection;

    private DB_Utils() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phone_shop?useSSL=false&serverTimezone=UTC",
                    "root",
                    "12345789");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized DB_Utils getInstance() {
        if (instance == null) {
            instance = new DB_Utils();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/phone_shop?useSSL=false&serverTimezone=UTC",
                        "root",
                        "12345789");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void closeAll(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if(resultSet != null) resultSet.close();
            if(preparedStatement != null) preparedStatement.close();
            if(connection != null) connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

