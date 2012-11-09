package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class TagAtFeature extends Feature
{
	int k;

	public TagAtFeature(int x)
	{
		k=x;
		name = "tag_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("tag", k);
		return s;
	}
}