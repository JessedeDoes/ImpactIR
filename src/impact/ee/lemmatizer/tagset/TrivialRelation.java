package impact.ee.lemmatizer.tagset;

public class TrivialRelation implements TagRelation 
{

	@Override
	public boolean compatible(Tag t1, Tag t2) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean corpusTagCompatibleWithLexiconTag(String t1, String t2,
			boolean allowConversions) 
	{
		// TODO Auto-generated method stub
		return t1.equalsIgnoreCase(t2);
	}
}
