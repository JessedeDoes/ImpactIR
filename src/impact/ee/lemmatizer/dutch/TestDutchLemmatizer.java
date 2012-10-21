package impact.ee.lemmatizer.dutch;

public class TestDutchLemmatizer 
{
	int nCorrect=0;
	int nItems=0;
	int nCorrectGivenTag=0;
	int nCorrectGivenPoS=0;
	
	public String toString()
	{
		double score = nCorrect / (double) nItems;
		double score2 = (nCorrect + nCorrectGivenPoS) / (double) nItems;
		return "items: " + nItems + " correct: "  + score + " with known PoS: " +  score2 + " with known Tag " + nCorrectGivenTag;
		
	}
}
