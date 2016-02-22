package nl.namescape.nelexicon;
import impact.ee.lexicon.EditableLexicon;
import impact.ee.lexicon.LexiconDatabase;
import impact.ee.util.StringUtils;

import java.sql.Connection;
import java.util.*;
import java.lang.reflect.*;

import nl.namescape.evaluation.Counter;
import nl.namescape.nelexicon.database.ObjectRelationalMapping;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;
import nl.openconvert.filehandling.DirectoryHandling;
import nl.openconvert.filehandling.DoSomethingWithFile;

import org.w3c.dom.*;

/**
 * This imports a bunch of ne-tagged TEI documents into a
 * "NE lexicon database"
 * 
 * ToDo: something with NE resolution
 * 
 * @author does
 *
 */
public class TEICorpusImporter implements DoSomethingWithFile
{
	EditableLexicon lexicon = null;
	LexiconDatabase  ldb = new LexiconDatabase("impactdb", "ORMTEST");
	Connection connection = ldb.connection;
	
	
	String psqlurl = "jdbc:postgresql://" + "svowdb02" + ":" + 5432 + "/" + "TestNeLexicon";
	
	DatabaseMapping databaseMapping = new DatabaseMapping();
	
	int nDocuments = 0;
	int maxDocuments = 10000;
	boolean addEntriesForParts = true;
	int quotationLength = 50;
	
	public TEICorpusImporter()
	{
		databaseMapping.init();
	}
		
	public void importDocument(String filename)
	{
		try
		{
			if (nDocuments >= maxDocuments)
			  return;
			Document d = XML.parse(filename);
			NEDocument document = new NEDocument(d);
			document.url = filename;
			importDocument(document);
			nDocuments++;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void importDocument(NEDocument document)
	{
		Element root = document.DOMDocument.getDocumentElement();
		List<Element> names = 
				XML.getElementsByTagname(root, "ns:ne", false);
		
		nl.openconvert.log.ConverterLog.defaultLog.println("TITLE: " + document.title);
		
		ElementConcordancer concordancer = new ElementConcordancer();
		
		databaseMapping.documentMapping.insertObject(connection,"documents",  document); 
		
		for (Element n: names)
		{
			NELemma neLemma = createNELemma(n);
			NEWordform neWord = createNEWordform(n);
			NEAnalyzedWordform awf = createNEAnalyzedWordform(neLemma, neWord);
			
			String id = n.getAttribute("xml:id");
			
			if (id != null && id.length() > 0)
			{
				
				String quotation = concordancer.getConcordance(n);
				NEAttestation at = createNEAttestation(document, awf, id, quotation);	
			}
			
			List<Element> neParts = XML.getElementsByTagname(n, "ns:nePart", false);
			int partNumber=0;
			
			if (addEntriesForParts) for (Element np: neParts)
			{
				NELemma pLemma = createNELemma(np);
				NEWordform pWord = createNEWordform(np);
				NEAnalyzedWordform pAwf = createNEAnalyzedWordform(pLemma, pWord);
				String pid = np.getAttribute("xml:id");
				if (pid != null && pid.length() > 0)
				{
					NEAttestation at = createNEAttestation(document, pAwf, pid, null);	
				}
				NEContainment nec = new NEContainment();
				nec.parent = awf;
				nec.child = pAwf;
				nec.partNumber = partNumber++;
				nec = (NEContainment) 
					DatabaseMapping.canonical(databaseMapping.containmentMap, nec);
				// HM (bah.) should add extra key for group id to this...
				// or should we add this to the PoS info for an NE (structure?)
			}
			// lexicon.addLemma(lemma, PoS, neLabel, gloss);
		}
	}

	

	private NEAttestation createNEAttestation(NEDocument document,
			NEAnalyzedWordform awf, String id, String quotation) 
	{
		NEAttestation at = new NEAttestation();
		at.awf = awf;
		at.document = document;
		at.tokenID = id;
		at.quotation = quotation;
		databaseMapping.attestationMap.put(at, at);
		return at;
	}

	private NEAnalyzedWordform createNEAnalyzedWordform(NELemma neLemma,
			NEWordform neWord) 
	{
		NEAnalyzedWordform awf = new NEAnalyzedWordform();
		awf.lemma = neLemma;
		awf.wordform = neWord;
		awf = (NEAnalyzedWordform) 
			DatabaseMapping.canonical(databaseMapping.awfMap,awf);
		return awf;
	}

	private NEWordform createNEWordform(Element n) 
	{
		NEWordform neWord = new NEWordform();
		neWord.wordform = n.getTextContent();
		neWord = (NEWordform) DatabaseMapping.canonical(databaseMapping.wordformMap,neWord);
		return neWord;
	}

	private NELemma createNELemma(Element n) 
	{
		NELemma neLemma = new NELemma();
		neLemma.lemma = n.getAttribute("normalizedForm");
		neLemma.neLabel = n.getAttribute("type");
		neLemma.gloss = n.getAttribute("gloss");
		neLemma = (NELemma) DatabaseMapping.canonical(databaseMapping.lemmaMap,neLemma);
		return neLemma;
	}
	
	public void flush()
	{
		databaseMapping.lemmaMapping.insertObjects(connection, "lemmata", databaseMapping.lemmaMap.keySet());
		databaseMapping.wordformMapping.insertObjects(connection, "wordforms", databaseMapping.wordformMap.keySet());
		
		for (Object o: databaseMapping.awfMap.keySet())
		{
			NEAnalyzedWordform awf = (NEAnalyzedWordform) o;
			awf.lemmaKey = awf.lemma.primaryKey;
			awf.wordformKey = awf.wordform.primaryKey;
		}
	
		databaseMapping.awfMapping.insertObjects(connection, 
				"analyzed_wordforms", databaseMapping.awfMap.keySet());
		
		for (Object o: databaseMapping.containmentMap.keySet())
		{
			NEContainment nec = (NEContainment) o;
			nec.parentKey = nec.parent.primaryKey;
			nec.childKey = nec.child.primaryKey;
		}
		
		if (databaseMapping.containmentMap.size() > 0)
		databaseMapping.containmentMapping.insertObjects(connection, 
				"analyzed_wordform_groups",  databaseMapping.containmentMap.keySet());
		
		for (Object o: databaseMapping.attestationMap.keySet())
		{
			NEAttestation at = (NEAttestation) o;
			at.documentKey = at.document.primaryKey;
			at.analyzedWordformKey = at.awf.primaryKey;
		}
		databaseMapping.attestationMapping.insertObjectsInPortions(connection, "token_attestations", databaseMapping.attestationMap.keySet(), 10000);
	}
	
	private String getNameText(Element e) // whoops;
	{
		List<String> parts = new ArrayList<String>();
		for (Element w: XML.getElementsByTagname(e, "w", false))
		{
			parts.add(w.getTextContent().trim());
		}
		return StringUtils.join(parts, " ");
	}
	
	
	@Override
	public void handleFile(String fileName)
	{
		// TODO Auto-generated method stub
		importDocument(fileName);
	}
	
	public static void main(String[] args)
	{
		TEICorpusImporter tci = new TEICorpusImporter();
		// tci.importDocument(args[0]);
		DirectoryHandling.traverseDirectory(tci, args[0]);
		tci.flush();
	}
}
