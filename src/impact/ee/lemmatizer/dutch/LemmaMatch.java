package impact.ee.lemmatizer.dutch;

public class LemmaMatch
{
	String wordform;
	String lemma;
	String corpusTag;
	enum MatchType {Lexicon, LexiconWithConversion, Guesser, Unknown};
	MatchType type;
	String lexiconTag;
	
	public int hashCode()
	{
		return wordform.hashCode() + lemma.hashCode() + corpusTag.hashCode() + lexiconTag.hashCode();
	}
	
	public String toString()
	{
		return "{" + type + ", " + lemma + ", " + lexiconTag + "}";
	}
	
	public boolean equals(Object other)
	{
		try
		{
			LemmaMatch o = (LemmaMatch) other;
			return wordform.equals(o.wordform) && lemma.equals(o.lemma) && lexiconTag.equals(o.lexiconTag)
					&& corpusTag.equals(o.corpusTag) && type == o.type;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
