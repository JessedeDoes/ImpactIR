package impact.ee.lemmatizer.tagset;

public class GiGaNTTagSet extends TagSet
{

	@Override
	public boolean isInflectingPoS(String pos) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Tag parseTag(String tag) 
	{
		// TODO Auto-generated method stub
		return Tag.parseParoleStyleTag(tag);
	}

}
