package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class SuffixFeature extends Feature
{
	int k;

	public SuffixFeature(int x)
	{
		k=x;
		name = "s_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", 0);
		if (s.length() >= k)
		{
			return s.substring(s.length()-k);
		}
		return "";
	}
}