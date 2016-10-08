/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbsync;

/**
 * Diese Klasse repraesentiert die Konfiguration einer Datenbankverbidnung <br>
 * zu einem spezifischen Host. Dazu werden hierin alle benoetigten Informationen <br>
 * fuer den Aufbau einer Verbindung gespeichert.
 * 
 * @author Gabriel Pawlowsky
 * @version 1.0
 */
public class Config {

    //In diesen Variablen werden die Informationen gespeichert, die zur Verbidnung
    //auf die Datenbank eines spezifischen Hosts benoetigt werden.
    private String myAddress;
    private String myUser;
    private String myPassword;
    private String myDatabase;

    /**
     * @return the myAddress
     */
    public String getMyAddress() {
        return myAddress;
    }

    /**
     * @param myAddress the myAddress to set
     */
    public void setMyAddress(String myAddress) {
        this.myAddress = myAddress;
    }

    /**
     * @return the myUser
     */
    public String getMyUser() {
        return myUser;
    }

    /**
     * @param myUser the myUser to set
     */
    public void setMyUser(String myUser) {
        this.myUser = myUser;
    }

    /**
     * @return the myPassword
     */
    public String getMyPassword() {
        return myPassword;
    }

    /**
     * @param myPassword the myPassword to set
     */
    public void setMyPassword(String myPassword) {
        this.myPassword = myPassword;
    }

    /**
     * @return the myDatabase
     */
    public String getMyDatabase() {
        return myDatabase;
    }

    /**
     * @param myDatabase the myDatabase to set
     */
    public void setMyDatabase(String myDatabase) {
        this.myDatabase = myDatabase;
    }    
}
