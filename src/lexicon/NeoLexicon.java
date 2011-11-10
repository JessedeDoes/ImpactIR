package lexicon;


import java.io.File;
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
import org.neo4j.kernel.impl.annotations.Documented;

public class NeoLexicon implements ILexicon
{
	// START SNIPPET: createReltype
	private static enum RelTypes implements RelationshipType
	{
		KNOWS,
		LEMMA_WORDFORM,
		LEMMA_WORDFORM_CI
	}
	// END SNIPPET: createReltype

	private  String DB_PATH = "c:/Temp/NeoTest";
	GraphDatabaseService graphDb = null;
	private Index<Node> nodeIndex = null;


	public NeoLexicon(String dbPath, boolean createNew)
	{
		DB_PATH=dbPath;
		if (createNew)
		{
			setup();
		}
		graphDb = new EmbeddedGraphDatabase( DB_PATH );
		registerShutdownHook( graphDb );
		nodeIndex = graphDb.index().forNodes( "nodes" );
	}

	public void helloWorldExample()
	{

		// START SNIPPET: startDb
		//setup();
		//graphDb = new EmbeddedGraphDatabase( DB_PATH );

		registerShutdownHook( graphDb );
		// END SNIPPET: startDb


		clearDatabase();

		System.out.println( "Shutting down database ..." );
		// START SNIPPET: shutdownServer
		graphDb.shutdown();
		// END SNIPPET: shutdownServer
	}

	private void clearDatabase() 
	{
		Transaction tx;
		tx = graphDb.beginTx();
		try
		{
			// START SNIPPET: removingData
			// let's remove the data
			for ( Node node : graphDb.getAllNodes() )
			{
				for ( Relationship rel : node.getRelationships() )
				{
					rel.delete();
				}
				node.delete();
			}
			// END SNIPPET: removingData
			tx.success();
		}
		finally
		{
			tx.finish();
		}
	}

	// START SNIPPET: shutdownHook
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running example before it's completed)
		Runtime.getRuntime()
		.addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				graphDb.shutdown();
			}
		} );
	}
	// END SNIPPET: shutdownHook

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


	public void slurpDB(String dbName)
	{
		int nItems=0;
		Transaction tx = graphDb.beginTx();
		try
		{
			LexiconDatabase l = new LexiconDatabase(dbName);
			for (WordForm w:l)
			{
				System.err.println(nItems + ": " + w);
				addWordform(w);
				nItems++;
				if (nItems % 10000 == 0)
				{
					System.err.println("new transaction...");
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
		System.err.println("items added: "  + nItems);
		graphDb.shutdown();
	}


	public void dumpDB()
	{
		for (Node n: graphDb.getAllNodes())
		{
			try
			{
				String wf = (String) n.getProperty("wordform");
				//System.err.println(wf);
				for (Relationship r: n.getRelationships())
				{
					Node lemmaNode = r.getOtherNode(n);
					String lemma = (String) lemmaNode.getProperty("lemma");
					System.err.println(wf + "\t" + lemma);
				}
			} catch (Exception e)
			{
				//e.printStackTrace();
			}
		}
	}
	@Override

	public void addWordform(WordForm w) 
	{
		// faster if the transaction is on  a set of wordforms?
		try
		{
			Node wordformNode = graphDb.createNode();
			Node lemmaNode = findLemmaNode(w.lemma,w.lemmaPoS);

			if (lemmaNode == null)
			{
				lemmaNode = this.addLemma(w.lemma, w.lemmaPoS);
			}
			Relationship relationship = 
					wordformNode.createRelationshipTo( lemmaNode, RelTypes.LEMMA_WORDFORM);

			wordformNode.setProperty( "wordform", w.wordform);
			wordformNode.setProperty( "tag", w.tag);
			
			nodeIndex.add(wordformNode, "wordform", w.wordform);
			nodeIndex.add(wordformNode, "wordformLowercase",  w.wordform.toLowerCase());
			
			// relationship.setProperty( "message", "brave Neo4j " );
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
		nodeIndex.add(lemmaNode, "lemma", lemma);
		nodeIndex.add(lemmaNode, "lemmaLowercase",  lemma.toLowerCase());
		return lemmaNode;
	}

	private Node findLemmaNode(String lemma, String lemmaPoS) 
	{
		// TODO Auto-generated method stub
		IndexHits<Node> hits = nodeIndex.get("lemma", lemma);
		if (hits.size() > 0)
			return hits.getSingle();
		return null;
	}

	private Set<Node> findWordFormsByLemma(String lemmaQuery)
	{
		System.err.println("start query for:" + lemmaQuery);
		Set<Node> nodes = new HashSet<Node>();
		
		IndexHits<Node> hits = nodeIndex.query("lemma", lemmaQuery);
		for (Node n: hits)
		{
			String lemma = (String) n.getProperty("lemma");
			for (Relationship r: n.getRelationships())
			{
				Node wordformNode = r.getOtherNode(n);
				String wordform = (String) wordformNode.getProperty("wordform");
				System.err.println(wordform + "\t" + lemma);
				nodes.add(wordformNode);
			}
		}
		System.err.println("found items: "  + nodes.size());
		return nodes;
    }
	
	@Override
	public Set<WordForm> findForms(String lemma, String tag) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<WordForm> findLemmata(String wordform) 
	{
		// TODO Auto-generated method stub
		System.err.println("start query for:" + wordform);
		Set<WordForm> wordforms = new HashSet<WordForm>();
		
		IndexHits<Node> hits = nodeIndex.query("wordform", wordform);
		for (Node n: hits)
		{
			String wf = (String) n.getProperty("wordform");
			for (Relationship r: n.getRelationships())
			{
				Node wordformNode = r.getOtherNode(n);
				String lemma = (String) wordformNode.getProperty("lemma");
				String pos = (String) wordformNode.getProperty("lemmaPoS");
				System.err.println(wf + "\t" + lemma);
				WordForm w = new WordForm();
				w.wordform = wf;
				w.lemma = lemma;
				w.lemmaPoS = pos;
				wordforms.add(w);
			}
		}
		System.err.println("found items: "  + wordforms.size());
		return wordforms;
	}

	@Override
	public Iterator<WordForm> iterator() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args)
	{
		boolean create = false;
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
			l.slurpDB(arg1);
		} else
		{
			NeoLexicon l = new NeoLexicon(arg0, false);
			//l.dumpDB();
			l.findLemmata(arg2);
		}
	}
}