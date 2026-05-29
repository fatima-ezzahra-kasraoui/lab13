-- Créer la base de données
CREATE DATABASE IF NOT EXISTS map_project CHARACTER SET utf8 COLLATE utf8_general_ci;
USE map_project;

-- Créer la table positions
CREATE TABLE IF NOT EXISTS `positions` (
  `id`        INT(11)      NOT NULL AUTO_INCREMENT,
  `latitude`  DOUBLE       NOT NULL,
  `longitude` DOUBLE       NOT NULL,
  `date`      DATETIME     NOT NULL,
  `imei`      VARCHAR(50)  NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
