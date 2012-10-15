package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

class WordAtFeature extends Feature
{
	int k;

	public WordAtFeature(int x)
	{
		k=x;
		name = "word_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", k);
		return s;
	}
}