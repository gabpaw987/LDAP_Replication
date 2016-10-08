package main;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;

import dbsync.Config;
import dbsync.ConfigLoader;
import dbsync.DataBaseConnection;
import dbsync.DatabaseHandler;
import filesync.replicator.Consumer;
import filesync.replicator.FileReplicator;

public class Start {

	// Die Daten aller Datenbanken
	private static ArrayList<String[][]> fullDatas;
	// Die Verbindungen zu allen Datenbanken.
	private static ArrayList<DataBaseConnection> connections;
	// Der Logger aus Log4j
	private static Logger logger = Logger.getLogger(Start.class);

	/**
	 * Die main-Methode. Sie uebernimmt alle im Klassenkommentar beschriebenenen <br>
	 * Aufgaben.
	 * 
	 * @param args
	 *            nichts
	 */
	public static void main(String[] args) {
		startDBSync();
		startFileSync();
	}

	/**
	 * Diese Methode ist quasi die main-Methode der DB-Synchronisation. <br>
	 * Es werden zuerst alle Configs ausgelesen. Danach wird fuer jeden<br>
	 * Host eine Datenbankverbidnung aufgebaut. Dann werden alle Daten aus allen
	 * Datenbanken<br>
	 * ausgelsen und gespeichert. Diese werden dem DatabaseHandler fuer das
	 * merging ubergben<br>
	 * der daraufhin eine Liste mit Queries zum einfuegen in alle Datenbanken
	 * zurueck gibt. <br>
	 * Diese Queries werden dann jeweils in die passenden Datenbanken eingefuegt
	 * und schon<br>
	 * sind alle Datenbanken auf dem selben Stand. Das Logging uebernimmt in
	 * dieser Klasse das Framework Log4j.
	 */
	public static void startDBSync() {
		fullDatas = new ArrayList<>();
		connections = new ArrayList<>();
		boolean failed = false;
		try {
			// Auslesen der Configs
			logger.info("Start reading config.");
			ConfigLoader configLoader = new ConfigLoader("./mysql.config");
			ArrayList<Config> configs = configLoader.getConfigs();
			// Selecten aller Daten aus allen Datenbanken
			logger.info("Start getting Data from Databases.");
			for (Config config : configs) {
				DataBaseConnection reader = new DataBaseConnection(config);
				fullDatas.add(reader.getFullData());
				connections.add(reader);
			}
		} catch (Exception e) {
			logger.warn(e.getMessage());
			failed = true;
		}
		if (!failed) {
			try {
				// Mergen des Tabnellen
				logger.info("Merging Tables");
				ArrayList<ArrayList<String>> queries = DatabaseHandler
						.mergeTables(fullDatas);
				// Einfuegen aller Queries in alle Datenbanken
				for (int i = 0; i < queries.size(); i++) {
					ArrayList<String> tableQueries = queries.get(i);
					boolean wasSuccessful = true;
					for (String query : tableQueries) {
						try {
							connections.get(i).setUpdate(query);
							logger.info(query);
						} catch (SQLException ex) {
							wasSuccessful = false;
							logger.warn("Could not insert query:"
									+ query
									+ "\nTherefore synchronization of the database from "
									+ connections.get(i).getC().getMyAddress()
									+ " failed.");
						}
					}
					if (wasSuccessful) {
						logger.info("The Replication of the database from "
								+ connections.get(i).getC().getMyAddress()
								+ " has been done successfully!");
					}
					connections.get(i).closeConnection();
				}
			} catch (IllegalStateException | SQLException e) {
				logger.warn(e.getMessage());
			}
		}
	}

	public static void startFileSync() {
		FileReplicator fr = new FileReplicator("localFiles/");

		try {
			Scanner fileScanner = new Scanner(new File("consumers.lst"));

			while (fileScanner.hasNextLine()) {
				String[] cinf = fileScanner.nextLine().split(",");
				Consumer c = new Consumer(cinf[0], cinf[1], cinf[2], cinf[3],
						cinf[4]);
				fr.addConsumer(c);
			}

			fr.startReplication();
			fileScanner.close();
		} catch (IOException e) {
			System.out.println("Error: Could not read configuration file.");
		}
	}
}