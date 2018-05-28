-- -----------------------------------------------------
-- Table `projects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `projects` ;

CREATE TABLE IF NOT EXISTS `projects` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `organism_id` INT NULL,
  `latest_version` TINYINT(1) NULL,
  `date` TIMESTAMP NULL,
  `version` INT NULL,
  `compartments_tool` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `psort_reports`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psort_reports` ;

CREATE TABLE IF NOT EXISTS `psort_reports` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `locus_tag` VARCHAR(100) NULL,
  `date` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `compartments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compartments` ;

CREATE TABLE IF NOT EXISTS `compartments` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `abbreviation` VARCHAR(10) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `psort_reports_has_compartments`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psort_reports_has_compartments` ;

CREATE TABLE IF NOT EXISTS `psort_reports_has_compartments` (
  `psort_report_id` INT NOT NULL,
  `compartment_id` INT NOT NULL,
  `score` FLOAT NULL,
  PRIMARY KEY (`psort_report_id`, `compartment_id`))
;
