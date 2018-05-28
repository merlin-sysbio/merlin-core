DROP VIEW IF EXISTS `sw_transporters` ;
-- -----------------------------------------------------
-- Table `projects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `projects` ;

CREATE TABLE IF NOT EXISTS `projects` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `organism_id` INT NOT NULL,
  `latest_version` TINYINT(1) NOT NULL,
  `date` TIMESTAMP NOT NULL,
  `version` INT NOT NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `sw_reports`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sw_reports` ;

CREATE TABLE IF NOT EXISTS `sw_reports` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `locus_tag` VARCHAR(100) NOT NULL,
  `date` TIMESTAMP NOT NULL,
  `matrix` VARCHAR(45) NULL,
  `number_TMD` INT(11) NOT NULL,
  `status` VARCHAR(45) NULL,
  PRIMARY KEY (`id`),
  INDEX `locus_tag` (`locus_tag` ASC));


-- -----------------------------------------------------
-- Table `sw_hits`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sw_hits` ;

CREATE TABLE IF NOT EXISTS `sw_hits` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `acc` VARCHAR(45) NOT NULL,
  `tcdb_id` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`));


-- -----------------------------------------------------
-- Table `sw_similarities`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sw_similarities` ;

CREATE TABLE IF NOT EXISTS `sw_similarities` (
  `sw_report_id` INT NOT NULL,
  `sw_hit_id` INT NOT NULL,
  `similarity` FLOAT NULL,
  PRIMARY KEY (`sw_report_id`, `sw_hit_id`));


-- -----------------------------------------------------
-- View `sw_transporters`
-- -----------------------------------------------------
DROP VIEW IF EXISTS `sw_transporters` ;
CREATE  OR REPLACE VIEW sw_transporters AS SELECT locus_tag, acc, tcdb_id, similarity, project_id FROM sw_reports
INNER JOIN sw_similarities ON sw_reports.id = sw_similarities.sw_report_id
INNER JOIN sw_hits ON sw_hits.id = sw_similarities.sw_hit_id;
