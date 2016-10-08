package filesync.replicator;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import filesync.exception.GetFileListException;
import filesync.exception.InvalidOutputException;


public class FileReplicator {

	private HashMap<String,Consumer> consumers;
	private ArrayList<String> addList;
	private ArrayList<String> deleteList;
	private String localDir;
	
	private static Logger logger = Logger.getLogger(FileReplicator.class);
	
	public FileReplicator(String localDir) {
		this.consumers = new HashMap<String,Consumer>();
		this.addList = new ArrayList<String>();
		this.deleteList = new ArrayList<String>();
		this.localDir = localDir;
	}
	
	public void addConsumer(Consumer c) {
		consumers.put(c.getAddress(), c);
		logger.info("New Consumer: "+c.getAddress());
	}
	
	public void removeConsumer(String address) {
		consumers.remove(address);
		logger.info("Removed Consumer: "+address);
	}
	
	public void startReplication() {
		logger.info("\n[---REPLICATION STARTED ON "+new java.util.Date()+"---]");
		//Consumer, bei denen ein Fehler aufgetreten ist werden hier
		//eingetragen, damit sie dann geloescht werden koennen.
		ArrayList<String> failedConsumers = new ArrayList<String>();
		
		logger.info("\n--------------MERGING DATA--------------");
		//Es wird durch die Consumer HashMap durchiteriert:
		for (Entry<String,Consumer> entry: consumers.entrySet()) {
			String outc = "";

			try {
				//Ausgabe: Welcher Consumer in welchem Directory geprueft wird
				outc += "\nChecking Files from Consumer "+entry.getKey()+":"+entry.getValue().getDirectory()+"\n";

				//Dateilisten von Provider und Consumer werden geladen
				String localFileString = this.getProviderFileList(localDir);
				String remoteFileString = this.getConsumerFileList(entry.getValue());
				
				FileData[] localFiles = this.convertLinuxLsOutput(localFileString);
				FileData[] remoteFiles;
				
				//if(entry.getValue().getOperatingSystem().contains("Linux")) {
					remoteFiles = this.convertLinuxLsOutput(remoteFileString);
				//} else {
				//	remoteFiles = this.convertWindowsDirOutput(remoteFileString);
				//}
				
				//Die erhaltenen Dateien werden angezeigt
				outc += "  PROVIDER\n";
				for(FileData lf: localFiles) {
					outc += "    |"+lf.getSize()+"  \t|"+lf.getCtime()+"| "+lf.getName()+"\n";
				}
				outc += "  CONSUMER\n";
				for(FileData lf: remoteFiles) {
					outc += "    |"+lf.getSize()+"  \t|"+lf.getCtime()+"| "+lf.getName()+"\n";
				}
				
				//Die Dateien werden nun abgeglichen
				if(remoteFiles.length>0) {

					/*********************************************
					 * Consumer Dateien auf Aenderungen
					 * und neue Dateien pruefen
					 ********************************************/
					for (FileData rd: remoteFiles) {
						boolean exists = false;
						for (FileData ld: localFiles) {
							//Hat der Provider die Datei bereits?
							if(rd.getName().equals(ld.getName())) {
								exists = true;
								//Ersetzt durch -u Parameter bei rsync
								//Ja, er hat sie -> Sync Liste
								/*
								if(this.getUnixTime(rd.getCtime())>this.getUnixTime(ld.getCtime())) {
									//nur, wenn es nicht schon geloescht wurde...
									if(!deleteList.contains(rd.getName())) {
										syncList.add(rd.getName());
									}
								}*/
							}
						}
						//Wenn sie noch nicht beim Provider existiert -> Sync+Addlist
						if(!exists) {
							if(!deleteList.contains(rd.getName())) {
								//syncList.add(rd.getName());
								addList.add(rd.getName());
							}
						}
					}
					
					/*********************************************
					 * Providerdaten auf Zustand pruefen
					 * (wurden sie schon geloescht vom Consumer?)
					 ********************************************/
					for (FileData ld: localFiles) {
						boolean exists = false;
						for (FileData rd: remoteFiles) {
							if(rd.getName().equals(ld.getName())) {
								exists = true;
							}
						}
						if (!exists) {
							deleteList.add(ld.getName());
						}
					}
					
					//Aenderungen vom Consumer holen
					outc += this.pullFromConsumer(entry.getValue());
					
				} else {
					//Vermutlich ist es unerwuenscht, dass ein leerer Ordner
					//bedeutet, dass ALLE Dateien geloescht werden muessen.
					outc += "    Warning: No files found. Skipping pull.";
				}
				
			} catch (GetFileListException e1) {
				outc += "Error while receiving List from "+entry.getKey();
				failedConsumers.add(entry.getKey());
			} catch (InvalidOutputException e2) {
				outc += "Unexpected answer from "+entry.getKey();
				failedConsumers.add(entry.getKey());
			} finally {
				//Vollstaendige Ausgabe (-> mehr Perfomance)
				logger.info(outc);
			}
			
		}
		
		//Entferne alle Consumer, bei denen ein Fehler aufgetreten ist
		//ACHTUNG: sofortiges entfernen nicht moeglich -> Fehler in der for Schleife
		for(String c: failedConsumers) {
			removeConsumer(c);
		}
		
		this.deleteMarkedData();
		this.pushToEveryone();
		logger.info("\n[---REPLICATION FINISHED ON "+new java.util.Date()+"---]");
	}
	
	private void deleteMarkedData() {
		for(String d: deleteList) {
			if(!addList.contains(d)) {
				File f = new File(localDir+d);
				f.delete();
				logger.info("Deleted "+d);
			}
		}
	}

	/**
	 * Holt sich die Daten von den Consumern
	 * @param syncList 
	 * @param consumer 
	 */
	public String pullFromConsumer(Consumer c) { 
		String command = "sh pull.sh "+c.getAddress()+" "+c.getUsername()+" "+c.getPassword()+" "+c.getDirectory()+" "+localDir;
		String output = "";
		try {
			String str;
			Process p = Runtime.getRuntime().exec(command);
		    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((str = in.readLine()) != null) {
            	output += str+"\n";
            }
            in.close();
		} catch (IOException e) {
			//e.printStackTrace();
			logger.warn("Error: pull.sh not found!");
		}

		return "Pulling: \n"+output;
	}
	
	public void pushToEveryone() {
		logger.info("\n--------------DISTRIBUTION--------------");
		for (Entry<String,Consumer> entry: consumers.entrySet()) {
			Consumer c = entry.getValue();
			String command = "sh push.sh "+c.getAddress()+" "+c.getUsername()+" "+c.getPassword()+" "+c.getDirectory()+" "+localDir;
			
			String output = "";
			output += "\nSending to: "+entry.getKey()+":"+c.getDirectory()+"\n";
					
			try {
				String str;
				Process p = Runtime.getRuntime().exec(command);
			    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
	            while ((str = in.readLine()) != null) {
	            	output += str+"\n";
	            }
	            in.close();
			} catch (IOException e) {
				logger.warn("Error: Could not push to "+c.getAddress());
			}
			
			logger.info(output);
		}
	}
	
	/**
	 * Gibt die Dateiliste eines Consumers zurueck.
	 * @param c Consumer
	 * @return Dateiliste
	 */
	private String getConsumerFileList(Consumer c) throws GetFileListException {
		//Kommentare siehe "getProviderFileList(String dir)"
		//Anmerkung: Es wird ein Shellscript ausgefuehrt, da es sonst nicht funktioniert.
		String command = "sh remls.sh "+c.getAddress()+" "+c.getUsername()+" "+c.getPassword()+" "+c.getDirectory();
		
		String output = "";
		try {
			String str;
			Process p = Runtime.getRuntime().exec(command);
		    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((str = in.readLine()) != null) {
            	output += str+"\n";
            }
            in.close();
		} catch (IOException e) {
			//e.printStackTrace();
			throw new GetFileListException();
		}
		//Kein Inhalt = Uebertragung oder Kommando fehlgeschlagen
		//mindestens die 1. Zeile MUSS existieren ("total"/"insgesamt")
		if(output.equals("")) {
			throw new GetFileListException();
		}
		return output;
	}
	
	/**
	 * Gibt die Dateiliste des Providers (lokaler Rechner) zurueck.
	 * @param dir Verzeichnis
	 * @return Dateiliste
	 */
	private String getProviderFileList(String dir) throws GetFileListException {
		//Befehl, der ausgefuehrt werden soll
		String command = "ls --time-style=long-iso -l "+dir;
		//Hier wird die Liste als roher String gespeichert
		String output = "";
		try {
			String str;
			//Programm wird ausgefuehrt
			Process p = Runtime.getRuntime().exec(command);
			//Output Stream des Programms (ls) wird ausgelesen
		    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((str = in.readLine()) != null) {
            	//und Zeile fuer Zeile in die Datei geschrieben
            	output += str+"\n";
            }
            in.close();
		} catch (IOException e) {
			//e.printStackTrace();
			throw new GetFileListException();
		}
		//Kein Inhalt = Kommando fehlgeschlagen
		//mindestens die 1. Zeile muss existieren
		if(output.equals("")) {
			throw new GetFileListException();
		}
		return output;
	}
	
	/**
	 * Wandelt die Ausgabe des "ls -l" Kommandos unter Linux
	 * in eine FileData Array um, damit die Informationen geordnet
	 * gelesen werden koennen.
	 * @param lsOutput Ausgabe des ls Kommandos
	 * @return FileData Array
	 */
	private FileData[] convertLinuxLsOutput(String lsOutput) throws InvalidOutputException {
		//In Zeilen aufteilen:
		String[] lsOutLines = lsOutput.split("\n");
		//Array fuer Rueckgabe definieren:
		FileData[] fd = new FileData[lsOutLines.length-1];
		
		//Alle Zeilen ab i=1 durchgehen und in Worte "brechen".
		//Dateiname, Dateigroesse und Aenderungsdatum werden rausgefiltert
		//um neue FileData Objekte zu erstellen.
		for(int i=1; i<lsOutLines.length; i++) {
			String[] words = lsOutLines[i].split("\\s+");
			
			//fuer leerzeichen...
			if(words.length>8) {
				for(int n=8; n<words.length; n++) {
					words[7] += " "+words[i];
				}
			}
			
			fd[i-1] = new FileData(words[7], words[4], words[5]+" "+words[6]);
		}
		return fd;
	}
	
	/*
	 * VERWORFEN
	 * 
	 * Wandelt die Ausgabe des "dir" Kommandos unter Windows
	 * in eine FileData Array um, damit die Informationen geordnet
	 * gelesen werden koennen.
	 * @param lsOutput Ausgabe des ls Kommandos
	 * @return FileData Array
	 *
	private FileData[] convertWindowsDirOutput(String dirOutput) throws InvalidOutputException {
		//In Zeilen aufteilen:
		String[] dirOutLines = dirOutput.split("\n");
		//Array fuer Rueckgabe definieren:
		FileData[] fd = new FileData[dirOutLines.length-2];
		
		//Alle Zeilen ab i=1 durchgehen und in Worte "brechen".
		//Dateiname, Dateigroesse und Aenderungsdatum werden rausgefiltert
		//um neue FileData Objekte zu erstellen.
		for(int i=5; i<dirOutLines.length-2; i++) {
			String[] words = dirOutLines[i].split("\\s+");
			logger.info(dirOutLines[i]+i);
			logger.info(words[3]+":"+ words[2]+":"+ words[0]+" "+words[1]);
			fd[i-1] = new FileData(words[3], words[2], words[0]+" "+words[1]);
		}
		return fd;
	}*/
	
	/**
	 * Transforms YYYY-MM-dd HH:mm" into Seconds.
	 * @param timestamp
	 * @return
	 */
	private long getUnixTime(String timestamp) {
		timestamp += ":00"; //fuer ein gueltiges Format notwendig
		return Timestamp.valueOf(timestamp).getTime();
	}
}
