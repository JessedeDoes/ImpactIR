package nl.namescape.nelexicon;

public class NEContainment
{
	public Integer primaryKey;
	public Integer partNumber;
	public NEAnalyzedWordform parent;
	public NEAnalyzedWordform child;
	public Integer parentKey = null;
	public Integer childKey = null;
	
	public boolean equals(NEContainment other)
	{
		return this.parent.equals(other.parent) && this.child.equals(other.child);
	}
	
	public int hashCode()
	{
		return (parent).hashCode() + child.hashCode();
	}
	
	
	public boolean equals(Object other)
	{
		try
		{
			NEContainment o = (NEContainment) other;
			return equals(o);
		} catch (Exception e)
		{
			
		}
		return false;
	}
}