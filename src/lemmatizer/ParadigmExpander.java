package lemmatizer;

import java.util.Set;

import lexicon.InMemoryLexicon;
import lexicon.WordForm;

public interface ParadigmExpander
{
	public void setCallback(FoundFormHandler callback);
	public void expandWordForm(WordForm w);
        public void expandLemmaList(String filename);
	public void findInflectionPatterns(InMemoryLexicon all, Set<WordForm> heldOut);
        public void findInflectionPatterns(String fileName);
}
