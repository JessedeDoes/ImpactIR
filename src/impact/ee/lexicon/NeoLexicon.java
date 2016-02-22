package impact.ee.lexicon;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.kernel.EmbeddedGraphDatabase;


/**
 * 
 * @author jesse
 * This was not a very practical idea.
 * Lexica are huge and not very fast in this way
 */
public class NeoLexicon implements ILexicon,   Iterable<WordForm>
{
	
	
	static int NODETYPE_WORDFORM = 0;
	static int NODETYPE_LEMMA = 1;	
	boolean caseSensitiveLookup=false;	
	
	private static enum RelTypes implements RelationshipType
	{
		KNOWS,
		LEMMA_WORDFORM,
		LEMMA_WORDFORM_CI
	};
	
	private  String DB_PATH = "c:/Temp/NeoTest";
	GraphDatabaseService graphDb = null;
	private Index<Node> nodeIndex = null;

	public void close()
	{
		nl.openconvert.log.ConverterLog.defaultLog.println("Yes!!! finalize is called. Great!");
		this.graphDb.shutdown(); // this might help??
	}
	
	public NeoLexicon(String dbPath, boolean createNew)
	{
		DB_PATH=dbPath;
		if (createNew)
		{
			setup();
		}
		graphDb = new EmbeddedGraphDatabase( DB_PATH );
		registerShutdownHook(graphDb);
		nodeIndex = graphDb.index().forNodes( "nodes" );
	}

	public void destroy()
	{
		nl.openconvert.log.ConverterLog.defaultLog.println("Shutting down " + DB_PATH);
		this.graphDb.shutdown();
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		} );
	}

	/**
	 * Make sure the DB directory doesn't exist.
	 */

	public void setup()
	{
		deleteRecursively( new File( DB_PATH ) );
	}

	private static void deleteRecursively( File file )
	{
		if ( !file.exists() )
		{
			return;
		}

		if ( file.isDirectory() )
		{
			for ( File child : file.listFiles() )
			{
				deleteRecursively( child );
			}
		}
		if ( !file.delete() )
		{
			throw new RuntimeException(
					"Couldn't empty database. Offending file:" + file );
		}
	}

	
/**
 * 
 * @param fileName
 */
	public void readWordsFromFile(String fileName)
	{
		if (fileName.startsWith("database:"))
		{
			this.slurpDB(fileName.substring("database:".length()));
			return;
		}
		int nItems=0;
		Transaction tx = graphDb.beginTx();
		try
		{
			Reader reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			BufferedReader b = new BufferedReader(reader) ; // UTF?

			String s;
			while ( (s = b.readLine()) != null) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
			{
				// nl.openconvert.log.ConverterLog.defaultLog.println(s);
				WordForm w = LexiconUtils.getWordformFromLine(s);
				if (w.wordform.indexOf(" ") >= 0 || w.lemma.indexOf(" ") >= 0) // temporary hack: no spaces
					continue;
				addWordform(w);
				nItems++;
				if (nItems % 50000 == 0)
				{
					nl.openconvert.log.ConverterLog.defaultLog.println("new transaction... " + nItems);
					tx.success();
					tx.finish();
					tx =  graphDb.beginTx();
				}
			}
			tx.success();
		} catch (Exception e)
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println("s = " + s);
			e.printStackTrace();
		}
		tx.finish();
	}
	
	public void slurpDB(String dbName)
	{
		int nItems=0;
		Transaction tx = graphDb.beginTx();
		try
		{
			LexiconDatabase l = new LexiconDatabase(dbName);
			l.useSimpleWordformsOnly = true;
			for (WordForm w:l)
			{
				nl.openconvert.log.ConverterLog.defaultLog.println(nItems + ": " + w);
				addWordform(w);
				nItems++;
				if (nItems % 50000 == 0)
				{
					nl.openconvert.log.ConverterLog.defaultLog.println("new transaction...");
					tx.success();
					tx.finish();
					
					tx =  graphDb.beginTx();
				}
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
		nl.openconvert.log.ConverterLog.defaultLog.println("items added: "  + nItems);
		//graphDb.shutdown();
	}


	public void dumpDB()
	{
		for (Node n: graphDb.getAllNodes())
		{
			try
			{
				String wf = (String) getProperty(n, "wordform");
				// nl.openconvert.log.ConverterLog.defaultLog.println(wf);
				if (wf != null)
				{
					impact.ee.lexicon.WordForm w = getWordFormFromNode(n);
					System.out.println(w);
					System.out.flush();
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	@Override
	public void addWordform(WordForm w) 
	{
		try
		{
			Node wordformNode = graphDb.createNode();
			wordformNode.setProperty("type", NODETYPE_WORDFORM);
			
			Node lemmaNode = findLemmaNode(w.lemma, w.lemmaPoS, w.lemmaID);
			
			if (lemmaNode == null)
			{
				lemmaNode = this.addLemma(w.lemma, w.lemmaPoS);
			}
			
			wordformNode.createRelationshipTo( lemmaNode, RelTypes.LEMMA_WORDFORM);

			wordformNode.setProperty( "wordform", w.wordform);
			wordformNode.setProperty( "tag", w.tag);
			wordformNode.setProperty( "lemmaID" , w.lemmaID);
			
			if (w.modernWordform != null)
			{
				wordformNode.setProperty("modernWordform", w.modernWordform);
				nodeIndex.add(wordformNode,"modernWordform", w.modernWordform);
			}
			wordformNode.setProperty("wordformFrequency", w.wordformFrequency);
			wordformNode.setProperty("lemmaFrequency", w.lemmaFrequency);
			nodeIndex.add(wordformNode, "wordform", w.wordform);
			nodeIndex.add(wordformNode, "wordformLowercase",  w.wordform.toLowerCase());
		}
		finally
		{

		}
	}

	public Node addLemma(String lemma, String lemmaPoS)
	{
		//
		Node lemmaNode = graphDb.createNode();
		lemmaNode.setProperty("lemma", lemma);
		lemmaNode.setProperty("lemma_lowercase", lemma.toLowerCase());
		lemmaNode.setProperty("lemmaPoS", lemmaPoS);
		lemmaNode.setProperty("type", NODETYPE_LEMMA);
		nodeIndex.add(lemmaNode, "lemma", lemma);
		nodeIndex.add(lemmaNode, "lemmaLowercase",  lemma.toLowerCase());
		return lemmaNode;
	}

	private Node findLemmaNode(String lemma, String lemmaPoS, String lemmaID) 
	{
		// TODO Auto-generated method stub
		if (lemmaID != null && lemmaID.length() > 0)
		{
			IndexHits<Node> hits = nodeIndex.get("lemmaID", lemmaID);
			if (hits.size() > 0)
				return hits.getSingle();
			return null;
		}
		IndexHits<Node> hits = nodeIndex.get("lemma", lemma);
		if (hits.size() > 0)
			return hits.getSingle();
		return null;
	}

	private Set<Node> findWordFormNodesByLemma(String lemmaQuery)
	{
		//nl.openconvert.log.ConverterLog.defaultLog.println("start query for:" + lemmaQuery);
		Set<Node> nodes = new HashSet<Node>();
		
		IndexHits<Node> hits = nodeIndex.get("lemmaLowercase", lemmaQuery.toLowerCase());
		
		for (Node n: hits)
		{
			//String lemma = (String) n.getProperty("lemma");
			for (Relationship r: n.getRelationships())
			{
				Node wordformNode = r.getOtherNode(n);
				// String wordform = (String) wordformNode.getProperty("wordform");
				//nl.openconvert.log.ConverterLog.defaultLog.println(wordform + "\t" + lemma);
				nodes.add(wordformNode);
			}
		}
		//nl.openconvert.log.ConverterLog.defaultLog.println("found items: "  + nodes.size());
		return nodes;
	}
	
	@Override
	public Set<WordForm> findForms(String lemma, String tag) 
	{
		
		Set<WordForm> s0 = findWordFormsByLemma(lemma);
		Set<WordForm> s1 = new HashSet<WordForm>();
		
		for (WordForm w: s0)
		{
			if (tag.equals("*") || w.lemmaPoS.equalsIgnoreCase(tag))
				s1.add(w);
		}
				
		return s1;
	}

	@Override
  	public Set<WordForm> findLemmata(String wordform)
	{
		return findLemmata(wordform,false);
	}

	@Override
	public Set<WordForm> searchByModernWordform(String wordform) 
	{
		Set<WordForm> wordforms = new HashSet<WordForm>();
		if (wordform == null || wordform.length() == 0)
			return wordforms;
		String property = "modernWordform";
		try
		{
			IndexHits<Node> hits =
				nodeIndex.get(property, wordform);
			for (Node n: hits)
			{
				WordForm w = this.getWordFormFromNode(n);
				if (w != null)
					wordforms.add(w);
			}
		} catch (Exception e)
		{

		}
		//nl.openconvert.log.ConverterLog.defaultLog.println("found items: "  + wordforms.size());
		return wordforms;
	}
	
	public void lookupWord(String word)
	{
		Set<WordForm> lookup1 = findLemmata(word,true);
		Set<WordForm> s = findWordFormsByLemma(word);
		System.out.println("as wordform: ");
		System.out.println("\t" + lookup1);
		System.out.println("as lemma: ");
                System.out.println("\t" + s);
	}


	public Set<WordForm> findWordFormsByLemma(String word) 
	{
		Set<Node> lookup2 = findWordFormNodesByLemma(word);
		Set<WordForm> s = new HashSet<WordForm>();
		for (Node n: lookup2) s.add(getWordFormFromNode(n));
		return s;
	}

	public Set<WordForm> findLemmata(String wordform, boolean query) 
	{
		Set<WordForm> wordforms = new HashSet<WordForm>();
		String property = caseSensitiveLookup?"wordform":"wordformLowercase";
		try
		{
			IndexHits<Node> hits = query? 
				nodeIndex.query(property, wordform) : 
				nodeIndex.get(property, wordform);
			for (Node n: hits)
			{
				WordForm w = this.getWordFormFromNode(n);
				if (w != null)
					wordforms.add(w);
			}
		} catch (Exception e)
		{

		}
		//nl.openconvert.log.ConverterLog.defaultLog.println("found items: "  + wordforms.size());
		return wordforms;
	}

	private Object getProperty(Node n, String propertyName)
	{
		try
		{
			return n.getProperty(propertyName);
		} catch (Exception e)
		{
			return null;
		}
	}



	public void lookupLemmataFromFile(String fileName)
	{
		Reader reader;
		//int s = System.ge
		try
		{
			reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			BufferedReader input = new BufferedReader(reader);
			String s;
			while ((s = input.readLine()) != null)
			{
				s = s.split("\\s+")[0];
				// nl.openconvert.log.ConverterLog.defaultLog.println(s);
				Set<WordForm> ws = findLemmata(s);
				for (WordForm w: ws)
					System.out.println(w);
			}
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public WordForm getWordFormFromNode(Node x)
	{
		try
		{
			int type = (Integer) x.getProperty("type");
			if (type == NODETYPE_WORDFORM)
			{
				WordForm w = new WordForm();
				w.lemmaID = (String) x.getProperty("lemmaID");
				w.wordform = (String) x.getProperty("wordform");
				w.tag = (String) x.getProperty("tag");
				String modernForm = (String) getProperty(x, "modernWordform");
				Integer lemmaFrequency = (Integer) getProperty(x, "lemmaFrequency");
				Integer wordformFrequency = (Integer) getProperty(x, "wordformFrequency");
				w.modernWordform = (modernForm != null) ? modernForm : "";
				w.lemmaFrequency = (lemmaFrequency != null) ? lemmaFrequency : 0;
				w.wordformFrequency = (wordformFrequency != null) ? wordformFrequency: 0;
				
				// and retrieve lemma...
				for (Relationship r: x.getRelationships())
				{
					Node l = r.getOtherNode(x);
					w.lemma = (String) l.getProperty("lemma");
					w.lemmaPoS = (String) l.getProperty("lemmaPoS");
				}
				return w;
			} else
			{
				//nl.openconvert.log.ConverterLog.defaultLog.println("Boe! Non wordform node retrieved by wordform query?? : " + 
				//			(String) x.getProperty("wordform"));
				return null;
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	class WordformIterator implements Iterator<WordForm>
	{
		Iterator<Node> nodeIterator;
		Node currentNode;
		WordForm nextWord = null;
		
		public WordformIterator()
		{
			nodeIterator = graphDb.getAllNodes().iterator();
		}
		
		public boolean hasNext()
		{
			if (nextWord != null)
				return true;
			if (!nodeIterator.hasNext())
				return false;
			while (true)
			{
				if (nodeIterator.hasNext())
				{
					currentNode = nodeIterator.next();
					//nl.openconvert.log.ConverterLog.defaultLog.println(currentNode);
					nextWord = getWordFormFromNode(currentNode);
					if (nextWord != null)
						return true;
						
				} else
				{
					return false;
				}
			}
		}
		
		public WordForm next()
		{
		    if (hasNext())
		    {
		    	WordForm n = nextWord;
		    	nextWord = null;
		    	return n;
		    } else
		    	return null;
		}

		@Override
		public void remove() 
		{
			// TODO Auto-generated method stub
			
		}
	}
	@Override
	public Iterator<WordForm> iterator() 
	{
		return new WordformIterator();
	}

	void clearDatabase() 
	{
		Transaction tx;
		tx = graphDb.beginTx();
		try
		{
			for ( Node node : graphDb.getAllNodes() )
			{
				for ( Relationship rel : node.getRelationships() )
				{
					rel.delete();
				}
				node.delete();
			}
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	public static void main(String[] args)
	{
		boolean create = true;
		String arg0 = null;
		String arg1 = null;
		String arg2 = null;
		
		if (args.length < 1) arg0 = "c:/Temp/NeoTest"; else arg0 = args[0];
		if (args.length < 2) arg1 = "EE3_5"; else arg1 = args[1];
		if (args.length < 3) arg2 = "aard*"; else arg2 = args[2];
		
		if (args.length >= 3)
			create = false;
		
		if (create)
		{
			NeoLexicon l = new NeoLexicon(arg0, true);
			l.readWordsFromFile(arg1);
			int k=0;
			for (WordForm w: l)
			{
				nl.openconvert.log.ConverterLog.defaultLog.println(k++ + "= " + w);
			}
		} else
		{
			NeoLexicon l = new NeoLexicon(arg0, false);
			//l.dumpDB();
			for (WordForm w: l.findLemmata(arg2))
			{
				System.out.println(w);
			}
		}
	}



}
