package impact.ee.lemmatizer.tagset;

public class CGN2Parole implements TagRelation 
{
	static final String[][] compatibilities =
		{
			{"N","NOU"},
			{"ADJ","ADJ"},
			{"WW","VRB"},
			{"TW", "NUM"},
			
		};
	@Override
	public boolean compatible(Tag t1, Tag t2) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean compatible(String t1, String t2) 
	{
		// TODO Auto-generated method stub
		return false;
	}

}
