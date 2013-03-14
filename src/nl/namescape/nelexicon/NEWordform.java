package nl.namescape.nelexicon;

public class NEWordform
{
	public Integer primaryKey = null;
	public String wordform;
	
	public boolean equals(NEWordform other)
	{
		return DatabaseMapping.equal(this.wordform,other.wordform); 			
	}
	
	public boolean equals(Object other)
	{
		try
		{
			NEWordform o = (NEWordform) other;
			return equals(o);
		} catch (Exception e)
		{
			
		}
		return false;
	}
	
	public int hashCode()
	{
		return (wordform).hashCode();
	}
}