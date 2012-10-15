package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class InitialAbbreviationFeature extends Feature
{
	int k;

	public InitialAbbreviationFeature(int x)
	{
		k=x;
		name = "initial_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", k);
		if (s != null)
		{
			boolean b = s.matches("^[A-Z][a-z]\\.?$") || s.matches("[A-Z][a-z]?\\.([A-Z][a-z]?\\.)*");
			return new Boolean(b).toString();
		} 
		return "false";
	}
}