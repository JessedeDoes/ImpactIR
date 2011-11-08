package lemmatizer;

import java.util.Set;

import lemmatizer.Lexicon.WordForm;

public interface ParadigmExpander
{
	public void setCallback(FoundFormHandler callback);
	public void expandWordForm(Lexicon.WordForm w);
        public void expandLemmaList(String filename);
	public void findInflectionPatterns(Lexicon all, Set<Lexicon.WordForm> heldOut);
        public void findInflectionPatterns(String fileName);
}
