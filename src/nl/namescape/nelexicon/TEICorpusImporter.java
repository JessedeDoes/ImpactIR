package nl.namescape.nelexicon;
import impact.ee.lexicon.EditableLexicon;
import impact.ee.lexicon.LexiconDatabase;
import impact.ee.util.StringUtils;

import java.util.*;
import java.lang.reflect.*;

import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.nelexicon.database.ObjectRelationalMapping;
import nl.namescape.tei.TEITagClasses;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

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
	DatabaseMapping databaseMapping = new DatabaseMapping();
	
	int nDocuments = 0;
	int maxDocuments= 10000;
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
		
		System.err.println("TITLE: " + document.title);
		
		Map<String, Document>
			tokenizedParagraphHash = new HashMap<String, Document>();
		
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
				addQuotationToAttestation(at,n, tokenizedParagraphHash);
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
					NEAttestation at = createNEAttestation(document, pAwf, pid);	
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

	private void addQuotationToAttestation(NEAttestation at, Element n, Map<String, Document> tokenizedParagraphHash) 
	{
		// TODO Auto-generated method stub
		Element ancestor = (Element) n.getParentNode();
		while (true)
		{
			String text = ancestor.getTextContent().trim();
			String[] words = text.split("\\s+");
			System.err.println("SIZE "  + words.length);
			if (words.length > this.quotationLength || 
					TEITagClasses.isSentenceSplittingElement(ancestor))
			{
			
				break;
			}
			try
			{
				ancestor = (Element) ancestor.getParentNode();
			} catch (Exception e)
			{
				break;
			}
		}
		//System.exit(1);
		Document tokenizedElement = tokenizedParagraphHash.get(ancestor.getAttribute("xml:id"));
		if (tokenizedElement == null)
		{
			tokenizedElement = 
			 new TEITokenizer().tokenizeString(XML.NodeToString(ancestor));
			tokenizedParagraphHash.put(ancestor.getAttribute("xml:id"), tokenizedElement);
		}
		// System.err.println(XML.documentToString(tokenizedElement));
		
		//at.quotation = ancestor.getTextContent(); // HM HM...
		
		List<Element> nameInContextx = XML.getElementsByTagnameAndAttribute(tokenizedElement.getDocumentElement(), 
				n.getTagName(), "xml:id", n.getAttribute("xml:id"), false);
		Element nameInContext = nameInContextx.get(0);
		
		//System.err.println(XML.NodeToString(tokenizedElement));
		
		List<Element> words = 
				TEITagClasses.getTokenElements(tokenizedElement.getDocumentElement());
		List<Element> wordsInEntity = 
				TEITagClasses.getTokenElements(nameInContext);
		
		Element firstWord = wordsInEntity.get(0);
		Element lastWord = wordsInEntity.get(wordsInEntity.size()-1);
		int startIndex = 1, endIndex=0;
		for (int i=0; i < words.size(); i++)
		{
			Element w = words.get(i);
			if (w==firstWord) startIndex=i;
			if (w==lastWord) endIndex=i;
		}
		int start = Math.max(0, startIndex-10);
		int end = Math.min(words.size(), endIndex+10);
		String concordance="";
		for (int i=start; i < end; i++)
		{
			if (i == startIndex)
				concordance += "<oVar>";
			
		
			concordance += words.get(i).getTextContent();
			if (i== endIndex)
				concordance += "</oVar>";
			if (i < end -1)
			{
				concordance += " ";
			}
		}
		System.err.println("CONC: " + concordance);
		at.quotation = concordance;
		// OK... now we have a suitable parent node
		// if the content is not tokenized, we should tokenize it really
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
		
		if (databaseMapping.containmentMap.size() > 0)
		databaseMapping.containmentMapping.insertObjects(ldb.connection, 
				"analyzed_wordform_groups",  databaseMapping.containmentMap.keySet());
		
		for (Object o: databaseMapping.attestationMap.keySet())
		{
			NEAttestation at = (NEAttestation) o;
			at.documentKey = at.document.primaryKey;
			at.analyzedWordformKey = at.awf.primaryKey;
		}
		
		databaseMapping.attestationMapping.insertObjectsInPortions(ldb.connection, "token_attestations", databaseMapping.attestationMap.keySet(), 10000);
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
