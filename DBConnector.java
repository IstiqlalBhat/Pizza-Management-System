package cpsc4620;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBConnector {

    // enter your user name here
    protected static String user = "admin";
    // enter your password here
    protected static String password = "Istiqlal1234";
    // enter your database name here
    private static String database_name = "PIZZERIA";
    // Do not change the port. 3306 is the default MySQL port
    private static String url = "jdbc:mysql://db2.cvgk6ig0avy2.us-east-1.rds.amazonaws.com:3306";
    private static Connection conn;


    /**
     * This function will handle the connection to the database
     *
     * @return true if the connection was successfully made
     * @throws SQLException
     * @throws IOException
     */
    public static Connection make_connection() throws SQLException, IOException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load the driver");

            System.out.println("Message     : " + e.getMessage());

            return null;
        }

        conn = DriverManager.getConnection(url + "/" + database_name, user, password);
        return conn;
    }
}
