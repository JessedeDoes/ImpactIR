package impact.ee.lexicon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

class WordFormIterator implements Iterator<WordForm>
{
	/**
	 * 
	 */
	private final LexiconDatabase lexiconDatabase;


	ResultSet rs;
	//String postAdbQuery = "select distinct modern_lemma, wordform, '', '' from AttestatieLexicon.lexiconSplit";
	
	
	String analyzedWordformTable = null; // this.lexiconDatabase.useSimpleWordformsOnly ? "simple_analyzed_wordforms" : "analyzed_wordforms";
	
	String extractWordformsQuery = 
		"select modern_lemma, wordform, lemma_part_of_speech, '' from lemmata, analyzed_wordforms a, wordforms" +
		" where lemmata.lemma_id = a.lemma_id and a.wordform_id = " +
		" wordforms.wordform_id";
	
	String query =  null;//extractWordformsQuery +  (this.lexiconDatabase.onlyVerified? " and verified_by is not null" : ""); // geattesteerd deel voor het historisch lexicon?
	
	PreparedStatement stmt = null;

	private void initializeQuery()
	{
		analyzedWordformTable = this.lexiconDatabase.useSimpleWordformsOnly ? "simple_analyzed_wordforms" : 
			"analyzed_wordforms";
		extractWordformsQuery = 
				"select persistent_id, modern_lemma, wordform, lemma_part_of_speech, '' from lemmata, " 
				+ analyzedWordformTable 
				+ " a, wordforms" +
				" where lemmata.lemma_id = a.lemma_id and a.wordform_id = " +
				" wordforms.wordform_id";
		query = extractWordformsQuery + 
				(this.lexiconDatabase.onlyVerified? " and verified_by is not null" : "");
		if (this.lexiconDatabase.dumpWithFrequenciesAndDerivations)
		{
			this.lexiconDatabase.runQueries(this.lexiconDatabase.prepareLexiconDumpSQL);
			if (this.lexiconDatabase.noDerivations)
			{
				query = "select  persistent_id, modern_lemma, wordform, lemma_part_of_speech, wordform_frequency, lemma_frequency, \"\" " +
						"from wordforms_with_frequency;";
			} else 
			query = "select  persistent_id, modern_lemma, wordform, lemma_part_of_speech, wordform_frequency, lemma_frequency, normalized_form " +
					"from wordforms_with_frequency left join derivations on derivations.analyzed_wordform_id = wordforms_with_frequency.analyzed_wordform_id;";
		}
	}
	
	public WordFormIterator(LexiconDatabase lexiconDatabase)
	{
		this.lexiconDatabase = lexiconDatabase;
		initializeQuery();
		try
		{
			nl.openconvert.log.ConverterLog.defaultLog.println(this.lexiconDatabase.connection);
			stmt = this.lexiconDatabase.connection.prepareStatement(query ,
					ResultSet.TYPE_FORWARD_ONLY, 
					ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery();
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			nl.openconvert.log.ConverterLog.defaultLog.println("piep: exception...");
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
					w.lemmaID  = new String(rs.getBytes(1), "UTF-8");
					w.lemma = new String(rs.getBytes(2), "UTF-8");
					w.wordform = new String(rs.getBytes(3), "UTF-8");
					w.lemmaPoS = new String(rs.getBytes(4), "UTF-8");
					w.tag = w.lemmaPoS;
					if (this.lexiconDatabase.dumpWithFrequenciesAndDerivations)
					{
						String wf = new String(rs.getBytes(5), "UTF-8");
						String lf = new String(rs.getBytes(6), "UTF-8");
						String normalized = new String(rs.getBytes(7), "UTF-8");
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