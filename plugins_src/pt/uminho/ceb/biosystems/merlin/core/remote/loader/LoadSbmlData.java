package pt.uminho.ceb.biosystems.merlin.core.remote.loader;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.MerlinImportUtils;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg.DatabaseInitialData;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg.LoadKeggData;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.ErrorsException;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.io.JSBMLValidationException;


public class LoadSbmlData extends LoadKeggData{

	//		private ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles;
	//		private ConcurrentHashMap<String,Integer> pathwaysID;


	public LoadSbmlData(MerlinImportUtils modelData, Connection connection, DatabaseInitialData databaseInitialData, TimeLeftProgress progress, AtomicInteger datum, AtomicBoolean cancel, AtomicInteger dataSize, long startTime) 
			throws IOException, XMLStreamException, ErrorsException, ParserConfigurationException, SAXException, JSBMLValidationException, SQLException{

		super(connection, databaseInitialData ,progress, datum,  cancel,  dataSize,  startTime);

		//			this.compoundsWithBiologicalRoles = new ConcurrentLinkedQueue<String>();
		//			this.pathwaysID = new ConcurrentHashMap<>();

		this.setKegg_Pathways_Hierarchy(modelData.getResultPathwaysHierarchy());
		this.setResultMetabolites(modelData.getResultMetasbolites());
		this.setResultCompartments(modelData.getResultCompartments());
		this.setResultEnzymes(modelData.getResultEnzymes());
		this.setResultReactions(modelData.getResultReactions());
		this.setResultGenes(modelData.getResultGenes());

	}
}


