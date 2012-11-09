package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class AllCapitalFeature extends Feature
{
	int k;

	public AllCapitalFeature(int x)
	{
		k=x;
		name = "AllCapital_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", k);
		if (s != null)
			return new Boolean(s.matches("^[A-Z][A-Z]*$")).toString();
		return "false";
	}
}