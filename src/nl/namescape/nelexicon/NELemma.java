package nl.namescape.nelexicon;

public class NELemma
{
	public String lemma;
	public String neLabel;
	public String nePartLabel;
	public String lemmaID;
	public String lemmaPoS;
	public String gloss;
	public String sex;
	public Integer primaryKey = null;
	
	public boolean equals(NELemma other)
	{
		//System.err.println("BLOEP!");
		return DatabaseMapping.equal(this.lemma,other.lemma) 
				&& DatabaseMapping.equal(this.lemmaPoS, other.lemmaPoS)
				&& DatabaseMapping.equal(this.neLabel, other.neLabel);
	}
	
	public boolean equals(Object other)
	{
		//System.err.println("BLOEP!");
		try
		{
			NELemma o = (NELemma) other;
			return equals(o);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public int hashCode()
	{
		return (lemma + neLabel + gloss  + sex).hashCode();
	}
}