/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbsync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Diese Klasse repraesentiert die Verbindung zu der Datenbank eines Users.<br>
 * Die Informationen ueber diesen User werden Klasse im Konstruktor uebergeben. <br>
 * Zuerst wird mittels JDBC eine Datenbankverbindung aufgebaut, danach kann <br>
 * der Benutzer auf der Datenbank beliebig viele Queries absetzen.
 * 
 * @author Gabriel Pawlowsky
 * @author Josef Sochovsky
 * @version 2.0
 */
public class DataBaseConnection {

    // ConfigLoader, in dem gespeichert ist welche Tabellen und Spalten gelesen
    // werden sollen.
    private Config c;
    // Die Treiber fuer die Verbindung zu postgresql und mysql
    static final String JDBC_DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    // In diesem boolean wird gespeichert, ob man gerade mit der Datenbank
    // verbunden ist oder nicht
    private boolean connectedToDatabase = false;
    // Verbindung und Statement für die DB-Verbindung
    private Connection connection;
    private Statement statement;
    // Hierin werden die kompletten ausgelsenen Daten gespeichert.
    private String[][] fullData;
    //Der Log4j-Logger
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DataBaseConnection.class);

    /**
     * Konstruktor, der die Parameter speichert und je nach spezifiziertem
     * dbms<br> die Lesenoperation mit den richtigen Daten startet.
     *
     * @param cl ConfigLoader der die Daten enthaelt
     * @throws ClassNotFoundException Exception wird ausgeloest, wenn die
     * Treiberklasse nicht<br> gefunden werden kann
     * @throws SQLException wird ausgeloest, wenn die DB-Verbindung nicht
     * ordnungsgemaess<br> aufgebaut werden kann
     */
    public DataBaseConnection(Config c) {
        try {
            // Speichern des Paramters, um Daten aus dem Config-File buntzen zu
            // koennen
            this.c = c;

            // fuerhre je nach uebergabeparameter die lese-Methode mit den richtigen
            // daten(usernmae, passwort, ...) zum spezifizierten dbms aus
            readData(JDBC_DRIVER_MYSQL, "jdbc:mysql://" + c.getMyAddress()
                    + "/" + c.getMyDatabase(), c.getMyUser(),
                    c.getMyPassword());
        } catch (ClassNotFoundException ex) {
            logger.warn(ex.getMessage());
        } catch (SQLException ex) {
            logger.warn(ex.getMessage());
        }
    }

    /**
     * Diese Methode liest mit den uebergebenen Daten, alle spezifizierten
     * Daten<br> aus der Datenbank und speichert diese in das Attribut fullData
     *
     * @param driver Treiberklassenbezeichnung fuer den Treiber des
     * spezifizierten<br> DBMS
     * @param url url zur Datenbank
     * @param username Usernamen auf der Datenbank
     * @param password Passwort zum angegeben Usernamen auf der Datenbank
     * @throws SQLException wird ausgeloest, wenn die DB-Verbindung nicht
     * ordnungsgemaess<br> aufgebaut werden kann
     */
    private void readData(String driver, String url, String username,
            String password)
            throws ClassNotFoundException, SQLException {
        // Gibt die Klasse des Treibers zurueck
        Class.forName(driver);

        // Erstellen der Datenbankverbindung mit hilfe der uebergebenen Werte
        connection = DriverManager.getConnection(url, username, password);

        // Erzeugen eines Statements mithilfe der Verbindung
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_READ_ONLY);

        // Speichern, dass die Datenbank nun Verbunden ist, falls alles
        // fehlerfrei abgelaufen ist
        connectedToDatabase = true;

        this.fullData = convertResultSetToArray(setQuery("SELECT * FROM artikel"));
    }

    /**
     * Methode, die eine nicht-Datenbank-verändernde Query ausfuehrt
     *
     * @param query Query, die ausgefuehrt werden soll
     * @throws SQLException Exception, die bei SQL Fehlern geworfen wird
     * @throws IllegalStateException Exception, die geworfen wird wenn die
     * Datenbank nicht<br> Verbunden ist
     */
    private ResultSet setQuery(String query) throws SQLException,
            IllegalStateException {
        // Ueberpruefung, ob die Datenbank-Verbindung in Ordnung ist,
        // anderenfalls werfen einer Exception
        if (!connectedToDatabase) {
            throw new IllegalStateException("Not Connected to Database");
        }

        // Ausfuehren der Query
        ResultSet resultSet = statement.executeQuery(query);

        // Setzen des Cursors auf das letzte Elemement
        // resultSet.last();

        return resultSet;
    }
/**
     * Methode, die eine Datenbank-verändernde Query ausfuehrt
     *
     * @param query Query, die ausgefuehrt werden soll
     * @throws SQLException Exception, die bei SQL Fehlern geworfen wird
     * @throws IllegalStateException Exception, die geworfen wird wenn die
     * Datenbank nicht<br> Verbunden ist
     */
    public int setUpdate(String query) throws SQLException,
            IllegalStateException {
        // Ueberpruefung, ob die Datenbank-Verbindung in Ordnung ist,
        // anderenfalls werfen einer Exception
        if (!connectedToDatabase) {
            throw new IllegalStateException("Not Connected to Database");
        }

        // Ausfuehren der Query
        int rowsAffected = statement.executeUpdate(query);

        return rowsAffected;
    }

    /**
     * Getter-Methode
     * 
     * @return the fullData
     */
    public String[][] getFullData() {
        return fullData;
    }

    /**
     * Getter-Methode
     * 
     * @return die Config dieser Datenbankverbindung.
     */
    public Config getC() {
        return c;
    }
    
    /**
     * Diese Methode sollte erst zum beenden eines Objektes dieser Klasse<br>
     * aufgerufen werden, da sie die aktuelle Datenbankverbindung schliesst.
     * 
     * @throws SQLException Tritt auf wenn nicht alle Verbindungen zur Datenbank<br>
     * geloescht werden konnten.
     */
    public void closeConnection() throws SQLException {
        statement.close();
        connection.close();
    }

    /**
     * Diese Methode konvertiert ein ResultSet zu einem String[][] Array damit
     * <br> man es in die uebergeordnete ArrayList speichern kann
     *
     * @param rs Das ResultSet das konvertiert werden soll
     * @return ein Array fuer das Gesamtergebnis
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    public String[][] convertResultSetToArray(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        ArrayList rows = new ArrayList();
        while (rs.next()) {
            String[] row = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getString(i);
            }
            rows.add(row);
        }

        rs.close();

        return (String[][]) rows.toArray(new String[rows.size()][columnCount]);
    }
}
