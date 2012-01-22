package lexicon;

import java.io.*;
import java.util.*;
//import java.util.zip.*;

//import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.SQLException;
//import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.Options;
import util.Resource;


public class LexiconDatabase extends util.Database  implements Iterable<WordForm> 
{
	//Connection connection = null;
	boolean onlyVerified = false;
	boolean useSimpleWordformsOnly = false;
	boolean dumpWithFrequenciesAndDerivations = true;
	
	static String createSimpleWordformTableSQL = Resource.getStringFromFile("sql/createSimple.sql");
	static String createViewsSQL = Resource.getStringFromFile("sql/createViews.sql");
	static String prepareLexiconDumpSQL = Resource.getStringFromFile("sql/prepareLexiconDump.sql");  		
	public LexiconDatabase(Properties props)
	{
		super(props);
	}

	public LexiconDatabase(String dbName)
	{
		this.mysqldbname = dbName;
		String xHost = Options.getOption("databaseHost", mysqlhost);
		mysqlurl = "jdbc:mysql://" + xHost + ":" + mysqlport + "/" + mysqldbname;
		init();
	}
	
	public  LexiconDatabase(String hostName, String dbName)
	{
		this.mysqlhost = hostName;
		this.mysqldbname = dbName;
		mysqlurl = "jdbc:mysql://" + mysqlhost + ":" + mysqlport + "/" + mysqldbname;
		init();
	}
	
	public LexiconDatabase()
	{
		init();
	}

	public void init()
	{
	
		try 
		{
			connection = (new ConnectorSimple()).connect(mysqlurl, mysqluser, mysqlpasswd);
			System.err.println("Connection: " + this.connection);
		} catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(1);
		}
	}

	public void createSimpleAnalyzedWordformTable()
	{
		String[] parts = createSimpleWordformTableSQL.split(";");
		for (String q: parts)
		{
			System.err.println(q);
			this.runQuery(q);
		}
	}
	
	public void createViews()
	{
		String[] parts = createViewsSQL.split(";");
		for (String q: parts)
		{
			q = q.replace("EE3_5", this.mysqldbname);
			System.err.println("<QUERY>\n" +  q + "\n</QUERY>");
			this.runQuery(q);
		}
	}
	
	class WordFormIterator implements Iterator<WordForm>
	{
		ResultSet rs;
		//String postAdbQuery = "select distinct modern_lemma, wordform, '', '' from AttestatieLexicon.lexiconSplit";
		
		
		String analyzedWordformTable = useSimpleWordformsOnly ? "simple_analyzed_wordforms" : 
			"analyzed_wordforms";
		
		String extractWordformsQuery = 
			"select modern_lemma, wordform, lemma_part_of_speech, '' from lemmata, analyzed_wordforms a, wordforms" +
			" where lemmata.lemma_id = a.lemma_id and a.wordform_id = " +
			" wordforms.wordform_id";
		
		String query = extractWordformsQuery + 
				(onlyVerified? " and verified_by is not null" : ""); // geattesteerd deel voor het historisch lexicon?
		
		PreparedStatement stmt = null;


		private void initializeQuery()
		{
			analyzedWordformTable = useSimpleWordformsOnly ? "simple_analyzed_wordforms" : 
				"analyzed_wordforms";
			extractWordformsQuery = 
					"select modern_lemma, wordform, lemma_part_of_speech, '' from lemmata, " 
					+ analyzedWordformTable 
					+ " a, wordforms" +
					" where lemmata.lemma_id = a.lemma_id and a.wordform_id = " +
					" wordforms.wordform_id";
			query = extractWordformsQuery + 
					(onlyVerified? " and verified_by is not null" : "");
			if (LexiconDatabase.this.dumpWithFrequenciesAndDerivations)
			{
				LexiconDatabase.this.runQueries(LexiconDatabase.this.prepareLexiconDumpSQL);
				query = "select  modern_lemma, wordform, lemma_part_of_speech, wordform_frequency, lemma_frequency, normalized_form " +
						"from wordforms_with_frequency left join derivations on derivations.analyzed_wordform_id = wordforms_with_frequency.analyzed_wordform_id;";
			}
		}
		
		public WordFormIterator()
		{
			initializeQuery();
			try
			{
				System.err.println(connection);
				stmt = LexiconDatabase.this.connection.prepareStatement(query ,
						ResultSet.TYPE_FORWARD_ONLY, 
						ResultSet.CONCUR_READ_ONLY);
				rs = stmt.executeQuery();
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				System.err.println("piep: exception...");
				e.printStackTrace();
				
				
			}
		}
		
		@Override
		public boolean hasNext()
		{
			// TODO Auto-generated method stub
			try
			{
				return (!rs.isLast());
			} catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		@Override
		public WordForm next()
		{
			try
			{
				if (rs.next())
				{
					//int nofcolumns = rs.getMetaData().getColumnCount();
					WordForm w = new WordForm();
					try
					{
						w.lemma = new String(rs.getBytes(1), "UTF-8");
						w.wordform = new String(rs.getBytes(2), "UTF-8");
						w.lemmaPoS = new String(rs.getBytes(3), "UTF-8");
						w.tag = w.lemmaPoS;
						if (LexiconDatabase.this.dumpWithFrequenciesAndDerivations)
						{
							String wf = new String(rs.getBytes(4), "UTF-8");
							String lf = new String(rs.getBytes(5), "UTF-8");
							String normalized = new String(rs.getBytes(6), "UTF-8");
							w.wordformFrequency = new Integer(wf);
							w.lemmaFrequency = new Integer(lf);
							w.modernWordform = normalized;
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					return w;
				}
			} catch (SQLException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void remove()
		{
			try
			{
				stmt.close();
			} catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public Iterator<WordForm> iterator()
	{
		return new WordFormIterator();
	}

	public InMemoryLexicon createInMemoryLexicon()
	{
		InMemoryLexicon l = new InMemoryLexicon();
		Iterator<WordForm> i = iterator();
		while (i.hasNext())
		{
			WordForm w = i.next();
			l.addWordform(w);
		}
		return l;
	}

	public static void main(String[] args) throws Exception
	{
		//System.err.println(createSimpleWordformTableSQL);
		System.err.println("load database...");
	
		LexiconDatabase l = new LexiconDatabase(args[0], args[1]);
		//l.createSimpleAnalyzedWordformTable();
		l.useSimpleWordformsOnly = true;
		l.dumpWithFrequenciesAndDerivations = true;
		System.err.println("database loaded");
		int k=0;
		OutputStreamWriter out = new OutputStreamWriter(System.out,"UTF-8");
		for (WordForm w: l)
		{
			out.write(w.wordform + "\t" + w.lemma + "\t" + w.lemmaPoS + "\t" + w.lemmaPoS + "\n");
			k++;
		}
		out.flush();
		System.err.println("#wordforms; " + k);
		//java.io.PrintWriter out = new java.io.PrintWriter(System.out);
	}
}
