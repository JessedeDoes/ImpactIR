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
	    	nl.openconvert.log.ConverterLog.defaultLog.println(map.get(x));
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
		nl.openconvert.log.ConverterLog.defaultLog.println("reading lexicon from: " + fileName);
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
				// nl.openconvert.log.ConverterLog.defaultLog.println(s);
				WordForm w = LexiconUtils.getWordformFromLine(s);
				if (w == null || w.wordform.indexOf(" ") >= 0 || w.lemma.indexOf(" ") >= 0) // temporary hack: no spaces
					continue;
				addWordform(w);
			}
		} catch (Exception e)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("s = " + s);
			e.printStackTrace();
		}
		db.commit();
	}
	
	/** 
	 * Take heed: mapdb cannot automatically update a changed object (the set of words here)
	 * So we have to force the refresh vy first removing and then re-inserting
	 * @param w
	 */
	
	private void addToIndexes(WordForm w)
	{
		if (w == null)
			return;
		
		// add to lemma index (niet goed genoeg)...
		
		Set<WordForm> wfz = lemma2forms.get(w.lemma);
		if (wfz == null)
			wfz =new HashSet<WordForm>();
		else
			lemma2forms.remove(w.lemma);
		wfz.add(w);
		lemma2forms.put(w.lemma, wfz);
		
		
		// add to wordform index
		String wkey = w.wordform.toLowerCase();
		Set<WordForm> lemz = form2lemmata.get(wkey);
		if (lemz == null)
			lemz =new HashSet<WordForm>();
		else
			form2lemmata.remove(wkey);	// needed for update???
			

		if (lemz.contains(w))
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println("I already had this: " + w);
		}
		lemz.add(w);
		form2lemmata.put(wkey,lemz);
		
		
		if (!w.wordform.toLowerCase().equals(w.wordform))
		{
			Set<WordForm> lemzz = form2lemmata.get(w.wordform);
			if (lemzz == null)
				lemzz =new HashSet<WordForm>();
			else
				form2lemmata.remove(w.wordform);
			lemzz.add(w);
			form2lemmata.put(w.wordform,lemzz);
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
		if (V == null)
			V = new HashSet<WordForm>();
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
		Set<WordForm> V  =  this.form2lemmata.get(wordform);
		if (V != null)
			return V;
		else return new HashSet<WordForm>();
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
	
	public void close()
	{
		db.commit();
		//db.compact();
		db.close();
	}
	
	public static void main(String[] args)
	{
		MapDBLexicon l = new MapDBLexicon("/home/jesse/Data/LemmatizerIMPACT/Historical.mapdb");
		//l.readFromFile("resources/exampledata/molexDump.txt");
		InMemoryLexicon iml = new InMemoryLexicon();
		iml.readFromFile("resources/exampledata/molexDump.txt");
	
		Set<String> lookmeup = new HashSet<String>();
		for (WordForm w: iml)
		{
			if (w.lemma.contains("aan"))
			{
				lookmeup.add(w.lemma);
			}
		}
		nl.openconvert.log.ConverterLog.defaultLog.println("done reading lexicon!");
		long st = System.currentTimeMillis();
		int N=0;
		for (String lem: lookmeup)
		{
			{
				Set<WordForm> x = l.findForms(lem, null);
				N++;
				System.out.println(x);
			}
		}
		long et = System.currentTimeMillis();
		long d = et -st;
		double tpa = d / (double) N;
		nl.openconvert.log.ConverterLog.defaultLog.println("N=" + N + " d = " + d + " tpa = " + tpa);
		
	}
}
