package impact.ee.lexicon.database;

import impact.ee.lexicon.LexiconDatabase;
import impact.ee.lexicon.WordForm;

import java.util.*;
public class TestORM 
{
	public static List<Object> testRead()
	{
		LexiconDatabase ldb = new LexiconDatabase("impactdb", "EE3_5");
		String table = "lemmata";
		ObjectRelationalMapping orm = 
				new ObjectRelationalMapping(WordForm.class, "analyzed_wordforms");
		
		orm.addField("modern_lemma", "lemma");
		orm.addField("wordform", "wordform");
		orm.addField("lemma_part_of_speech", "lemmaPoS");
		orm.addField("persistent_id", "lemmaID");
		
		List<Object> objects = 
			orm.fetchObjects(ldb.connection, 
					"select * from lemmata l, simple_analyzed_wordforms a, wordforms w where modern_lemma like 'a%' " +
			" and a.wordform_id = w.wordform_id and l.lemma_id = a.lemma_id limit 30");
		
		for (Object o: objects)
		{
			//System.out.println(o);
		}
		return objects;
	}
	
	public static void testWrite()
	{
		LexiconDatabase ldb = new LexiconDatabase("impactdb", "ORMTEST");
		String table = "lemmata";
		
		ObjectRelationalMapping orm = 
				new ObjectRelationalMapping(WordForm.class, "lemmata");
		orm.addField("modern_lemma", "lemma");
		//orm.addField("wordform", "wordform");
		orm.addField("lemma_part_of_speech", "lemmaPoS");
		orm.addField("persistent_id", "lemmaID");
		orm.setPrimaryKeyField("primaryKey");
		WordForm w = new WordForm();
		
		w.lemma = "ondersteboven";
		w.lemmaPoS = "ADP";
		w.lemmaID="M000000";
		
		WordForm w1 = new WordForm();
		w1.lemma = "boven";
		w1.lemmaPoS = "ADP";
		w1.lemmaID = "M000001";
		
		List<Object> wl  = new ArrayList<Object>();
		
		wl.add(w);
		wl.add(w1);
		
		orm.insertObjects(ldb.connection, "lemmata", wl);
		
		for (Object o: wl)
		{
			try
			{
				System.err.println(orm.primaryKeyField.get(o) + " --> " + o);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		//List<Object> nogwat = testRead();
		//orm.insertObjects(ldb.connection, "lemmata", nogwat);
	
	}
	
	public static void main(String[] args)
	{
		testWrite();
	}
}
