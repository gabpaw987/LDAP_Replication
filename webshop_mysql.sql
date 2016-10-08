DROP DATABASE IF EXISTS vsdb_webshop;
CREATE DATABASE vsdb_webshop;
USE vsdb_webshop;

DROP TABLE IF EXISTS artikel;
CREATE TABLE artikel (
id INT,
version INT,
kategorie VARCHAR(255),
abez VARCHAR(255),
abesch VARCHAR(255),
preis DECIMAL(10,2),
deleted BOOLEAN,
PRIMARY KEY (id)
)ENGINE = INNODB;

INSERT INTO artikel VALUES (1,1,"Getraenk","Red Bull","Erfrischungsgetraenk fuer Idioten",1.29,false);
INSERT INTO artikel VALUES (2,1,"Nahrungsmittel","Felix Ketchup","Gut!!!",0.99,false);
INSERT INTO artikel VALUES (3,1,"Nahrungsmittel","Brot","Brot ist gesund.",0.39,false);
INSERT INTO artikel VALUES (4,1,"Nahrungsmittel","Kuchen","Wer kein Brot hat, soll Kuchen essen.",0.79,false);
INSERT INTO artikel VALUES (5,1,"Hygieneartikel","Zahnpasta","Macht Zaehne sauber.",1.99,false);
INSERT INTO artikel VALUES (6,1,"Hygieneartikel","Shampoo","Macht Haare sauber.",0.99,false);
INSERT INTO artikel VALUES (7,1,"Freizeitartikel","Fahrrad","Kann man besteigen.",999.99,false);
INSERT INTO artikel VALUES (8,1,"Bueromaterial","Papier 500Stk.","Zum Drucken von Ausarbeitungen.",5.99,false);
INSERT INTO artikel VALUES (9,1,"Getraenk","Milch","Pferdemilch?",1.99,false);
INSERT INTO artikel VALUES (10,1,"Hygieneartikel","Parfum","Bitte nicht.",0.99,false);