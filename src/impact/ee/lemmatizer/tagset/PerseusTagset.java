package impact.ee.lemmatizer.tagset;

public class PerseusTagset extends TagSet 
{

	@Override
	public boolean isInflectingPoS(String pos) 
	{
		// TODO Auto-generated method stub
		return (pos.matches("^[vnta]$"));
	}

	@Override
	public Tag parseTag(String tag) {
		// TODO Auto-generated method stub
		return Tag.parseParoleStyleTag(tag);
	}

}
