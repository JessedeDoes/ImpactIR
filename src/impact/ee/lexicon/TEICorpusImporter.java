package impact.ee.lexicon;
import impact.ee.lexicon.database.ObjectRelationalMapping;
import impact.ee.util.StringUtils;

import java.util.*;
import java.lang.reflect.*;

import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.tei.Metadata;
import nl.namescape.util.XML;

import org.w3c.dom.*;

public class TEICorpusImporter implements DoSomethingWithFile
{
	EditableLexicon lexicon = null;
	LexiconDatabase  ldb = new LexiconDatabase("impactdb", "ORMTEST");
	
	ObjectRelationalMapping lemmaMapping = 
			new ObjectRelationalMapping(NELemma.class, "lemmata");
	ObjectRelationalMapping wordformMapping = 
			new ObjectRelationalMapping(NEWordform.class, "wordforms");
	ObjectRelationalMapping awfMapping = 
			new ObjectRelationalMapping(NEAnalyzedWordform.class, "analyzed_wordforms");
	ObjectRelationalMapping attestationMapping = 
			new ObjectRelationalMapping(NEAttestation.class, "token_attestations");
	ObjectRelationalMapping documentMapping = 
			new ObjectRelationalMapping(NEDocument.class, "documents");
	
	Map<Object, Object> lemmaMap = new HashMap<Object, Object>();
	Map<Object, Object> wordformMap = new HashMap<Object, Object>();
	Map<Object, Object> awfMap = new HashMap<Object, Object>();
	Map<Object, Object> attestationMap = new HashMap<Object, Object>();
	
	
	//Set<WordForm> lemmata = new HashSet<WordForm>();
	
	
	static boolean equal(Object o1, Object o2)
	{
		if (o1==null) return o2==null;
		if (o2==null) return false;
		return o1.equals(o2);
	}
	
	public static class CorpusDocument
	{
		String title;
		String author;
		Integer primaryKey;
	}
	
	public static class NELemma
	{
		public String lemma;
		public String neLabel;
		public String nePartLabel;
		public String lemmaID;
		public String lemmaPoS;
		public String gloss;
		public String sex; // parts only
		public Integer primaryKey = null;
		
		public boolean equals(NELemma other)
		{
			//System.err.println("BLOEP!");
			return equal(this.lemma,other.lemma) 
					&& equal(this.lemmaPoS, other.lemmaPoS)
					&& equal(this.neLabel, other.neLabel);
		}
		
		public boolean equals(Object other)
		{
			//System.err.println("BLOEP!");
			try
			{
				NELemma o = (NELemma) other;
				return equals(o);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			return false;
		}
		
		public int hashCode()
		{
			return (lemma + neLabel + gloss  + sex).hashCode();
		}
	}
	
	public static class NEWordform
	{
		public Integer primaryKey = null;
		public String wordform;
		
		public boolean equals(NEWordform other)
		{
			return equal(this.wordform,other.wordform); 			
		}
		
		public boolean equals(Object other)
		{
			try
			{
				NEWordform o = (NEWordform) other;
				return equals(o);
			} catch (Exception e)
			{
				
			}
			return false;
		}
		public int hashCode()
		{
			return (wordform).hashCode();
		}
	}
	
	public static class NEAnalyzedWordform
	{
		public Integer primaryKey = null;
		public Integer lemmaKey = null;
		public Integer wordformKey = null;
		public NELemma lemma = null;
		public NEWordform wordform = null;
		public int hashCode()
		{
			return (lemma).hashCode() + wordform.hashCode();
		}
		
		public boolean equals(NEAnalyzedWordform other)
		{
			return this.lemma.equals(other.lemma) && this.wordform.equals(other.wordform);
		}
		
		public boolean equals(Object other)
		{
			try
			{
				NEAnalyzedWordform o = (NEAnalyzedWordform) other;
				return equals(o);
			} catch (Exception e)
			{
				
			}
			return false;
		}
	}
	
	public static class NEAttestation // use group attestations (?)
	{
		public Integer primaryKey;
		public Integer analyzedWordformKey;
		public Integer documentKey;
		public String tokenID;
		public NEDocument document;
		public NEAnalyzedWordform awf;	
	}

	public static class NEDocument
	{
		public Integer primaryKey;
		public String title;
		public String author;
		public String publicationYear;
		public String documentID;
		public void getMetadata(Document d) 
		{
			// TODO Auto-generated method stub
			Metadata m = new Metadata(d);
			this.title = m.getValue("title");
			this.author = m.getValue("author");
			this.publicationYear = m.getValue("pubyear");
			this.documentID = m.getValue("idno");
		}
	}
	
	static class NEWordformGroup
	{
		
	}
	
	
	public TEICorpusImporter()
	{
		init();
	}
	
	public void init()
	{
		// lemmata 
		lemmaMapping.addField("modern_lemma", "lemma");
		//orm.addField("wordform", "wordform");
		lemmaMapping.addField("lemma_part_of_speech", "lemmaPoS");
		lemmaMapping.addField("persistent_id", "lemmaID");
		lemmaMapping.addField("ne_label", "neLabel");
		lemmaMapping.setPrimaryKeyField("primaryKey");
		
		wordformMapping.addField("wordform", "wordform");
		wordformMapping.setPrimaryKeyField("primaryKey");
		
		awfMapping.addField("lemma_id", "lemmaKey");
		awfMapping.addField("wordform_id", "wordformKey");
		awfMapping.setPrimaryKeyField("primaryKey");
		
		documentMapping.addField("title", "title");
		documentMapping.addField("author", "author");
		documentMapping.addField("persistent_id", "documentID");
		documentMapping.addField("pub_year", "publicationYear");
		documentMapping.setPrimaryKeyField("primaryKey");
		
		attestationMapping.addField("document_id", "documentKey");
		attestationMapping.addField("analyzed_wordform_id", "analyzedWordformKey");
		attestationMapping.addField("token_id", "tokenID");
		
		attestationMapping.setPrimaryKeyField("primaryKey");
		
		//attestationMapping.addField();
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
			importDocument(d);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void importDocument(Document d)
	{
		Element root = d.getDocumentElement();
		List<Element> names = 
				XML.getElementsByTagname(root, "ns:ne", false);
		
		NEDocument document = new NEDocument();
		document.getMetadata(d);
		System.err.println("TITLE: " + document.title);
		
		documentMapping.insertObject(ldb.connection,"documents",  document); 
		
		for (Element n: names)
		{
			String wordform = n.getTextContent(); // getNameText(n);
			String lemma = n.getAttribute("normalizedForm");
			
			
			NELemma neLemma = new NELemma();
			neLemma.lemma = lemma;
			neLemma.neLabel = n.getAttribute("type");
			neLemma = (NELemma) canonical(lemmaMap,neLemma);
			
			
			NEWordform neWord = new NEWordform();
			neWord.wordform = wordform;
			neWord = (NEWordform) canonical(wordformMap,neWord);
			
			NEAnalyzedWordform awf = new NEAnalyzedWordform();
			awf.lemma = neLemma;
			awf.wordform = neWord;
			awf = (NEAnalyzedWordform) canonical(awfMap,awf);
			
			String id = n.getAttribute("xml:id");
			if (id != null && id.length() > 0)
			{
				NEAttestation at = new NEAttestation();
				at.awf = awf;
				at.document = document;
				at.tokenID = id;
				attestationMap.put(at, at);
			}
			//lexicon.addLemma(lemma, PoS, neLabel, gloss);
		}
		
	}
	
	public void flush()
	{
		lemmaMapping.insertObjects(ldb.connection, "lemmata", lemmaMap.keySet());
		wordformMapping.insertObjects(ldb.connection, "wordforms", wordformMap.keySet());
		
		for (Object o: awfMap.keySet())
		{
			NEAnalyzedWordform awf = (NEAnalyzedWordform) o;
			awf.lemmaKey = awf.lemma.primaryKey;
			awf.wordformKey = awf.wordform.primaryKey;
		}
		
		awfMapping.insertObjects(ldb.connection, "analyzed_wordforms", awfMap.keySet());
		
		for (Object o: attestationMap.keySet())
		{
			NEAttestation at = (NEAttestation) o;
			at.documentKey = at.document.primaryKey;
			at.analyzedWordformKey = at.awf.primaryKey;
		}
		
		attestationMapping.insertObjects(ldb.connection, "token_attestations", attestationMap.keySet());
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
