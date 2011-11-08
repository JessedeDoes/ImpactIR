package lemmatizer;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import lemmatizer.Lexicon.WordForm;

public class LexiconDatabase extends util.Database  implements Iterable<WordForm> 
{
	Connection connection = null;

	public LexiconDatabase(Properties props)
	{
		super(props);
	}

	public LexiconDatabase(String dbName)
	{
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
			this.connection = (new ConnectorSimple()).connect(mysqlurl, mysqluser, mysqlpasswd);
			System.err.println("Connection: " + this.connection);
		} catch (Exception e)
		{
			e.printStackTrace();
			//System.exit(1);
		}
	}

	class WordFormIterator implements Iterator<Lexicon.WordForm>
	{
		ResultSet rs;
		String postAdbQuery = "select distinct modern_lemma, wordform, '', '' from AttestatieLexicon.lexiconSplit";
		String ldbQuery = 
			"select modern_lemma, wordform, lemma_part_of_speech, '' from lemmata, a2 a, wordforms" +
			" where lemmata.lemma_id = a.lemma_id and a.wordform_id = " +
			" wordforms.wordform_id";
		String adbQuery = "";
		String query = ldbQuery + " and verified_by is not null"; // geattesteerd deel voor het historisch lexicon?
		
		PreparedStatement stmt = null;


		public WordFormIterator()
		{
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
					int nofcolumns = rs.getMetaData().getColumnCount();
					WordForm w = new Lexicon.WordForm();
					try
					{
						w.lemma = new String(rs.getBytes(1), "UTF-8");
						w.wordform = new String(rs.getBytes(2), "UTF-8");
						w.lemmaPoS =  new String(rs.getBytes(3), "UTF-8");
						w.tag = w.lemmaPoS;
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

	public Iterator<Lexicon.WordForm> iterator()
	{
		return new WordFormIterator();
	}

	public Lexicon createInMemoryLexicon()
	{
		Lexicon l = new Lexicon();
		Iterator<Lexicon.WordForm> i = iterator();
		while (i.hasNext())
		{
			WordForm w = i.next();
			l.addWordform(w);
		}
		return l;
	}

	public static void main(String[] args) throws Exception
	{
		System.err.println("load database...");
		LexiconDatabase l = new LexiconDatabase(args[0]);
		System.err.println("database loaded");
		int k=0;
		OutputStreamWriter out = new OutputStreamWriter(System.out,"UTF-8");
		for (WordForm w: l)
		{
			out.write(w.wordform + "\t" + w.lemma + "\t" + w.lemmaPoS + "\t" + w.lemmaPoS + "\n");
			k++;
		}
		System.err.println("#wordforms; " + k);
		//java.io.PrintWriter out = new java.io.PrintWriter(System.out);
	}
}
