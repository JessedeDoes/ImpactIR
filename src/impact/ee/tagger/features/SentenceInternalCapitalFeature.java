package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class SentenceInternalCapitalFeature extends Feature
{
	int k;

	public SentenceInternalCapitalFeature(int x)
	{
		k=x;
		name = "sentenceInternalCapital_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", k);
		if (s != null)
		{
			boolean b = s.matches("^[A-Z][a-z]*$");
			if (b)
			{
				String previous = ((Context) o).getAttributeAt("word", k-1);
				boolean b1 =  previous.matches(".*[!?\\.].*");
				return new Boolean(!b1).toString();
			} else
				return "false";
		}
		return "false";
	}
}