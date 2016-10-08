package dbsync;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Diese Klasse handlet das mergen der Datenbanken. Die mergeTables-Methode <br>
 * bekommt eine beliebige Anzahl an Datensaetzen aus verschieden vielen Datenbanken <br>
 * und merged alle diese zusammen. Anschliessend erzeugt sie mittels der anderen Hilfmethoden <br>
 * INSERT-, UDAPTE- und DELETE-Queries, die alle Datenbanken auf den richtigen aktuellen Stand bringen.
 * 
 * @author Gabriel Pawlowsky
 * @version 1.0
 */
public class DatabaseHandler {
    
    /**
     * Diese Methode bekommt eine beliebige Anzahl an Datensaetzen aus verschieden vielen Datenbanken <br>
     * und merged alle diese zusammen. Anschliessend erzeugt sie mittels der anderen Hilfmethoden <br>
     * INSERT-, UDAPTE- und DELETE-Queries, die alle Datenbanken auf den richtigen aktuellen Stand bringen.
     * 
     * @param tableData die alle Daten mehrerer Datenbanken. Jedes String[][] beschreibt dabei 
     * alle Daten einer Datenbank
     * @return Alle Queries, die benoetigt werden um die Datenbanken wieder auf den<br>
     * Stand zu bringen. Dabei werden die Queries jeweils in der uebergeordneten ArrayList<br>
     * in der selben Reihenfolge gespeichert wie sie in die ubergebenen Datenbanken-Daten <br>
     * eingefuegt werden muessen.
     */
    public static ArrayList<ArrayList<String>> mergeTables(ArrayList<String[][]> tableData) {
        ArrayList<ArrayList<String>> changeQueries = new ArrayList<>();
        ArrayList<ArrayList<String>> realTable = new ArrayList<>();

        //Save first table to real table
        for (String[] line : tableData.get(0)) {
            ArrayList<String> tempList = new ArrayList<>();
            for (String cell : line) {
                tempList.add(cell);
            }
            if(line[line.length-1].equals("1"))
                tempList.add("delete");
            else
                tempList.add("old");
            realTable.add(tempList);
        }

        ArrayList<String> removedCellIds = new ArrayList<>();
        //for each table more than one
        for (int i = 0; i < tableData.size(); i++) {
            String[][] currentTable = tableData.get(i);
            //for each line
            for (String[] currentLine : currentTable) {
                String id = currentLine[0];
                boolean contains = false;
                //test if this current line existis in realTable
                for (int z = 0; z < realTable.size(); z++) {
                    ArrayList<String> realTableLine = realTable.get(z);
                    //if line is there
                    if (id.equals(realTableLine.get(0))) {
                        contains = true;
                        boolean areEqual = true;
                        //test if they are equal
                        for (int j = 0; j < currentLine.length; j++) {
                            if (!currentLine[j].equals(realTableLine.get(j))) {
                                areEqual = false;
                            }
                        }
                        //if not check version
                        if (!areEqual) {
                            if (currentLine[currentLine.length - 1].equals("0")) {
                                //if version of current is higher, update
                                if (Integer.parseInt(currentLine[1]) > Integer.parseInt(realTableLine.get(1))) {
                                    //update line
                                    realTable.remove(realTableLine);
                                    ArrayList<String> tempList = new ArrayList<>();
                                    for (String cell : currentLine) {
                                        tempList.add(cell);
                                    }
                                    tempList.add("update");
                                    realTable.add(tempList);
                                }
                            } else if (currentLine[currentLine.length - 1].equals("1")) {
                                //if version of current is higher, update
                                if (Integer.parseInt(currentLine[1]) > Integer.parseInt(realTableLine.get(1))) {
                                    //delete line
                                    realTable.remove(realTableLine);
                                    ArrayList<String> tempList = new ArrayList<>();
                                    for (String cell : currentLine) {
                                        tempList.add(cell);
                                    }
                                    tempList.add("delete");
                                    realTable.add(tempList);
                                }
                            }
                        }
                    }
                }
                if (!contains && currentLine[currentLine.length - 1].equals("0")) {
                    //if it has not just been removed
                    if (!removedCellIds.contains(currentLine[0])) {
                        //insert line
                        ArrayList<String> tempList = new ArrayList<>();
                        for (String cell : currentLine) {
                            tempList.add(cell);
                        }
                        tempList.add("insert");
                        realTable.add(tempList);
                    }
                    //check if it was deleted
                }
            }
        }

        //generate changes
        for (int i = 0; i < tableData.size(); i++) {
            String[][] currentTable = tableData.get(i);
            changeQueries.add(new ArrayList<String>());
            for (ArrayList<String> realTableLine : realTable) {
                String id = realTableLine.get(0);
                boolean contains = false;
                for (String[] currentTableLine : currentTable) {
                    if (id.equals(currentTableLine[0])) {
                        contains = true;
                        boolean areEqual = true;
                        //test if they are equal
                        for (int j = 0; j < currentTableLine.length; j++) {
                            if (!currentTableLine[j].equals(realTableLine.get(j))) {
                                areEqual = false;
                            }
                        }
                        if (!areEqual && !realTableLine.get(realTableLine.size() - 1).equals("delete")) {
                            //add update query
                            changeQueries.get(i).add(generateUpdateQuery(realTableLine));
                        } else if ((!areEqual && realTableLine.get(realTableLine.size() - 1).equals("delete"))
                                || realTableLine.get(realTableLine.size() - 2).equals("1")
                                || currentTableLine[currentTableLine.length - 1].equals("1")) {
                            //add delete query
                            changeQueries.get(i).add(generateDeleteQuery(id));
                        }
                    }
                }
                if (!contains && !realTableLine.get(realTableLine.size() - 1).equals("delete")) {
                    //add insert query
                    changeQueries.get(i).add(generateInsertQuery(realTableLine));
                }
            }
        }

        return changeQueries;
    }

    /**
     * Diese Methode erzeugt zu der uebergabe einer Zeile fuer die Datenbank <br>
     * eine passende UPDATE-Query, die diese Zeile in der Datenbank aktualisiert.
     * 
     * @param line die Zeile die eingefuegt werden soll
     * @return die UPDATE-Query als String
     */
    public static String generateUpdateQuery(ArrayList<String> line) {
        return "UPDATE artikel SET version=" + line.get(1) + ", kategorie='"
                + line.get(2) + "', abez='" + line.get(3) + "', abesch='"
                + line.get(4) + "', preis=" + line.get(5) + " WHERE id=" + line.get(0);
    }
    
    /**
     * Diese Methode erzeugt zu der uebergabe einer Zeile fuer die Datenbank <br>
     * eine passende INSERT-Query, die diese Zeile in die Datenbank einfuegt.
     * 
     * @param line die Zeile die eingefuegt werden soll
     * @return die INSERT-Query als String
     */
    public static String generateInsertQuery(ArrayList<String> line) {
        return "INSERT INTO artikel VALUES (" + line.get(0) + ", " + line.get(1)
                + ", '" + line.get(2) + "', '" + line.get(3) + "', '" + line.get(4)
                + "', " + line.get(5) + ", false)";
    }

    /**
     * 
     * Diese Methode erzuegt eine DELETE-Query, die den Eintrag mit der uebergebenen <br>
     * ID auf der Datenbank entfernt.
     * 
     * @param id zu der der Eintrag geloscht werden soll
     * @return die DELETE-Query
     */
    public static String generateDeleteQuery(String id) {
        return "DELETE FROM artikel WHERE id=" + id;
    }
}
