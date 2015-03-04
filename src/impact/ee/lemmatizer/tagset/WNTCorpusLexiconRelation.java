package impact.ee.lemmatizer.tagset;

public class WNTCorpusLexiconRelation implements TagRelation {

	@Override
	public boolean compatible(Tag t1, Tag t2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean corpusTagCompatibleWithLexiconTag(String t1, String t2,
			boolean allowConversions) 
	{
		// TODO Auto-generated method stub
		//System.err.println("CHECK: <" + t1 + "> ? <" + t2 + ">");
		if (t2 == null)
			return true;
		return t2.contains(t1.trim());
	}

}
