package impact.ee.lexicon;

import java.util.Iterator;
import java.util.Set;



public interface ILexicon extends Iterable<WordForm>
{
	public abstract void addWordform(WordForm w);
	public abstract Set<WordForm> findForms(String lemma, String tag);
	public abstract Set<WordForm> findLemmata(String wordform);
	public abstract Set<WordForm> searchByModernWordform(String wordform);
	public abstract Iterator<WordForm> iterator();
}