package dbsync;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Diese Klasse liest das benoetigte Config-File aus und speichert die MySQL- <br>
 * Konfigurationen fuer jeden einzelnen Client in ein spüezielles Config-Objekt <br>
 * das spaeter verarbeitet werden soll.
 * 
 * @author Gabriel Pawlowsky
 * @version 1.0
 */
public class ConfigLoader {
    // der Inhalt der Config Datei
    private ArrayList<String> inhalt;
    // die einzelnen Host-spezifischen MySQL-Configs
    private ArrayList<Config> configs;
    //Der logger aus Log4j
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ConfigLoader.class);

    /**
     * Konsturktor der das uebergebene File einliest und speichert, sodass es <br>
     * spaeter bearbeitet werden kann.
     * 
     * @param file Name des betroffenen Files
     */
    public ConfigLoader(String file) {
        this.configs = new ArrayList<>();
        try {
            inhalt = (ArrayList<String>) Files.readAllLines(Paths.get(file), Charset.defaultCharset());
        } catch (IOException ex) {
            logger.warn("Config-File not avaibale or not accessible.");
        }
        interpret();
    }

    /**
     * Diese Methode interpretiert den Inhalt der uebergebenen Datei und <br>
     * speichert alle uebergebenen Informationen in  host-spezifische Config-Objekte.
     */
    private void interpret() {
        for (String line : inhalt) {
            String[] splitLine = line.split(" ");
            Config c = new Config();

            c.setMyAddress(splitLine[0]);
            c.setMyUser(splitLine[1]);
            c.setMyPassword(splitLine[2]);
            c.setMyDatabase(splitLine[3]);
            this.configs.add(c);
        }
    }

    /**
     * Getter-Methode
     * 
     * @return die Liste aller eingelenenen Configs 
     */
    public ArrayList<Config> getConfigs() {
        return configs;
    }

    /**
     * Setter-Methode
     * 
     * @param configs alle eingelesenen Configs
     */
    public void setConfigs(ArrayList<Config> configs) {
        this.configs = configs;
    }
    
}
