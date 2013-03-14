package nl.namescape.nelexicon;

public class NEAnalyzedWordform
{
	public Integer primaryKey = null;
	public Integer lemmaKey = null;
	public Integer wordformKey = null;
	public NELemma lemma = null;
	public NEWordform wordform = null;
	public int hashCode()
	{
		return (lemma).hashCode() + wordform.hashCode();
	}
	
	public boolean equals(NEAnalyzedWordform other)
	{
		return this.lemma.equals(other.lemma) && this.wordform.equals(other.wordform);
	}
	
	public boolean equals(Object other)
	{
		try
		{
			NEAnalyzedWordform o = (NEAnalyzedWordform) other;
			return equals(o);
		} catch (Exception e)
		{
			
		}
		return false;
	}
}