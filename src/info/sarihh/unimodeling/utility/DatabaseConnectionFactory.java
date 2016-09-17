package info.sarihh.unimodeling.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This factory class establishes a connection with a MySQL, PostgreSQL, or
 * Oracle DBMS.
 * Author: Sari Haj Hussein
 */
public class DatabaseConnectionFactory {

    /** This method connects to the DBMS, specified by driverName and located at
     * url, using the specified user and password. The method ensures that there
     * is only one instance of the Connection class. */
    public static Connection getDatabaseConnection(String driverName, String url, String user, String password) {
        switch (driverName) {
            case "MySQL Driver":
                conn = getConnection("com.mysql.jdbc.Driver", url, user, password);
                break;
            case "Oracle Thin Driver":
                conn = getConnection("oracle.jdbc.driver.OracleDriver", url, user, password);
                break;
            case "PostgreSQL Driver":
                conn = getConnection("org.postgresql.Driver", url, user, password);
                break;
            default:
                conn = getConnection("oracle.jdbc.driver.OracleDriver", url, user, password);
                break;
        }
        return conn;
    }

    private static Connection getConnection(String className, String url, String user, String password) {
        try {
            Class.forName(className);
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
    }
    private static Connection conn;
}