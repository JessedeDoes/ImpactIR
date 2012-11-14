package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class PoSAtFeature extends Feature
{
	int k;

	public PoSAtFeature(int x)
	{
		k=x;
		name = "pos_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("tag", k);
		s = TaggerFeatures.extractPoS(s);
		return s;
	}
}