package nl.namescape.nelexicon;
import impact.ee.lexicon.EditableLexicon;
import impact.ee.lexicon.LexiconDatabase;
import impact.ee.lexicon.database.ObjectRelationalMapping;
import impact.ee.util.StringUtils;

import java.util.*;
import java.lang.reflect.*;

import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.util.XML;

import org.w3c.dom.*;

public class TEICorpusImporter implements DoSomethingWithFile
{
	EditableLexicon lexicon = null;
	LexiconDatabase  ldb = new LexiconDatabase("impactdb", "ORMTEST");
	DatabaseMapping databaseMapping = new DatabaseMapping();
	
	
	static boolean equal(Object o1, Object o2)
	{
		if (o1==null) return o2==null;
		if (o2==null) return false;
		return o1.equals(o2);
	}
	
	public TEICorpusImporter()
	{
		databaseMapping.init();
	}
	

	
	private static Object canonical(Map<Object,Object> m, Object o)
	{
		if (!m.containsKey(o))
		{
			m.put(o, o);
			return o;
		} else
		{
			//System.err.println("Ah.. seen before: o");
			return m.get(o);
		}
	}
	
	public void importDocument(String filename)
	{
		try
		{
			Document d = XML.parse(filename);
			NEDocument document = new NEDocument(d);
			document.url = filename;
			importDocument(document);
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
		
		
	
		System.err.println("TITLE: " + document.title);
		
		databaseMapping.documentMapping.insertObject(ldb.connection,"documents",  document); 
		
		for (Element n: names)
		{
			NELemma neLemma = createNELemma(n);
			NEWordform neWord = createNEWordform(n);
			NEAnalyzedWordform awf = createNEAnalyzedWordform(neLemma, neWord);
			
			String id = n.getAttribute("xml:id");
			
			if (id != null && id.length() > 0)
			{
				NEAttestation at = createNEAttestation(document, awf, id);	
			}
			
			List<Element> neParts = XML.getElementsByTagname(n, "ns:nePart", false);
			int partNumber=0;
			for (Element np: neParts)
			{
				NELemma pLemma = createNELemma(np);
				NEWordform pWord = createNEWordform(np);
				NEAnalyzedWordform pAwf = createNEAnalyzedWordform(pLemma, pWord);
				String pid = np.getAttribute("xml:id");
				if (id != null && id.length() > 0)
				{
					NEAttestation at = createNEAttestation(document, pAwf, pid);	
				}
				NEContainment nec = new NEContainment();
				nec.parent = awf;
				nec.child = pAwf;
				nec.partNumber = partNumber++;
				nec = (NEContainment) 
					canonical(databaseMapping.containmentMap, nec);
				// HM bah. should add extra key for group id to this...
				// or should we add this to the PoS info for an NE (structure?)
			}
			
			// lexicon.addLemma(lemma, PoS, neLabel, gloss);
		}
	}

	private NEAttestation createNEAttestation(NEDocument document,
			NEAnalyzedWordform awf, String id) 
	{
		NEAttestation at = new NEAttestation();
		at.awf = awf;
		at.document = document;
		at.tokenID = id;
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
			canonical(databaseMapping.awfMap,awf);
		return awf;
	}

	private NEWordform createNEWordform(Element n) 
	{
		NEWordform neWord = new NEWordform();
		neWord.wordform = n.getTextContent();
		neWord = (NEWordform) canonical(databaseMapping.wordformMap,neWord);
		return neWord;
	}

	private NELemma createNELemma(Element n) 
	{
		NELemma neLemma = new NELemma();
		neLemma.lemma = n.getAttribute("normalizedForm");
		neLemma.neLabel = n.getAttribute("type");
		neLemma.gloss = n.getAttribute("gloss");
		neLemma = (NELemma) canonical(databaseMapping.lemmaMap,neLemma);
		return neLemma;
	}
	
	public void flush()
	{
		databaseMapping.lemmaMapping.insertObjects(ldb.connection, "lemmata", databaseMapping.lemmaMap.keySet());
		databaseMapping.wordformMapping.insertObjects(ldb.connection, "wordforms", databaseMapping.wordformMap.keySet());
		
		for (Object o: databaseMapping.awfMap.keySet())
		{
			NEAnalyzedWordform awf = (NEAnalyzedWordform) o;
			awf.lemmaKey = awf.lemma.primaryKey;
			awf.wordformKey = awf.wordform.primaryKey;
		}
		databaseMapping.awfMapping.insertObjects(ldb.connection, 
				"analyzed_wordforms", databaseMapping.awfMap.keySet());
		
		for (Object o: databaseMapping.containmentMap.keySet())
		{
			NEContainment nec = (NEContainment) o;
			nec.parentKey = nec.parent.primaryKey;
			nec.childKey = nec.child.primaryKey;
		}
		
		databaseMapping.containmentMapping.insertObjects(ldb.connection, 
				"analyzed_wordform_groups",  databaseMapping.containmentMap.keySet());
		
		for (Object o: databaseMapping.attestationMap.keySet())
		{
			NEAttestation at = (NEAttestation) o;
			at.documentKey = at.document.primaryKey;
			at.analyzedWordformKey = at.awf.primaryKey;
		}
		
		databaseMapping.attestationMapping.insertObjects(ldb.connection, "token_attestations", databaseMapping.attestationMap.keySet());
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
		//tci.importDocument(args[0]);
		DirectoryHandling.traverseDirectory(tci, args[0]);
		tci.flush();
	}
}
