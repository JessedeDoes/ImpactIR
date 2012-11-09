package impact.ee.lemmatizer;
public class SimplePatternFinder implements PatternFinder
{
	@Override
	public Pattern findPattern(String a, String b, String PoS)
	{
		return null;
	}
	/**
	 * Problem.
	 * This procedure favors af(breken/gebroken) over (af/afge)br(eken/oken)
	 * Would not be the case for slightly adaptive, more EM-like procedure? 
	 */
	public Pattern findPattern(String a, String b)
	{
		SimplePattern bestPattern = new SimplePattern();
		int bestCost = 100;
		for (int i=0; i < a.length(); i++)
		{
			String leftSuffix = a.substring(a.length()-i,a.length()); 
			String stem = a.substring(0,a.length()-i);

			if (b.startsWith(stem)) // NEE: niet altijd goed, stem zou "" kunnen zijn of 1 letter of zo
			{
				SimplePattern r = new SimplePattern();
				r.leftPrefix = r.rightPrefix = null;
				r.leftSuffix = leftSuffix;
				r.rightSuffix = b.substring(stem.length());
				r.leftPrefix = r.rightPrefix = "";
				r.finalOnly = true;
				bestPattern = r;
				bestCost = i;
				break;
			}
		}

		if (bestPattern != null)
		{
		}

		for (int i=0; i < a.length(); i++)
		{
			String leftSuffix = a.substring(a.length()-i);
			String leftStem = a.substring(0, a.length()-i);

			for (int j=0; j < leftStem.length(); j++)
			{
				if (i+j >= bestCost) break;
				String s = leftStem.substring(j);
				int k = b.indexOf(s);
				if (k >= 0)
				{
					String leftPrefix = leftStem.substring(0,j); 
					String rightPrefix = b.substring(0,k);
					String rightSuffix = b.substring(k+s.length());
					if (i+j < bestCost)
					{
						bestCost = i+j;
						bestPattern = new SimplePattern(leftPrefix, leftSuffix, rightPrefix, rightSuffix);
					}
				}
			} 
		}
		return bestPattern;
	}
}
