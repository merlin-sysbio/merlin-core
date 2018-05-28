
-- -----------------------------------------------------
-- Drop tables
-- -----------------------------------------------------
DROP TABLE IF EXISTS `interpro_result_has_model` ;
DROP TABLE IF EXISTS `interpro_result_has_entry` ;
DROP TABLE IF EXISTS `interpro_location` ;
DROP TABLE IF EXISTS `interpro_model` ;
DROP TABLE IF EXISTS `interpro_xRef` ;
DROP TABLE IF EXISTS `interpro_entry` ;
DROP TABLE IF EXISTS `interpro_result` ;
DROP TABLE IF EXISTS `interpro_results` ;

-- -----------------------------------------------------
-- Table `interpro_results`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_results` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `query` VARCHAR(45) NOT NULL,
  `querySequence` LONGTEXT NOT NULL,
  `mostLikelyEc` VARCHAR(45) NULL,
  `mostLikelyLocalization` VARCHAR(45) NULL,
  `mostLikelyName` VARCHAR(250) NULL,
  `status` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
;

-- -----------------------------------------------------
-- Table `interpro_entry`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_entry` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `accession` VARCHAR(45) NOT NULL,
  `description` VARCHAR(250) NOT NULL,
  `name` VARCHAR(250) NOT NULL,
  `type` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `interpro_xRef`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_xRef` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `category` VARCHAR(45) NULL,
  `database` VARCHAR(45) NOT NULL,
  `name` VARCHAR(250) NOT NULL,
  `external_id` VARCHAR(45) NULL,
  `entry_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_xRef_entry1_idx` (`entry_id` ASC),
  CONSTRAINT `fk_xRef_entry1`
    FOREIGN KEY (`entry_id`)
    REFERENCES `interpro_entry` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;


-- -----------------------------------------------------
-- Table `interpro_result`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_result` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `tool` VARCHAR(25) NOT NULL,
  `eValue` FLOAT NULL,
  `score` FLOAT NULL,
  `familyName` VARCHAR(250) NULL,
  `accession` VARCHAR(45) NULL,
  `name` VARCHAR(250) NULL,
  `ec` VARCHAR(45) NULL,
  `goName` VARCHAR(250) NULL,
  `localization` VARCHAR(45) NULL,
  `database` VARCHAR(45) NULL,
  `results_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_result_results_idx` (`results_id` ASC),
  CONSTRAINT `fk_result_results`
    FOREIGN KEY (`results_id`)
    REFERENCES `interpro_results` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;


-- -----------------------------------------------------
-- Table `interpro_location`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_location` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `start` INT NOT NULL,
  `end` INT NOT NULL,
  `score` FLOAT NULL,
  `hmmStart` INT NULL,
  `hmmEnd` INT NULL,
  `eValue` FLOAT NULL,
  `envStart` INT NULL,
  `envEnd` INT NULL,
  `hmmLength` INT NULL,
  `result_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_location_result1_idx` (`result_id` ASC),
  CONSTRAINT `fk_location_result1`
    FOREIGN KEY (`result_id`)
    REFERENCES `interpro_result` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;


-- -----------------------------------------------------
-- Table `interpro_model`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_model` (
  `accession` VARCHAR(45) NOT NULL,
  `description` VARCHAR(250) NULL,
  `name` VARCHAR(250) NULL,
  PRIMARY KEY (`accession`))
;


-- -----------------------------------------------------
-- Table `interpro_result_has_model`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_result_has_model` (
  `result_id` INT NOT NULL,
  `model_accession` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`result_id`, `model_accession`),
  INDEX `fk_result_has_model_model1_idx` (`model_accession` ASC),
  INDEX `fk_result_has_model_result1_idx` (`result_id` ASC),
  CONSTRAINT `fk_result_has_model_result1`
    FOREIGN KEY (`result_id`)
    REFERENCES `interpro_result` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_result_has_model_model1`
    FOREIGN KEY (`model_accession`)
    REFERENCES `interpro_model` (`accession`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;


-- -----------------------------------------------------
-- Table `interpro_result_has_entry`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `interpro_result_has_entry` (
  `result_id` INT NOT NULL,
  `entry_id` INT NOT NULL,
  PRIMARY KEY (`result_id`, `entry_id`),
  INDEX `fk_result_has_entry_entry1_idx` (`entry_id` ASC),
  INDEX `fk_result_has_entry_result1_idx` (`result_id` ASC),
  CONSTRAINT `fk_result_has_entry_result1`
    FOREIGN KEY (`result_id`)
    REFERENCES `interpro_result` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_result_has_entry_entry1`
    FOREIGN KEY (`entry_id`)
    REFERENCES `interpro_entry` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
;

