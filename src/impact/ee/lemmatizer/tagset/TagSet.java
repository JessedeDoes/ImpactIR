package impact.ee.lemmatizer.tagset;

public abstract class TagSet 
{
	public char multiValueSeparator='|';
	public String getPoS(String tag)
	{
		tag = tag.replaceAll("\\(.*", "");
		return tag;
	}
	public abstract boolean isInflectingPoS(String pos);
	
	public abstract Tag parseTag(String tag);
}
