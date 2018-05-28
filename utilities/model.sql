-- -----------------------------------------------------
-- Schema schema_model
-- -----------------------------------------------------

DROP VIEW IF EXISTS `reactions_view` ;

DROP VIEW IF EXISTS `reactions_view_noPath_or_noEC` ;

DROP TABLE IF EXISTS `ri_function` ;

DROP TABLE IF EXISTS `chromosome` ;

DROP TABLE IF EXISTS `gene` ;

DROP TABLE IF EXISTS `sequence` ;

DROP TABLE IF EXISTS `compartment` ;

DROP TABLE IF EXISTS `pathway` ;

DROP TABLE IF EXISTS `compound` ;

DROP TABLE IF EXISTS `reaction` ;

DROP TABLE IF EXISTS `sequence_feature` ;

DROP TABLE IF EXISTS `stoichiometry` ;

DROP TABLE IF EXISTS `transcription_unit` ;

DROP TABLE IF EXISTS `transcription_unit_gene` ;

DROP TABLE IF EXISTS `feature` ;

DROP TABLE IF EXISTS `protein` ;

DROP TABLE IF EXISTS `enzyme` ;

DROP TABLE IF EXISTS `strain` ;

DROP TABLE IF EXISTS `substrate_affinity` ;

DROP TABLE IF EXISTS `functional_parameter` ;

DROP TABLE IF EXISTS `module` ;

DROP TABLE IF EXISTS `entityisfrom` ;

DROP TABLE IF EXISTS `reaction_has_enzyme` ;

DROP TABLE IF EXISTS `promoter` ;

DROP TABLE IF EXISTS `enzymatic_cofactor` ;

DROP TABLE IF EXISTS `regulatory_event` ;

DROP TABLE IF EXISTS `protein_complex` ;

DROP TABLE IF EXISTS `subunit` ;

DROP TABLE IF EXISTS `protein_composition` ;

DROP TABLE IF EXISTS `experimental_factor` ;

DROP TABLE IF EXISTS `pathway_has_reaction` ;

DROP TABLE IF EXISTS `experiment_description` ;

DROP TABLE IF EXISTS `experiment_substrate_affinity` ;

DROP TABLE IF EXISTS `experiment_inhibitor` ;

DROP TABLE IF EXISTS `experiment_turnover_number` ;

DROP TABLE IF EXISTS `sigma_promoter` ;

DROP TABLE IF EXISTS `metabolic_regulation` ;

DROP TABLE IF EXISTS `activating_reaction` ;

DROP TABLE IF EXISTS `dictionary` ;

DROP TABLE IF EXISTS `enzymatic_alternative_cofactor` ;

DROP TABLE IF EXISTS `aliases` ;

DROP TABLE IF EXISTS `dblinks` ;

DROP TABLE IF EXISTS `transcription_unit_promoter` ;

DROP TABLE IF EXISTS `effector` ;

DROP TABLE IF EXISTS `modules_has_compound` ;

DROP TABLE IF EXISTS `gene_has_compartment` ;

DROP TABLE IF EXISTS `gene_has_orthology` ;

DROP TABLE IF EXISTS `pathway_has_module` ;

DROP TABLE IF EXISTS `same_as` ;

DROP TABLE IF EXISTS `is_super_reaction` ;

DROP TABLE IF EXISTS `superpathway` ;

-- -----------------------------------------------------
-- Table `ri_function`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `ri_function` (
  `idri_function` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `symbol` CHAR(2) NULL,
  `ri_function` CHAR(20) NULL,
  PRIMARY KEY (`idri_function`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `chromosome`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `chromosome` (
  `idchromosome` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(15) NOT NULL,
  PRIMARY KEY (`idchromosome`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `gene`
-- -----------------------------------------------------

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
  INDEX `gene_name` (`name` ASC),
  INDEX `gene_FKIndex1` (`chromosome_idchromosome` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sequence`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `sequence` (
  `idsequence` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `gene_idgene` INT UNSIGNED NOT NULL,
  `sequence_type` VARCHAR(20) NULL,
  `sequence` TEXT NULL,
  `sequence_length` INT UNSIGNED NULL,
  PRIMARY KEY (`idsequence`),
  INDEX `sequence_FKIndex1` (`gene_idgene` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway`
-- -----------------------------------------------------

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

CREATE TABLE IF NOT EXISTS `compartment` (
  `idcompartment` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(25) NULL,
  `abbreviation` VARCHAR(20) NULL,
  PRIMARY KEY (`idcompartment`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `reaction`
-- -----------------------------------------------------

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
  INDEX `reversible` (`reversible` ASC),
  INDEX `fk_reaction_compartment1_idx` (`compartment_idcompartment` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `stoichiometry`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `stoichiometry` (
  `idstoichiometry` INT NOT NULL AUTO_INCREMENT,
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `compartment_idcompartment` INT UNSIGNED NOT NULL,
  `stoichiometric_coefficient` FLOAT NOT NULL,
  `numberofchains` VARCHAR(20) NULL,
  PRIMARY KEY (`idstoichiometry`, `reaction_idreaction`, `compound_idcompound`, `compartment_idcompartment`),
  INDEX `metabolite_FKIndex1` (`compound_idcompound` ASC),
  INDEX `metabolite_FKIndex2` (`reaction_idreaction` ASC),
  INDEX `metabolite_FKIndex3` (`compartment_idcompartment` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `feature`
-- -----------------------------------------------------

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

CREATE TABLE IF NOT EXISTS `sequence_feature` (
  `feature_idfeature` INT UNSIGNED NOT NULL,
  `sequence_idsequence` INT UNSIGNED NOT NULL,
  `start_position_approximate` VARCHAR(20) NULL,
  `end_position_approximate` VARCHAR(20) NULL,
  PRIMARY KEY (`feature_idfeature`),
  INDEX `sequence_feature_FKIndex1` (`feature_idfeature` ASC),
  INDEX `sequence_feature_FKIndex2` (`sequence_idsequence` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `transcription_unit` (
  `idtranscription_unit` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NULL,
  PRIMARY KEY (`idtranscription_unit`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit_gene`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `transcription_unit_gene` (
  `transcription_unit_idtranscription_unit` INT UNSIGNED NOT NULL,
  `gene_idgene` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`transcription_unit_idtranscription_unit`, `gene_idgene`),
  INDEX `tugene_FKIndex1` (`transcription_unit_idtranscription_unit` ASC),
  INDEX `tugene_FKIndex2` (`gene_idgene` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `protein`
-- -----------------------------------------------------

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

CREATE TABLE IF NOT EXISTS `enzyme` (
  `ecnumber` VARCHAR(15) NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `optimal_ph` FLOAT NULL,
  `posttranslational_modification` VARCHAR(100) NULL,
  `inModel` TINYINT(1) NULL,
  `source` VARCHAR(45) NULL,
  PRIMARY KEY (`ecnumber`, `protein_idprotein`),
  INDEX `fk_enzyme_protein1_idx` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `reaction_has_enzyme`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `reaction_has_enzyme` (
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`reaction_idreaction`, `enzyme_ecnumber`, `enzyme_protein_idprotein`),
  INDEX `reactionhasenzyme_FKIndex1` (`reaction_idreaction` ASC),
  INDEX `fk_reaction_has_enzyme_enzyme1_idx` (`enzyme_ecnumber` ASC, `enzyme_protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `strain`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `strain` (
  `idstrain` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(60) NULL,
  PRIMARY KEY (`idstrain`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `entityisfrom`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `entityisfrom` (
  `strain_idstrain` INT UNSIGNED NOT NULL,
  `wid` INT UNSIGNED NULL,
  INDEX `entityisfrom_FKIndex1` (`strain_idstrain` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzymatic_cofactor`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `enzymatic_cofactor` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `prosthetic` TINYINT(1) NULL,
  INDEX `enzimaticreactioncofactor_FKIndex1` (`compound_idcompound` ASC),
  INDEX `enzimatic_cofactor_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `promoter`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `promoter` (
  `idpromoter` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NULL,
  `absolute_position` DOUBLE NULL,
  PRIMARY KEY (`idpromoter`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `regulatory_event`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `regulatory_event` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `promoter_idpromoter` INT UNSIGNED NOT NULL,
  `ri_function_idri_function` INT UNSIGNED NOT NULL,
  `binding_site_position` DECIMAL(15,6) NULL,
  PRIMARY KEY (`protein_idprotein`, `promoter_idpromoter`, `ri_function_idri_function`),
  INDEX `regulatory_event_FKIndex1` (`promoter_idpromoter` ASC),
  INDEX `regulatory_event_FKIndex2` (`protein_idprotein` ASC),
  INDEX `regulatory_event_FKIndex3` (`ri_function_idri_function` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `enzymatic_alternative_cofactor`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `enzymatic_alternative_cofactor` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `original_cofactor` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`compound_idcompound`, `original_cofactor`, `protein_idprotein`),
  INDEX `enzimatic_reaction_alternative_compound_FKIndex1` (`compound_idcompound` ASC),
  INDEX `enzimatic_alternative_cofactor_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `module`
-- -----------------------------------------------------

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
-- Table `protein_complex`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `protein_complex` (
  `idprotein_complex` INT NOT NULL,
  `name` TEXT NULL,
  PRIMARY KEY (`idprotein_complex`))
;


-- -----------------------------------------------------
-- Table `subunit`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `subunit` (
  `gene_idgene` INT UNSIGNED NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `module_id` INT NULL,
  `protein_complex_idprotein_complex` INT NULL,
  `note` VARCHAR(45) NULL,
  `gpr_status` VARCHAR(45) NULL,
  INDEX `subunit_FKIndex2` (`gene_idgene` ASC),
  INDEX `fk_subunit_module1_idx` (`module_id` ASC),
  INDEX `fk_subunit_protein_complex1_idx` (`protein_complex_idprotein_complex` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `protein_composition`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `protein_composition` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `subunit` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`protein_idprotein`, `subunit`),
  INDEX `protein_composition_FKIndex1` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `functional_parameter`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `functional_parameter` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `parameter_type` VARCHAR(50) NOT NULL,
  `parameter_value` FLOAT NULL,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`, `parameter_type`),
  INDEX `inhibitor_FKIndex1` (`compound_idcompound` ASC),
  INDEX `inhibitor_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `substrate_affinity`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `substrate_affinity` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `substrate_affinity` FLOAT NOT NULL,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`),
  INDEX `substract_FKIndex2` (`compound_idcompound` ASC),
  INDEX `substrate_activity_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway_has_reaction`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `pathway_has_reaction` (
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  INDEX `path_reaction_FKIndex2` (`reaction_idreaction` ASC),
  INDEX `pathway_has_reaction_FKIndex2` (`pathway_idpathway` ASC),
  PRIMARY KEY (`reaction_idreaction`, `pathway_idpathway`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experimental_factor`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `experimental_factor` (
  `idexperimental_factor` INT UNSIGNED NOT NULL,
  `factor` VARCHAR(255) NULL,
  PRIMARY KEY (`idexperimental_factor`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_description`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `experiment_description` (
  `idexperiment` INT UNSIGNED NOT NULL,
  `experiment_descriptional_factor_idexperimental_factor` INT UNSIGNED NOT NULL,
  `value` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`idexperiment`, `experiment_descriptional_factor_idexperimental_factor`),
  INDEX `experiment_FKIndex1` (`experiment_descriptional_factor_idexperimental_factor` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_substrate_affinity`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `experiment_substrate_affinity` (
  `experiment_description` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `substrate_affinity` FLOAT NULL,
  PRIMARY KEY (`experiment_description`, `compound_idcompound`, `protein_idprotein`),
  INDEX `experiment_km_FKIndex1` (`compound_idcompound` ASC),
  INDEX `experiment_substrate_activity_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_inhibitor`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `experiment_inhibitor` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `experiment_description` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `ki` FLOAT NULL,
  PRIMARY KEY (`compound_idcompound`, `experiment_description`, `protein_idprotein`),
  INDEX `experiment_inhibitor_FKIndex1` (`compound_idcompound` ASC),
  INDEX `experiment_inhibitor_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `experiment_turnover_number`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `experiment_turnover_number` (
  `experiment_description` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `turnover_number` FLOAT NULL,
  PRIMARY KEY (`experiment_description`, `compound_idcompound`, `protein_idprotein`),
  INDEX `experiment_turnover_number_FKIndex1` (`compound_idcompound` ASC),
  INDEX `experiment_turnover_number_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `transcription_unit_promoter`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `transcription_unit_promoter` (
  `promoter_idpromoter` INT UNSIGNED NOT NULL,
  `transcription_unit_idtranscription_unit` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`promoter_idpromoter`, `transcription_unit_idtranscription_unit`),
  INDEX `transcription_unit_promoter_FKIndex1` (`promoter_idpromoter` ASC),
  INDEX `transcription_unit_promoter_FKIndex2` (`transcription_unit_idtranscription_unit` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `sigma_promoter`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `sigma_promoter` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `promoter_idpromoter` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`protein_idprotein`, `promoter_idpromoter`),
  INDEX `sigma_promoter_FKIndex1` (`protein_idprotein` ASC),
  INDEX `sigma_promoter_FKIndex2` (`promoter_idpromoter` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `metabolic_regulation`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `metabolic_regulation` (
  `compound_idcompound` INT UNSIGNED NOT NULL,
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `mode` CHAR(1) NULL,
  `mechanism` VARCHAR(25) NULL,
  PRIMARY KEY (`compound_idcompound`, `protein_idprotein`),
  INDEX `metabolic_regulation_FKIndex1` (`protein_idprotein` ASC),
  INDEX `metabolic_regulation_FKIndex2` (`compound_idcompound` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `activating_reaction`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `activating_reaction` (
  `reaction_idreaction` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`reaction_idreaction`, `enzyme_ecnumber`, `enzyme_protein_idprotein`),
  INDEX `activating_reaction_FKIndex2` (`reaction_idreaction` ASC),
  INDEX `fk_activating_reaction_enzyme1_idx` (`enzyme_ecnumber` ASC, `enzyme_protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `dictionary`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `dictionary` (
  `class` VARCHAR(1) NOT NULL,
  `aliases` VARCHAR(250) NOT NULL,
  `common_name` VARCHAR(250) NULL,
  PRIMARY KEY (`class`, `aliases`))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `aliases`
-- -----------------------------------------------------

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

CREATE TABLE IF NOT EXISTS `effector` (
  `protein_idprotein` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`protein_idprotein`, `compound_idcompound`),
  INDEX `effector_FKIndex1` (`compound_idcompound` ASC),
  INDEX `effector_FKIndex2` (`protein_idprotein` ASC))


ROW_FORMAT = DEFAULT;


-- -----------------------------------------------------
-- Table `pathway_has_compound`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_compound` ;

CREATE TABLE IF NOT EXISTS `pathway_has_compound` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `compound_idcompound`),
  INDEX `fk_pathway_has_compound_pathway1_idx` (`pathway_idpathway` ASC),
  INDEX `fk_pathway_has_compound_compound1_idx` (`compound_idcompound` ASC))
;


-- -----------------------------------------------------
-- Table `pathway_has_enzyme`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `pathway_has_enzyme` ;

CREATE TABLE IF NOT EXISTS `pathway_has_enzyme` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `enzyme_ecnumber` VARCHAR(15) NOT NULL,
  `enzyme_protein_idprotein` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `enzyme_ecnumber`, `enzyme_protein_idprotein`),
  INDEX `fk_pathway_has_enzyme_pathway1_idx` (`pathway_idpathway` ASC),
  INDEX `fk_pathway_has_enzyme_enzyme1_idx` (`enzyme_ecnumber` ASC, `enzyme_protein_idprotein` ASC))
;


-- -----------------------------------------------------
-- Table `gene_has_compartment`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `gene_has_compartment` (
  `gene_idgene` INT UNSIGNED NOT NULL,
  `compartment_idcompartment` INT UNSIGNED NOT NULL,
  `primaryLocation` TINYINT(1) NULL,
  `score` VARCHAR(20) NULL,
  PRIMARY KEY (`gene_idgene`, `compartment_idcompartment`),
  INDEX `fk_gene_has_compartment_compartment1_idx` (`compartment_idcompartment` ASC),
  INDEX `fk_gene_has_compartment_gene1_idx` (`gene_idgene` ASC))
;


-- -----------------------------------------------------
-- Table `modules_has_compound`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `modules_has_compound` (
  `modules_id` INT NOT NULL,
  `compound_idcompound` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`modules_id`, `compound_idcompound`),
  INDEX `fk_modules_has_compound_compound1_idx` (`compound_idcompound` ASC),
  INDEX `fk_modules_has_compound_modules1_idx` (`modules_id` ASC))
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
  PRIMARY KEY (`module_id`, `orthology_id`),
  INDEX `fk_modules_has_orthology_orthology1_idx` (`orthology_id` ASC),
  INDEX `fk_modules_has_orthology_modules1_idx` (`module_id` ASC))
;


-- -----------------------------------------------------
-- Table `pathway_has_module`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `pathway_has_module` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `module_id` INT NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `module_id`),
  INDEX `fk_pathway_has_modules_modules1_idx` (`module_id` ASC),
  INDEX `fk_pathway_has_modules_pathway1_idx` (`pathway_idpathway` ASC))
;


-- -----------------------------------------------------
-- Table `gene_has_orthology`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `gene_has_orthology` (
  `gene_idgene` INT UNSIGNED NOT NULL,
  `orthology_id` INT NOT NULL,
  `similarity` FLOAT NULL,
  PRIMARY KEY (`gene_idgene`, `orthology_id`),
  INDEX `fk_gene_has_orthology_orthology1_idx` (`orthology_id` ASC),
  INDEX `fk_gene_has_orthology_gene1_idx` (`gene_idgene` ASC))
;


-- -----------------------------------------------------
-- Table `same_as`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `same_as` (
  `metabolite_id` INT UNSIGNED NOT NULL,
  `similar_metabolite_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`metabolite_id`, `similar_metabolite_id`),
  INDEX `fk_same_as_similar_metabolite_id_idx` (`similar_metabolite_id` ASC),
  INDEX `fk_same_as_metabolite_id_idx` (`metabolite_id` ASC))
;


-- -----------------------------------------------------
-- Table `is_super_reaction`
-- -----------------------------------------------------

CREATE TABLE IF NOT EXISTS `is_super_reaction` (
  `super_reaction_id` INT UNSIGNED NOT NULL,
  `sub_reaction_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`super_reaction_id`, `sub_reaction_id`),
  INDEX `fk_is_super_reaction_sub_reaction_id_idx` (`sub_reaction_id` ASC),
  INDEX `fk_is_super_reaction_super_reaction_id_idx` (`super_reaction_id` ASC))
;


-- -----------------------------------------------------
-- Table `superpathway`
-- -----------------------------------------------------


CREATE TABLE IF NOT EXISTS `superpathway` (
  `pathway_idpathway` INT UNSIGNED NOT NULL,
  `superpathway` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`pathway_idpathway`, `superpathway`),
  INDEX `fk_superpathway_superpathway` (`superpathway` ASC),
  INDEX `fk_superpathway_pathway` (`pathway_idpathway` ASC))
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
