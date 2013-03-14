package nl.namescape.nelexicon;

public class NEContainment
{
	public Integer primaryKey;
	public Integer partNumber;
	public NEAnalyzedWordform parent;
	public NEAnalyzedWordform child;
	public Integer parentKey = null;
	public Integer childKey = null;
}