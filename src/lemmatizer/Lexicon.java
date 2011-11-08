package lemmatizer;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import util.Trie;
import java.io.FileReader;
import java.util.*;

/**
 * This should be called InMemoryLexicon and implement a "Lexicon" interface
 * <br>
 * It would be nice to keep the lexicon the the database, but rather slow..
 * 
 * @author jesse
 *
 */

public class Lexicon implements Iterable<Lexicon.WordForm>
{
	public HashMap<String, Set<WordForm>> lemma2forms = new HashMap<String, Set<WordForm>>();
	public HashMap<String, Set<WordForm>> form2lemmata = new HashMap<String, Set<WordForm>>();
	public Set<WordForm> wordforms = new HashSet<WordForm>();


	static class WordForm
	{
		String lemma = "";
		String wordform = "";
		String tag = "nil";
		String lemmaPoS = "nil";
		
		int wordformFrequency = 0;
		int lemmaFrequency = 0;

		public String toString()
		{
                   if (wordformFrequency > 0 || lemmaFrequency > 0)
                   {
                       return "{" + lemma + "," + wordform + "," + tag + ", f(w)=" + wordformFrequency + ", f(l)=" + lemmaFrequency + "}";
                   } else
			return "{" + lemma + "," + wordform + "," + tag + "}";
		}
		
		public String toStringTabSeparated()
		{
			return wordform + "\t" + lemma + "\t" + tag + "\t" + lemmaPoS;
		}
		
		public boolean equals(Object other)
		{
			try
			{
				WordForm wf = (WordForm) other;
				return lemma.equals(wf.lemma) && wordform.equals(wf.wordform)
						&& tag.equals(wf.tag) && lemmaPoS.equals(wf.lemmaPoS);
			} catch (Exception e)
			{
				return false;
			}
		}

		public int hashCode()
		{
			return lemma.hashCode() + wordform.hashCode() + tag.hashCode();
		}
	}
	
	public void readFromFile(String fileName)
	{
		if (fileName.startsWith("database:"))
		{
			String dbName = fileName.substring("database:".length());
			LexiconDatabase ldb = new LexiconDatabase(dbName);
			for (Lexicon.WordForm w: ldb)
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
			
			int ruleId = 1;
			while ( (s = b.readLine()) != null) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
			{
				String[] parts = s.split("\t");
				
				WordForm w = new WordForm();
				w.wordform = parts[0]; 
				w.lemma = parts[1];
				w.tag = parts[2];
				w.lemmaPoS = parts[3];
				
				try
				{
					if (parts.length > 4)
					{
						w.lemmaFrequency = Integer.parseInt(parts[4]);
						if (w.lemmaFrequency > 10000)
						{
							System.err.println(s);
							System.exit(1);
						}
					}
					if (parts.length > 5)
						w.wordformFrequency = Integer.parseInt(parts[5]);
				} catch (Exception e)
				{
					//e.printStackTrace();
				}
				if (w.wordform.indexOf(" ") >= 0 || w.lemma.indexOf(" ") >= 0) // temporary hack: no spaces
					continue;
				addWordform(w);
			}
		} catch (Exception e)
		{
			System.err.println("s = " + s);
			e.printStackTrace();
		}
	}

	public void addWordform(WordForm w)
	{
		wordforms.add(w);
		addToIndexes(w);
	}
	
	private void addToIndexes(WordForm w)
	{
		if (w == null)
			return;
		Set<WordForm> wfz = lemma2forms.get(w.lemma);
		if (wfz == null)
			lemma2forms.put(w.lemma, (wfz =new 	HashSet<WordForm>()));

		wfz.add(w);
		
		Set<WordForm> lemz = form2lemmata.get(w.wordform);
		if (lemz == null)
			form2lemmata.put(w.wordform, (lemz =new HashSet<WordForm>()));
		lemz.add(w);
	}
	
	public boolean containsLemmaWordform(String lemma, String wordform)
	{
		Set<WordForm> s= lemma2forms.get(lemma);
		if (s!= null)
		{
			for (WordForm w: s)
				if (w.wordform.equals(wordform))
					return true;
		}
		return false;
	}
	
	/**
	 * beware: this is <i>true</i> if tag does not occur at all for the given lemma
	 * @param lemma
	 * @param wordform
	 * @param tag
	 * @return
	 */
	public boolean containsLemmaWordformForTag(String lemma, String wordform, String tag)
	{
		Set<WordForm> s= lemma2forms.get(lemma);
		boolean foundTag = false;
		if (s != null)
		{
			
				for (WordForm w: s)
				{
					if (w.wordform.equals(wordform))
						return true;
			    foundTag |= tag.equals(w.tag);
				}
		}
		return !foundTag;
	}
	
	public Set<WordForm> findForms(String lemma, String tag)
	{
		return lemma2forms.get(lemma);
	}
	
	public Set<WordForm> findLemmata(String wordform)
	{
		return form2lemmata.get(wordform.toLowerCase());
	}
	
	public boolean hasInflectedForm(String form, String lemma, String tag)
	{
		return false;
	}
	
	/**
	 * Beware: this creates a new one on each invocation
	 * @return
	 */
	
	public Trie createTrie(boolean addWordBoundaries)
	{
		Trie t = new Trie();
		for (String w: form2lemmata.keySet())
		{
			if (addWordBoundaries)
				w= spellingvariation.Alphabet.initialBoundaryString + w + 
				spellingvariation.Alphabet.finalBoundaryString;
			t.root.putWord(w);
		}
		return t;
	}

	@Override
	public Iterator<WordForm> iterator()
	{
		// TODO Auto-generated method stub
		return wordforms.iterator();
	}
}
