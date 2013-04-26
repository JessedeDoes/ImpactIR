package impact.ee.lexicon.check;
import impact.ee.lexicon.*;
import impact.ee.util.*;
public class LexiconChecker 
{
	public void checkLemmaWordformDistance(ILexicon lexicon)
	{
		int k=0;
		for (WordForm w: lexicon)
		{
			int d = LevenshteinDistance.computeLevenshteinDistance(w.lemma, w.wordform); 
			if (1.5*d > w.lemma.length())
			{
				System.err.println(k++ +  ": (" + d + ") " + w);
			}
		}
	}
	
	public static void main(String[] args)
	{
		LexiconDatabase l = new LexiconDatabase("impactdb.inl.loc", "EE3_5");
		l.useSimpleWordformsOnly = true;
		l.dumpWithFrequenciesAndDerivations = false;
		LexiconChecker c = new LexiconChecker();
		c.checkLemmaWordformDistance(l);
	}
}
