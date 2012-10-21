package impact.ee.lemmatizer.dutch;

public class TestDutchLemmatizer 
{
	int nCorrect=0;
	int nItems=0;
	int nCorrectGivenTag=0;
	int nCorrectGivenPoS=0;
	
	public String toString()
	{
		return "items: " + nItems + " correct: "  + nCorrect + " with known PoS: " +  nCorrectGivenPoS + " with known Tag " + nCorrectGivenTag;
	}
}
