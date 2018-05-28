
DROP VIEW IF EXISTS `reactions_view_noPath_or_noEC` ;
DROP TABLE IF EXISTS `reactions_view_noPath_or_noEC`;
DROP VIEW IF EXISTS `reactions_view` ;
DROP TABLE IF EXISTS `reactions_view`;

-- -----------------------------------------------------
-- Table `ri_function`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ri_function` ;

CREATE TABLE IF NOT EXISTS `ri_function` (
  `idri_function` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `symbol` CHAR(2) NULL,
  `ri_function` CHAR(20) NULL,
  PRIMARY KEY (`idri_function`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `chromosome`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `chromosome` ;

CREATE TABLE IF NOT EXISTS `chromosome` (
  `idchromosome` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(15) NOT NULL,
  PRIMARY KEY (`idchromosome`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `gene`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gene` ;

CREATE TABLE IF NOT EXISTS `gene` (
  `idgene` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `chromosome_idchromosome` INT UNSIGNED NULL,
  `name` VARCHAR(120) NULL,
  `locusTag` VARCHAR(45) NULL,
  `transcription_direction` CHAR(3) NULL,
  `left_end_position` VARCHAR(45) NULL,
  `right_end_position` VARCHAR(100) NULL,
  `boolean_rule` VARCHAR(200) NULL,
  `origin` VARCHAR(40) NOT NULL,
  `sequence_id` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idgene`),
  INDEX `gene_name` (`name` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sequence`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sequence` ;

CREATE TABLE IF NOT EXISTS `sequence` (
  `idsequence` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `gene_idgene` INT UNSIGNED NOT NULL,
  `sequence_type` VARCHAR(20) NULL,
  `sequence` TEXT NULL,
  `sequence_length` INT UNSIGNED NULL,
  PRIMARY KEY (`idsequence`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway` ;

CREATE TABLE IF NOT EXISTS `pathway` (
  `idpathway` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(5) NOT NULL,
  `name` VARCHAR(120) NOT NULL,
  `path_sbml_file` VARCHAR(200) NULL,
  `image` BLOB NULL,
  PRIMARY KEY (`idpathway`),
  INDEX `name` (`name` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compound` ;

CREATE TABLE IF NOT EXISTS `compound` (
  `idcompound` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(400) NULL,
  `inchi` VARCHAR(1500) NULL,
  `kegg_id` VARCHAR(15) NULL,
  `entry_type` VARCHAR(9) NULL,
  `formula` TEXT NULL,
  `molecular_weight` VARCHAR(100) NULL,
  `neutral_formula` VARCHAR(120) NULL,
  `charge` SMALLINT UNSIGNED NULL,
  `smiles` VARCHAR(1200) NULL,
  `hasBiologicalRoles` TINYINT(1) NULL,
  PRIMARY KEY (`idcompound`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `compartment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `compartment` ;

CREATE TABLE IF NOT EXISTS `compartment` (
  `idcompartment` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(25) NULL,
  `abbreviation` VARCHAR(20) NULL,
  PRIMARY KEY (`idcompartment`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `reaction` ;

CREATE TABLE IF NOT EXISTS `reaction` (
  `idreaction` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(400) NULL,
  `equation` TEXT NULL,
  `reversible` TINYINT(1) NULL,
  `boolean_rule` VARCHAR(400) NULL,
  `inModel` TINYINT(1) NULL,
  `isGeneric` TINYINT(1) NULL,
  `isSpontaneous` TINYINT(1) NULL,
  `isNonEnzymatic` TINYINT(1) NULL,
  `source` VARCHAR(45) NOT NULL,
  `originalReaction` TINYINT(1) NOT NULL,
  `compartment_idcompartment` INT UNSIGNED NULL,
  `notes` TEXT NULL,
  `lowerBound` BIGINT NULL,
  `upperBound` BIGINT NULL,
  PRIMARY KEY (`idreaction`),
  INDEX `reversible` (`reversible` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `stoichiometry`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `stoichiometry` ;

CREATE TABLE IF NOT EXISTS `stoichiometry` (
  `idstoichiometry` INT NOT NULL AUTO_INCREMENT,
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `compartment_idcompartment` INT UNSIGNED NOT NULL,
  `stoichiometric_coefficient` FLOAT NOT NULL,
  `numberofchains` VARCHAR(20) NULL,
  PRIMARY KEY (`idstoichiometry`, `reaction_idreaction`, `compound_idcompound`, `compartment_idcompartment`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `feature`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `feature` ;

CREATE TABLE IF NOT EXISTS `feature` (
  `idfeature` INT UNSIGNED NOT NULL,
  `class` VARCHAR(50) NULL,
  `description` VARCHAR(1300) NULL,
  `start_position` INT UNSIGNED NULL,
  `end_position` INT UNSIGNED NULL,
  PRIMARY KEY (`idfeature`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sequence_feature`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sequence_feature` ;

CREATE TABLE IF NOT EXISTS `sequence_feature` (
  `feature_idfeature` INT UNSIGNED NOT NULL,
  `sequence_idsequence` INT UNSIGNED NOT NULL,
  `start_position_approximate` VARCHAR(20) NULL,
  `end_position_approximate` VARCHAR(20) NULL,
  PRIMARY KEY (`feature_idfeature`),
  INDEX `sequence_feature_FKIndex1` (`feature_idfeature` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transcription_unit` ;

CREATE TABLE IF NOT EXISTS `transcription_unit` (
  `idtranscription_unit` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NULL,
  PRIMARY KEY (`idtranscription_unit`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit_gene`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transcription_unit_gene` ;

CREATE TABLE IF NOT EXISTS `transcription_unit_gene` (
  `transcription_unit_idtranscription_unit` INT UNSIGNED NOT NULL,
  `gene_idgene` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`transcription_unit_idtranscription_unit`, `gene_idgene`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `protein`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `protein` ;

CREATE TABLE IF NOT EXISTS `protein` (
  `idprotein` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(400) NOT NULL,
  `class` VARCHAR(120) NULL,
  `inchi` VARCHAR(255) NULL,
  `molecular_weight` FLOAT NULL,
  `molecular_weight_exp` FLOAT NULL,
  `molecular_weight_kd` FLOAT NULL,
  `molecular_weight_seq` FLOAT NULL,
  `pi` FLOAT NULL,
  PRIMARY KEY (`idprotein`),
  INDEX `idprotein` (`idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzyme` ;

CREATE TABLE IF NOT EXISTS `enzyme` (
  `ecnumber` VARCHAR(15) NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `inModel` TINYINT(1) NULL,
  `source` VARCHAR(45) NULL,
  `gpr_status` VARCHAR(45) NULL,
  PRIMARY KEY (`ecnumber`, `protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `reaction_has_enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `reaction_has_enzyme` ;

CREATE TABLE IF NOT EXISTS `reaction_has_enzyme` (
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`reaction_idreaction`, `enzyme_ecnumber`, `enzyme_protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `strain`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `strain` ;

CREATE TABLE IF NOT EXISTS `strain` (
  `idstrain` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(60) NULL,
  PRIMARY KEY (`idstrain`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `entityisfrom`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `entityisfrom` ;

CREATE TABLE IF NOT EXISTS `entityisfrom` (
  `strain_idstrain` INT UNSIGNED NOT NULL,
  `wid` INT UNSIGNED NULL)


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzymatic_cofactor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzymatic_cofactor` ;

CREATE TABLE IF NOT EXISTS `enzymatic_cofactor` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `prosthetic` TINYINT(1) NULL)


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `promoter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `promoter` ;

CREATE TABLE IF NOT EXISTS `promoter` (
  `idpromoter` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NULL,
  `absolute_position` DOUBLE NULL,
  PRIMARY KEY (`idpromoter`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `regulatory_event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `regulatory_event` ;

CREATE TABLE IF NOT EXISTS `regulatory_event` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `promoter_idpromoter` INT UNSIGNED NOT NULL,
  `ri_function_idri_function` INT UNSIGNED NOT NULL,
  `binding_site_position` DECIMAL(15,6) NULL,
  PRIMARY KEY (`protein_idprotein`, `promoter_idpromoter`, `ri_function_idri_function`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzymatic_alternative_cofactor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzymatic_alternative_cofactor` ;

CREATE TABLE IF NOT EXISTS `enzymatic_alternative_cofactor` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `original_cofactor` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`compound_idcompound`, `original_cofactor`, `protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `subunit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `subunit` ;

CREATE TABLE IF NOT EXISTS `subunit` (
  `gene_idgene` INT UNSIGNED NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL)


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `protein_composition`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `protein_composition` ;

CREATE TABLE IF NOT EXISTS `protein_composition` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `subunit` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`protein_idprotein`, `subunit`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `functional_parameter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `functional_parameter` ;

CREATE TABLE IF NOT EXISTS `functional_parameter` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `parameter_type` VARCHAR(50) NOT NULL,
  `parameter_value` FLOAT NULL,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`, `parameter_type`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `substrate_affinity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `substrate_affinity` ;

CREATE TABLE IF NOT EXISTS `substrate_affinity` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `substrate_affinity` FLOAT NOT NULL,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway_has_reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_reaction` ;

CREATE TABLE IF NOT EXISTS `pathway_has_reaction` (
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`reaction_idreaction`, `pathway_idpathway`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experimental_factor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experimental_factor` ;

CREATE TABLE IF NOT EXISTS `experimental_factor` (
  `idexperimental_factor` INT UNSIGNED NOT NULL,
  `factor` VARCHAR(255) NULL,
  PRIMARY KEY (`idexperimental_factor`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_description`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_description` ;

CREATE TABLE IF NOT EXISTS `experiment_description` (
  `idexperiment` INT UNSIGNED NOT NULL,
  `experiment_descriptional_factor_idexperimental_factor` INT UNSIGNED NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`idexperiment`, `experiment_descriptional_factor_idexperimental_factor`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_substrate_affinity`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_substrate_affinity` ;

CREATE TABLE IF NOT EXISTS `experiment_substrate_affinity` (
  `experiment_description` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `substrate_affinity` FLOAT NULL,
  PRIMARY KEY (`experiment_description`, `compound_idcompound`, `protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_inhibitor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_inhibitor` ;

CREATE TABLE IF NOT EXISTS `experiment_inhibitor` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `experiment_description` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `ki` FLOAT NULL,
  PRIMARY KEY (`compound_idcompound`, `experiment_description`, `protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_turnover_number`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `experiment_turnover_number` ;

CREATE TABLE IF NOT EXISTS `experiment_turnover_number` (
  `experiment_description` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `turnover_number` FLOAT NULL,
  PRIMARY KEY (`experiment_description`, `compound_idcompound`, `protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit_promoter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `transcription_unit_promoter` ;

CREATE TABLE IF NOT EXISTS `transcription_unit_promoter` (
  `promoter_idpromoter` INT UNSIGNED NOT NULL,
  `transcription_unit_idtranscription_unit` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`promoter_idpromoter`, `transcription_unit_idtranscription_unit`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sigma_promoter`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `sigma_promoter` ;

CREATE TABLE IF NOT EXISTS `sigma_promoter` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `promoter_idpromoter` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`protein_idprotein`, `promoter_idpromoter`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `metabolic_regulation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `metabolic_regulation` ;

CREATE TABLE IF NOT EXISTS `metabolic_regulation` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `mode` CHAR(1) NULL,
  `mechanism` VARCHAR(25) NULL,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `activating_reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `activating_reaction` ;

CREATE TABLE IF NOT EXISTS `activating_reaction` (
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`reaction_idreaction`, `enzyme_ecnumber`, `enzyme_protein_idprotein`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `dictionary`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dictionary` ;

CREATE TABLE IF NOT EXISTS `dictionary` (
  `class` VARCHAR(1) NOT NULL,
  `aliases` VARCHAR(250) NOT NULL,
  `common_name` VARCHAR(250) NULL,
  PRIMARY KEY (`class`, `aliases`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `aliases`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `aliases` ;

CREATE TABLE IF NOT EXISTS `aliases` (
  `idalias` INT NOT NULL AUTO_INCREMENT,
  `class` VARCHAR(2) NOT NULL,
  `entity` INT UNSIGNED NOT NULL,
  `alias` VARCHAR(1200) NOT NULL,
  PRIMARY KEY (`idalias`, `class`, `entity`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `dblinks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dblinks` ;

CREATE TABLE IF NOT EXISTS `dblinks` (
  `class` VARCHAR(2) NOT NULL,
  `internal_id` INT UNSIGNED NOT NULL,
  `external_database` VARCHAR(150) NOT NULL,
  `external_id` TEXT NOT NULL,
  PRIMARY KEY (`class`, `internal_id`, `external_database`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `effector`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `effector` ;

CREATE TABLE IF NOT EXISTS `effector` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`protein_idprotein`, `compound_idcompound`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway_has_compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_compound` ;

CREATE TABLE IF NOT EXISTS `pathway_has_compound` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `compound_idcompound`))
;


-- -----------------------------------------------------
-- Table `pathway_has_enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_enzyme` ;

CREATE TABLE IF NOT EXISTS `pathway_has_enzyme` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `enzyme_ecnumber`, `enzyme_protein_idprotein`))
;


-- -----------------------------------------------------
-- Table `gene_has_compartment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gene_has_compartment` ;

CREATE TABLE IF NOT EXISTS `gene_has_compartment` (
  `gene_idgene` INT UNSIGNED NOT NULL,
  `compartment_idcompartment` INT UNSIGNED NOT NULL,
  `primaryLocation` TINYINT(1) NULL,
  `score` VARCHAR(20) NULL,
  PRIMARY KEY (`gene_idgene`, `compartment_idcompartment`))
;


-- -----------------------------------------------------
-- Table `module`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `module` ;

CREATE TABLE IF NOT EXISTS `module` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `reaction` TEXT NOT NULL,
  `entry_id` VARCHAR(6) NOT NULL,
  `stoichiometry` VARCHAR(45) NULL,
  `name` VARCHAR(200) NULL,
  `definition` TEXT NOT NULL,
  `hieralchical_class` TEXT NULL,
  `type` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `modules_has_compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `modules_has_compound` ;

CREATE TABLE IF NOT EXISTS `modules_has_compound` (
  `modules_id` INT NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`modules_id`, `compound_idcompound`))
;


-- -----------------------------------------------------
-- Table `orthology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `orthology` ;

CREATE TABLE IF NOT EXISTS `orthology` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `entry_id` VARCHAR(20) NOT NULL,
  `locus_id` VARCHAR(45) NULL,
  PRIMARY KEY (`id`))
;


-- -----------------------------------------------------
-- Table `module_has_orthology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `module_has_orthology` ;

CREATE TABLE IF NOT EXISTS `module_has_orthology` (
  `module_id` INT NOT NULL,
  `orthology_id` INT NOT NULL,
  PRIMARY KEY (`module_id`, `orthology_id`))
;


-- -----------------------------------------------------
-- Table `pathway_has_module`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_module` ;

CREATE TABLE IF NOT EXISTS `pathway_has_module` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `module_id` INT NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `module_id`))
;


-- -----------------------------------------------------
-- Table `gene_has_orthology`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `gene_has_orthology` ;

CREATE TABLE IF NOT EXISTS `gene_has_orthology` (
  `gene_idgene` INT UNSIGNED NOT NULL,
  `orthology_id` INT NOT NULL,
  `similarity` FLOAT NULL,
  PRIMARY KEY (`gene_idgene`, `orthology_id`))
;


-- -----------------------------------------------------
-- Table `same_as`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `same_as` ;

CREATE TABLE IF NOT EXISTS `same_as` (
  `metabolite_id` INT UNSIGNED NOT NULL,
  `similar_metabolite_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`metabolite_id`, `similar_metabolite_id`))
;


-- -----------------------------------------------------
-- Table `is_super_reaction`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `is_super_reaction` ;

CREATE TABLE IF NOT EXISTS `is_super_reaction` (
  `super_reaction_id` INT UNSIGNED NOT NULL,
  `sub_reaction_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`super_reaction_id`, `sub_reaction_id`))
;


-- -----------------------------------------------------
-- Table `superpathway`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `superpathway` ;

CREATE TABLE IF NOT EXISTS `superpathway` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `superpathway` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `superpathway`))
;


-- -----------------------------------------------------
-- Table `enzyme_has_enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzyme_has_enzyme` ;

CREATE TABLE IF NOT EXISTS `enzyme_has_enzyme` (
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber1` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein1` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`enzyme_ecnumber`, `enzyme_protein_idprotein`, `enzyme_ecnumber1`, `enzyme_protein_idprotein1`))
;


-- -----------------------------------------------------
-- Table `enzyme_has_module`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `enzyme_has_module` ;

CREATE TABLE IF NOT EXISTS `enzyme_has_module` (
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  `module_id` INT NOT NULL,
  `name` VARCHAR(45) NULL,
  `note` VARCHAR(45) NULL,
  PRIMARY KEY (`enzyme_ecnumber`, `enzyme_protein_idprotein`, `module_id`))
;

-- -----------------------------------------------------
-- View `reactions_view_noPath_or_noEC`
-- -----------------------------------------------------

CREATE  OR REPLACE VIEW reactions_view_noPath_or_noEC AS
SELECT DISTINCT idreaction, reaction.name AS reaction_name , equation, reversible, pathway.idpathway, pathway.name AS pathway_name , inModel, isGeneric, reaction.source, originalReaction, compartment_idcompartment , notes
FROM reaction
LEFT JOIN pathway_has_reaction ON idreaction=pathway_has_reaction.reaction_idreaction
LEFT JOIN pathway ON pathway.idpathway=pathway_has_reaction.pathway_idpathway
WHERE (idpathway IS NULL ) 
ORDER BY pathway.name,  reaction.name;

-- -----------------------------------------------------
-- View `reactions_view`
-- -----------------------------------------------------

CREATE  OR REPLACE VIEW reactions_view AS
SELECT DISTINCT idreaction, reaction.name AS reaction_name, equation, reversible, pathway.idpathway, pathway.name AS pathway_name, reaction.inModel, isGeneric, reaction.source, originalReaction, compartment_idcompartment, notes
FROM reaction
INNER JOIN pathway_has_reaction ON idreaction=pathway_has_reaction.reaction_idreaction
INNER JOIN pathway ON pathway.idpathway=pathway_has_reaction.pathway_idpathway
WHERE pathway_has_reaction.pathway_idpathway=pathway.idpathway
ORDER BY pathway.name, reaction.name;

