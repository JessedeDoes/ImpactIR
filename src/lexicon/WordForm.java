package lexicon;


public class WordForm
{
	public String lemma = "";
	public String wordform = "";
	public String tag = "nil";
	public String lemmaPoS = "nil";
	
	public int wordformFrequency = 0;
	public int lemmaFrequency = 0;

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
