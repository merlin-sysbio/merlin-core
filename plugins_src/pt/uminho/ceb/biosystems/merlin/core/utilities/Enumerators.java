package pt.uminho.ceb.biosystems.merlin.core.utilities;

/**
 * @author Oscar Dias
 *
 */
public class Enumerators {

	/**
	 * @author Oscar Dias
	 *
	 */
	public enum CompartmentsTool {

		PSort,
		LocTree,
		WoLFPSORT;

		public String getTool(String tool) {

			if(tool.equals("PSort"))
				return "PSORTb v3.0";

			if(tool.equals("LocTree"))
				return "LocTree3";
			
			if(tool.equals("WoLFPSORT"))
				return "WoLFPSORT";

			return tool;
		}
	}
	
	/**
	 * @author Oscar Dias
	 *
	 */
	public enum HomologySearchServer {

		EBI,
		NCBI,
		HMMER
	}
	
	/**
	 * @author ODias
	 *
	 */
	public static enum IntegrationType {
		
		MODEL,
		MERGE,
		ANNOTATION
		
	}
	
	/**
	 * @author ODias
	 *
	 */
	public enum EbiRemoteDatabasesEnum{
		
		uniprotkb, 
		uniprotkb_swissprot, 
		uniprotkb_swissprotsv, 
		uniprotkb_trembl, 
		uniprotkb_archaea, 
		uniprotkb_arthropoda, 
		uniprotkb_bacteria
	}
	

	/**
	 * @author ODias
	 *
	 */
	public enum HmmerRemoteDatabasesEnum {
		
		uniprotkb,
		swissprot,
		uniprotrefprot,
		pdb,
//		ensemblgenomes, 
//		ensembl, 
		qfo,
		rp75,
		rp55,
		rp35,
		rp15
		
		//http://hmmer-web-docs.readthedocs.io/en/latest/searches.html?highlight=databases
	}
	
	/**
	 * @author ODias
	 *
	 */
	public enum BlastMatrix{
		AUTOSELECTION,
		BLOSUM62,
		BLOSUM45,
		BLOSUM80,
		PAM30,
		PAM70
	}

	/**
	 * @author ODias
	 *
	 */
	public enum BlastProgram{
		//ncbi-
		blastp,
		//ncbi-
		//		blastn,
		blastx,
		//		tblastn,
		//		tblastx
	}

	public enum SequenceType {
		
		protein,
		dna,
		rna
	}
	
	public enum NumberofAlignments {
		
		five (5),
		ten (10){
			@Override
			public String toString(){
				return "10";
			}
		},
		twenty (20){
			@Override
			public String toString(){
				return "20";
			}
		},
		fifty (50){
			@Override
			public String toString(){
				return "50";
			}
		},
		one_hundred (100){
			@Override
			public String toString(){
				return "100";
			}
		},
		one_hundred_and_fifty (150){
			@Override
			public String toString(){
				return "150";
			}
		},
		two_hundred (200){
			@Override
			public String toString(){
				return "200";
			}
		},
		two_hundred_and_fifty (250){
			@Override
			public String toString(){
				return "250";
			}
		},
		five_hundred (500){
			@Override
			public String toString(){
				return "500";
			}
		},
		seven_hundred_and_fifty (750){
			@Override
			public String toString(){
				return "750";
			}
		},
		one_thousand (1000){
			@Override
			public String toString(){
				return "1000";
			}
		};		
		
		private final int index;   

		NumberofAlignments(int index) {
			this.index = index;
		}

		public int index() { 
			return index; 
		}
		
		@Override
		public String toString(){
			return "5";
		}
	}

	/**
	 * @author ODias
	 *
	 */
	public enum NcbiRemoteDatabasesEnum{
		nr,
		swissprot,
		yeast,
		refseq_protein,
		ecoli,
		pdb
	}

	/**
	 * @author ODias
	 *
	 */
	public enum WordSize{

		auto (-1),
		wordSize_2 (2),
		wordSize_3 (3);

		private final int index;   

		WordSize(int index) {
			this.index = index;
		}

		public int index() { 
			return index; 
		}
	}

	public enum GeneticCode {

		Standard (1),
		Vertebrate_Mitochondrial (2),
		Mitochondrial (3),
		MoldProtoCoelMitoMycoSpiro (4),
		Invertebrate_Mitochondrial (5),
		Ciliate_Macronuclear (6),
		Echinodermate_Mitochondrial (9),
		Alt_Ciliate_Macronuclear (10),
		Eubacterial (11),
		Alternative_Yeast (12),
		Ascidian_Mitochondrial (13),
		Flatworm_Mitochondrial (14),
		Blepharisma_Macronuclear (15);

		private final int index;   

		GeneticCode(int index) {
			this.index = index;
		}

		public int index() { 
			return index; 
		}
	}
	
	/**
	 * @author ODias
	 *
	 */
	public enum SchemaType {

		all_information,
		model,
		enzymes_annotation,
		transport_proteins,
		transport_annotations,
		compartment_annotation, 
		interpro_annotation,
		ignore
	}
	
	/**
	 * Types of database information
	 * 
	 * @author Oscar Dias
	 *
	 */
	public enum InformationType {
		
		HOMOLOGY,
		KEGG,
		KO,
		TRANSPORTERS,
		MANUAL,
		EBIOMASS,
		DRAINS,
		GENBANK,
		GFF
		
		//ENUM('HOMOLOGY','MANUAL','KEGG','TRANSPORTERS','KO')
	}
	
	/**
	 * GFF3 Source
	 * 
	 * @author Antonio Dias
	 *
	 */
	public enum GFFSource {
		
		UniProt,
//		NCBI,
		Other
		
	}
	
	public enum BlastSource {
		
		NCBI,
		EBI
	}
	
	public enum FileExtension {
		
		OUT,
		TXT
	}
	
	public enum reversibilitySource {
		
		ModelSEED,
		Zeng
	}
	
	public enum reversibilityTemplate {
		
		GramNegative,
		GramPositive,
		Microbial,
		Mycobacteria,
//		Plant,
		Human
	}
	
	public enum ebiomassTemplate {
		
		Archea,
		Cyano,
		GramPositive,
		GramNegative,
		Mold,
		Yeast,
		Custom
	}
	
	/**
	 * @author davidelagoa
	 *
	 */
	public enum ExportType{
		
		REPORTS,
		INTEGRATION
		
	}
}
	
	
	
