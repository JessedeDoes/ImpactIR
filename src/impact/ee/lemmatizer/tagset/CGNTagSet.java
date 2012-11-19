package impact.ee.lemmatizer.tagset;

public class CGNTagSet extends TagSet 
{
	
	@Override
	public boolean isInflectingPoS(String tag) 
	{
		if (tag == null)
			return false;
		String pos = this.getPoS(tag);
		return pos.equals("WW") || pos.equals("N") || pos.equals("ADJ");
	}
}
