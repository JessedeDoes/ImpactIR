package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class CapitalFirstFeature extends Feature
{
	int k;

	public CapitalFirstFeature(int x)
	{
		k=x;
		name = "capitalFirst_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", k);
		if (s != null)
			return new Boolean(s.matches("^[A-Z][a-z]*$")).toString();
		return "false";
	}
}