package impact.ee.lemmatizer.reverse;

import impact.ee.lemmatizer.FoundFormHandler;
import impact.ee.lexicon.InMemoryLexicon;
import impact.ee.lexicon.WordForm;

import java.util.Set;


public interface ParadigmExpander
{
	public void setCallback(FoundFormHandler callback);
	public void expandWordForm(WordForm w);
	public void expandLemmaList(String filename);
	public void findInflectionPatterns(InMemoryLexicon all, Set<WordForm> heldOut);
	public void findInflectionPatterns(String fileName);
}
