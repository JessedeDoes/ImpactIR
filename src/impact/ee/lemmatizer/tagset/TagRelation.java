package impact.ee.lemmatizer.tagset;

public interface TagRelation 
{
	public boolean compatible(Tag t1, Tag t2);
	public boolean compatible(String t1, String t2);
}
