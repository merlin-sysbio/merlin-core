DROP VIEW IF EXISTS `gene_to_metabolite_direction` ;
-- -----------------------------------------------------
-- Table `metabolites`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `metabolites` ;

CREATE TABLE IF NOT EXISTS `metabolites` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(500) NOT NULL,
  `kegg_miriam` VARCHAR(100) NULL,
  `kegg_name` VARCHAR(500) NULL,
  `chebi_miriam` VARCHAR(100) NULL,
  `chebi_name` VARCHAR(500) NULL,
  `datatype` VARCHAR(45) NOT NULL,
  `kegg_formula` VARCHAR(45) NULL,
  `chebi_formula` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `taxonomy_data`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `taxonomy_data` ;

CREATE TABLE IF NOT EXISTS `taxonomy_data` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `organism` TEXT NULL,
  `taxonomy` TEXT NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `general_equation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `general_equation` ;

CREATE TABLE IF NOT EXISTS `general_equation` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `equation` VARCHAR(450) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `tc_numbers`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tc_numbers` ;

CREATE TABLE IF NOT EXISTS `tc_numbers` (
  `tc_number` VARCHAR(45) NOT NULL,
  `tc_version` INT NOT NULL,
  `taxonomy_data_id` INT NOT NULL,
  `tc_family` VARCHAR(45) NOT NULL,
  `tc_location` VARCHAR(45) NULL,
  `affinity` VARCHAR(45) NULL,
  `general_equation_id` INT NOT NULL,
  PRIMARY KEY (`tc_number`, `tc_version`))
;


-- -----------------------------------------------------
-- Table `transport_types`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transport_types` ;

CREATE TABLE IF NOT EXISTS `transport_types` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  `directions` VARCHAR(45) NULL,
  `reversible` TINYINT(1) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `transport_systems`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transport_systems` ;

CREATE TABLE IF NOT EXISTS `transport_systems` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `transport_type_id` INT NOT NULL,
  `reversible` TINYINT(1) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `directions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `directions` ;

CREATE TABLE IF NOT EXISTS `directions` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `direction` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `transported_metabolites_directions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transported_metabolites_directions` ;

CREATE TABLE IF NOT EXISTS `transported_metabolites_directions` (
  `metabolite_id` INT NOT NULL,
  `direction_id` INT NOT NULL,
  `transport_system_id` INT NOT NULL,
  `stoichiometry` INT NOT NULL,
  PRIMARY KEY (`metabolite_id`, `direction_id`, `transport_system_id`))
;


-- -----------------------------------------------------
-- Table `synonyms`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `synonyms` ;

CREATE TABLE IF NOT EXISTS `synonyms` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `metabolite_id` INT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `datatype` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `genes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes` ;

CREATE TABLE IF NOT EXISTS `genes` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `project_id` INT NOT NULL,
  `locus_tag` VARCHAR(45) NOT NULL,
  `status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `genes_has_metabolites`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes_has_metabolites` ;

CREATE TABLE IF NOT EXISTS `genes_has_metabolites` (
  `gene_id` INT NOT NULL,
  `metabolite_id` INT NOT NULL,
  `similarity_score_sum` FLOAT NOT NULL,
  `taxonomy_score_sum` FLOAT NOT NULL,
  `frequency` INT NOT NULL,
  PRIMARY KEY (`gene_id`, `metabolite_id`))
;


-- -----------------------------------------------------
-- Table `tcdb_registries`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tcdb_registries` ;

CREATE TABLE IF NOT EXISTS `tcdb_registries` (
  `uniprot_id` VARCHAR(45) NOT NULL,
  `version` INT NOT NULL,
  `tc_number` VARCHAR(45) NOT NULL,
  `tc_version` INT NOT NULL,
  `loaded_at` TIMESTAMP NOT NULL,
  `latest_version` TINYINT(1) NOT NULL,
  `status` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`uniprot_id`, `version`))
;


-- -----------------------------------------------------
-- Table `genes_has_tcdb_registries`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes_has_tcdb_registries` ;

CREATE TABLE IF NOT EXISTS `genes_has_tcdb_registries` (
  `gene_id` INT NOT NULL,
  `similarity` FLOAT NOT NULL,
  `uniprot_id` VARCHAR(45) NOT NULL,
  `version` INT NOT NULL,
  PRIMARY KEY (`gene_id`, `uniprot_id`, `version`))
;


-- -----------------------------------------------------
-- Table `genes_has_metabolites_has_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `genes_has_metabolites_has_type` ;

CREATE TABLE IF NOT EXISTS `genes_has_metabolites_has_type` (
  `gene_id` INT NOT NULL,
  `metabolite_id` INT NOT NULL,
  `transport_type_id` INT NOT NULL,
  `transport_type_score_sum` FLOAT NOT NULL,
  `taxonomy_score_sum` FLOAT NOT NULL,
  `frequency` INT NOT NULL,
  PRIMARY KEY (`gene_id`, `transport_type_id`, `metabolite_id`))
;


-- -----------------------------------------------------
-- Table `tc_numbers_has_transport_systems`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `tc_numbers_has_transport_systems` ;

CREATE TABLE IF NOT EXISTS `tc_numbers_has_transport_systems` (
  `transport_system_id` INT NOT NULL,
  `tc_number` VARCHAR(45) NOT NULL,
  `tc_version` INT NOT NULL,
  PRIMARY KEY (`transport_system_id`, `tc_number`, `tc_version`))
;


-- -----------------------------------------------------
-- Table `metabolites_ontology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `metabolites_ontology` ;

CREATE TABLE IF NOT EXISTS `metabolites_ontology` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `metabolite_id` INT NOT NULL,
  `child_id` INT NOT NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- View `gene_to_metabolite_direction`
-- -----------------------------------------------------
DROP VIEW IF EXISTS `gene_to_metabolite_direction` ;
CREATE  OR REPLACE VIEW gene_to_metabolite_direction AS 
SELECT g.id as gene_id,
g.locus_tag,
tc.tc_family as tc_family,
tt.name as transport_type,
ts.id as transport_reaction_id,
m.id as metabolite_id,
m.name as metabolite_name,
m.kegg_name  as metabolite_kegg_name,
tmd.stoichiometry as stoichiometry,
m.kegg_miriam,
d.direction,
ts.reversible,
m.chebi_miriam AS chebi_miriam,
tc.tc_number as tc_number,
tr.uniprot_id as uniprot_id,
ghtr.similarity as similarity,
taxonomy_data_id,
project_id
FROM genes_has_tcdb_registries as ghtr
INNER JOIN genes as g on g.id = ghtr.gene_id
INNER JOIN tcdb_registries as tr on ghtr.uniprot_id=tr.uniprot_id AND ghtr.version=tr.version
INNER JOIN tc_numbers as tc on tr.tc_number=tc.tc_number AND tc.tc_version=tr.tc_version
INNER JOIN tc_numbers_has_transport_systems as tc_ts on  tc.tc_number = tc_ts.tc_number  AND tc.tc_version=tc_ts.tc_version
INNER JOIN transport_systems as ts on  tc_ts.transport_system_id= ts.id
INNER JOIN transport_types as tt on ts.transport_type_id = tt.id
INNER JOIN transported_metabolites_directions as tmd on ts.id = tmd.transport_system_id
INNER JOIN metabolites as m on m.id=tmd.metabolite_id
INNER JOIN directions as d on d.id=tmd.direction_id;
