package lexicon;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.io.FileReader;
import java.util.*;

import trie.Trie;



/**
 * This should be called InMemoryLexicon and implement a "Lexicon" interface
 * <br>
 * It would be nice to keep the lexicon in the database, but it would presumably be rather slow.
 * 
 * @author jesse
 *
 */

public class InMemoryLexicon implements Iterable<WordForm>, ILexicon
{
	public HashMap<String, Set<WordForm>> lemma2forms = new HashMap<String, Set<WordForm>>();
	public HashMap<String, Set<WordForm>> form2lemmata = new HashMap<String, Set<WordForm>>();
	public Set<WordForm> wordforms = new HashSet<WordForm>();


	public void readFromFile(String fileName)
	{
		System.err.println("read lexicon from: " + fileName);
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
			
			int ruleId = 1;
			while ( (s = b.readLine()) != null) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
			{
				// System.err.println(s);
				WordForm w = LexiconUtils.getWordformFromLine(s);
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
	
	/* (non-Javadoc)
	 * @see lemmatizer.ILexicon#findForms(java.lang.String, java.lang.String)
	 */
	
	public Set<WordForm> findForms(String lemma, String tag)
	{
		return lemma2forms.get(lemma);
	}
	
	/* (non-Javadoc)
	 * @see lemmatizer.ILexicon#findLemmata(java.lang.String)
	 */
	
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

	/* (non-Javadoc)
	 * @see lemmatizer.ILexicon#iterator()
	 */
	
	public Iterator<WordForm> iterator()
	{
		// TODO Auto-generated method stub
		return wordforms.iterator();
	}
}
