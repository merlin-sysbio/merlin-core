-- -----------------------------------------------------
-- procedure getMetaboliteTaxonomyScores
-- -----------------------------------------------------
DROP procedure IF EXISTS `getMetaboliteTaxonomyScores`;
-- -----------------------------------------------------
-- procedure getTransportTypeTaxonomyScore
-- -----------------------------------------------------
DROP procedure IF EXISTS `getTransportTypeTaxonomyScore`;
-- -----------------------------------------------------
-- function getFrequency
-- -----------------------------------------------------
DROP function IF EXISTS `getFrequency`;

DELIMITER $$
CREATE PROCEDURE getMetaboliteTaxonomyScores (IN originTaxonomy BIGINT UNSIGNED, IN minimal_hits BIGINT UNSIGNED, IN alpha FLOAT UNSIGNED, IN beta_penalty FLOAT UNSIGNED, IN idproject BIGINT UNSIGNED)
BEGIN
   SELECT metabolite_id, gene_id, similarity_score_sum/(
       SELECT SUM(similarity)
       FROM genes_has_tcdb_registries
       INNER JOIN genes ON genes.id = genes_has_tcdb_registries.gene_id
       INNER JOIN tcdb_registries ON (genes_has_tcdb_registries.uniprot_id=tcdb_registries.uniprot_id AND genes_has_tcdb_registries.version=tcdb_registries.version)
       WHERE project_id = idproject AND latest_version AND genes_has_tcdb_registries.gene_id = genes_has_metabolites.gene_id)
   *alpha+(1-alpha)*
   (taxonomy_score_sum*(1-(minimal_hits-getFrequency(frequency,minimal_hits))*beta_penalty)/(originTaxonomy*frequency)) as final_score
   FROM genes_has_metabolites;
  /*WHERE genes_id = geneid;*/
END 
$$


DELIMITER $$
CREATE PROCEDURE getTransportTypeTaxonomyScore (IN originTaxonomy BIGINT UNSIGNED, IN minimal_hits BIGINT UNSIGNED, IN alpha FLOAT UNSIGNED, IN beta_penalty FLOAT UNSIGNED, IN idproject BIGINT UNSIGNED)
BEGIN
   SELECT transport_type_id, metabolite_id, gene_id, transport_type_score_sum/(
       SELECT SUM(similarity)
       FROM genes_has_tcdb_registries
       INNER JOIN genes_has_metabolites ON genes_has_tcdb_registries.gene_id=genes_has_metabolites.gene_id
	   INNER JOIN genes ON genes.id = genes_has_tcdb_registries.gene_id
       INNER JOIN tcdb_registries ON (genes_has_tcdb_registries.uniprot_id=tcdb_registries.uniprot_id AND genes_has_tcdb_registries.version=tcdb_registries.version)
       WHERE project_id = idproject AND latest_version AND genes_has_tcdb_registries.gene_id = genes_has_metabolites_has_type.gene_id
       AND genes_has_metabolites.metabolite_id=genes_has_metabolites_has_type.metabolite_id)
	   *alpha+(1-alpha)*
	  (genes_has_metabolites_has_type.taxonomy_score_sum*(1-(minimal_hits-getFrequency(genes_has_metabolites_has_type.frequency,minimal_hits))*beta_penalty)/(originTaxonomy*genes_has_metabolites_has_type.frequency)) as final_score
   FROM genes_has_metabolites_has_type
   ORDER BY gene_id , metabolite_id , transport_type_id;
/*   WHERE metabolites_id=metabolitesid;*/
  END
  $$


DELIMITER $$
CREATE FUNCTION getFrequency(frequency INT, minimal_hits INT)
  RETURNS INT
  BEGIN
    DECLARE result INT(11);

    IF frequency > minimal_hits THEN SET result = minimal_hits;
    ELSE SET result = frequency;
    END IF;

    RETURN result;
  END 
$$