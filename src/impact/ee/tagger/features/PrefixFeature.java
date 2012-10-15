package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

public class PrefixFeature extends Feature
{
	int k;

	public PrefixFeature(int x)
	{
		k=x;
		name = "p_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", 0);
		if (s.length() >= k)
		{
			return s.substring(0,k);
		}
		return "";
	}
}