package impact.ee.lexicon;
import org.mapdb.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;

public class MapDBLexicon implements ILexicon
{
	DB db;
	private ConcurrentNavigableMap<String, Set<WordForm>> lemma2forms;
	public ConcurrentNavigableMap<String, Set<WordForm>> form2lemmata;
	
	public MapDBLexicon(String fileName)
	{
		 db = DBMaker.newFileDB(new File(fileName))
	               .closeOnJvmShutdown()
	               .encryptionEnable("password")
	               .make();
		 lemma2forms = db.getTreeMap("lemma2forms");
		 form2lemmata = db.getTreeMap("form2lemma");
	}
	
	public void someStuff(String dbName)
	{
	    db = DBMaker.newFileDB(new File("testdb"))
	               .closeOnJvmShutdown()
	               .encryptionEnable("password")
	               .make();
	  
	    ConcurrentNavigableMap<String, WordForm> map = db.getTreeMap("collectionName");
	    for (String x : map.keySet())
	    {
	    	System.err.println(map.get(x));
	    }
	    WordForm w = new WordForm();
	    w.wordform = "apen";
	    w.lemma = "aap";
	    w.lemmaPoS  = "znw";
	    for (int i=0; i < 1000; i++)
	    {
	    	map.put("apen" + i, w);
	    	map.put("aap" + i, w);
	    }
	    db.commit();
	    db.close();
	}
	

	public void readFromFile(String fileName)
	{
		System.err.println("reading lexicon from: " + fileName);
		if (fileName.startsWith("database:"))
		{
			String dbName = fileName.substring("database:".length());
			LexiconDatabase ldb = new LexiconDatabase(dbName);
			for (WordForm w: ldb)
			{
				this.addWordform(w);
			}
			return;
		}
		String s="";
		try
		{
			Reader reader = new InputStreamReader(new FileInputStream(fileName), "UTF-8");
			BufferedReader b = new BufferedReader(reader) ; // UTF?
			
			while ( (s = b.readLine()) != null) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
			{
				// System.err.println(s);
				WordForm w = LexiconUtils.getWordformFromLine(s);
				if (w == null || w.wordform.indexOf(" ") >= 0 || w.lemma.indexOf(" ") >= 0) // temporary hack: no spaces
					continue;
				addWordform(w);
			}
		} catch (Exception e)
		{
			System.err.println("s = " + s);
			e.printStackTrace();
		}
		db.commit();
	}
	
	private void addToIndexes(WordForm w)
	{
		if (w == null)
			return;
		Set<WordForm> wfz = lemma2forms.get(w.lemma);
		if (wfz == null)
			lemma2forms.put(w.lemma, (wfz =new 	HashSet<WordForm>()));

		wfz.add(w);
		
		Set<WordForm> lemz = form2lemmata.get(w.wordform.toLowerCase());
		if (lemz == null)
			form2lemmata.put(w.wordform.toLowerCase(), (lemz =new HashSet<WordForm>()));
		lemz.add(w);
		
		if (!w.wordform.toLowerCase().equals(w.wordform))
		{
			Set<WordForm> lemzz = form2lemmata.get(w.wordform);
			if (lemzz == null)
				form2lemmata.put(w.wordform, (lemzz =new HashSet<WordForm>()));
			lemzz.add(w);
		}
	}
	
	


	@Override
	public void addWordform(WordForm w) 
	{
		// TODO Auto-generated method stub
		this.addToIndexes(w);
	}


	@Override
	public Set<WordForm> findForms(String lemma, String tag) 
	{
		// TODO Auto-generated method stub
		Set<WordForm> V = this.lemma2forms.get(lemma);
		if (tag == null)
			return V;
		Set<WordForm> V1 = new HashSet<WordForm>();
		for (WordForm w: V)
		{
			if (w.tag.equals(tag))
				V1.add(w);
		}
		return V1;
	}


	@Override
	public Set<WordForm> findLemmata(String wordform) 
	{
		// TODO Auto-generated method stub
		return this.form2lemmata.get(wordform);
	}


	@Override
	public Set<WordForm> searchByModernWordform(String wordform) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Iterator<WordForm> iterator() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args)
	{
		MapDBLexicon l = new MapDBLexicon("molex.testdb");
		//l.readFromFile("resources/exampledata/molexDump.txt");
		InMemoryLexicon iml = new InMemoryLexicon();
		iml.readFromFile("resources/exampledata/molexDump.txt");
	
		Set<String> lookmeup = new HashSet<String>();
		for (WordForm w: iml)
		{
			if (w.lemma.contains("s"))
			{
				lookmeup.add(w.lemma);
			}
		}
		System.err.println("done reading lexicon!");
		long st = System.currentTimeMillis();
		int N=0;
		for (String lem: lookmeup)
		{
			{
				Set<WordForm> x = l.findForms(lem, null);
				N++;
				//System.out.println(x);
			}
		}
		long et = System.currentTimeMillis();
		long d = et -st;
		double tpa = d / (double) N;
		System.err.println("N=" + N + " d = " + d + " tpa = " + tpa);
		
	}
}
