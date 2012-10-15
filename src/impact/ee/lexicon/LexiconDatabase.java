package impact.ee.lexicon;

import impact.ee.lexicon.QuotationCorpus.inSample;
import impact.ee.lexicon.QuotationCorpus.inTraining;
import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;
import impact.ee.util.Database;
import impact.ee.util.Options;
import impact.ee.util.Resource;

import java.io.*;
import java.util.*;
//import java.util.zip.*;

//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;




public class LexiconDatabase extends impact.ee.util.Database  implements Iterable<WordForm>, ILexicon 
{
	//Connection connection = null;
	boolean onlyVerified = false;
	boolean useSimpleWordformsOnly = false;
	boolean dumpWithFrequenciesAndDerivations = true;
	boolean noDerivations = true;
	
	static String createSimpleWordformTableSQL = Resource.getStringFromFile("sql/createSimple.sql");
	static String createViewsSQL = Resource.getStringFromFile("sql/createViews.sql");
	static String prepareLexiconDumpSQL = Resource.getStringFromFile("sql/prepareLexiconDump.sql");  		
	static String createEmptyLexiconSQL = Resource.getStringFromFile("sql/emptyLexiconDatabase.sql");
	
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
	
	public Iterator<WordForm> iterator()
	{
		return new WordFormIterator(this);
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

	
	@Override
	public void addWordform(WordForm w) 
	{
		// TODO Auto-generated method stub
		
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
		Set<WordForm> r = new HashSet<WordForm>();
		String q =
				" select l.*, w.*, a.part_of_speech from lemmata l, simple_analyzed_wordforms a, wordforms w " +
				" where l.lemma_id = a.lemma_id and a.wordform_id = w.wordform_id and w.wordform=?";
		try 
		{
			PreparedStatement stmt = 
					this.connection.prepareStatement(q, ResultSet.TYPE_FORWARD_ONLY,
							ResultSet.CONCUR_READ_ONLY);
			stmt.setBytes(1, wordform.getBytes("UTF-8"));
			ResultSet rs = stmt.executeQuery();
			Database.MapFetcher mf = new Database.MapFetcher(rs);
			Map<String,String> fieldMap = null;
			while ((fieldMap  = mf.fetchMap()) != null) // mis je nu de eerste??
			{
				WordForm wf = new WordForm();
				wf.lemma = fieldMap.get("modern_lemma");
				wf.lemmaPoS = fieldMap.get("lemma_part_of_speech");
				wf.tag = fieldMap.get("part_of_speech");
				wf.wordform = wordform;
				wf.lemmaID = fieldMap.get("persistent_id");
				r.add(wf);
			}
			rs.close();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return r;
	}

	@Override
	public Set<WordForm> searchByModernWordform(String wordform) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public Corpus getQuotationCorpus(inTraining t, inSample s) 
	{
		// TODO Auto-generated method stub
		QuotationCorpus x = new QuotationCorpus(this, t, s);
		return x;
	}

	public void createRandomSelections()
	{
		this.runQuery("create table attestationPortions (attestation_id int, random1 float, random2 float, " +
				" inTrainingSet int, inSampleSet int)");
		this.runQuery("insert into attestationPortions select attestation_id, rand(), rand(), 0, 0 from token_attestations");
		this.runQuery("update attestationPortions set inTrainingSet=1 where random1 < 0.8");
		this.runQuery("update attestationPortions set inSampleSet=1 where random2 < 0.1");
		this.runQuery("alter table attestationPortions add index(attestation_id)");
		this.runQuery("alter table attestationPortions add index(inSampleSet)");
		this.runQuery("alter table attestationPortions add index(inTrainingSet)");
	}
	
	public static void main(String[] args) throws Exception
	{
		//System.err.println(createSimpleWordformTableSQL);
		System.err.println("load database...");
	
		LexiconDatabase l = new LexiconDatabase(args[0], args[1]);
		// l.createRandomSelections();
		// System.exit(0);
		
		if (args.length > 2)
		{
			Set<WordForm> wfs = l.findLemmata(args[2]);
			for (WordForm w: wfs)
			{
				System.out.println(w.lemmaID + " " + w);
			}
			return;
		}
		//l.createSimpleAnalyzedWordformTable();
		
		l.useSimpleWordformsOnly = true;
		l.dumpWithFrequenciesAndDerivations = true;
		System.err.println("database loaded");
		int k=0;
		OutputStreamWriter out = new OutputStreamWriter(System.out,"UTF-8");
		for (WordForm w: l)
		{
			out.write(w.wordform + "\t" + w.lemma + "\t" + w.lemmaPoS + "\t" + w.lemmaPoS + "\t" + w.modernWordform + "\n");
			k++;
		}
		out.flush();
		System.err.println("#wordforms; " + k);
		//java.io.PrintWriter out = new java.io.PrintWriter(System.out);
	}
}
