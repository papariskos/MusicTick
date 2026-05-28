package com.musictick;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConfig — Κεντρική διαμόρφωση σύνδεσης βάσης δεδομένων.
 *
 * Για να αλλάξετε τα στοιχεία σύνδεσης (π.χ. κωδικό root),
 * αρκεί να τροποποιήσετε ΜΟΝΟ αυτό το αρχείο.
 *
 * Χρήση σε οποιοδήποτε αρχείο:
 *   Connection conn = DBConfig.getConnection();
 */
public class DBConfig {

    /** JDBC URL της βάσης δεδομένων MySQL */
    public static final String DB_URL =
        "jdbc:mysql://localhost:3306/musictick" +
        "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    /** Username σύνδεσης MySQL */
    public static final String DB_USER = "root";

    /** Κωδικός MySQL (κενό = χωρίς κωδικό) */
    public static final String DB_PASSWORD = "";

    /**
     * Επιστρέφει νέα σύνδεση προς τη βάση δεδομένων.
     * @throws SQLException αν αποτύχει η σύνδεση
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
