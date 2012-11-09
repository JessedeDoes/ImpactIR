package impact.ee.tagger.ner;

import impact.ee.classifier.Feature;

public class WordInFeature extends Feature
{
	int k;
	public WordInFeature(int k)
	{
		this.k = k;
		this.name = "wordIn" + k;
	}
	
	public String getValue(Object o)
	{
		Chunk c = (Chunk) o;
		if (k < c.length)
		{
			return c.context.getAttributeAt("word", k);
		}
		return null;
	}
}