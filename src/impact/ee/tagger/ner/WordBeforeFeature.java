package impact.ee.tagger.ner;

import impact.ee.classifier.Feature;

public class WordBeforeFeature extends Feature
{
	int k;
	public WordBeforeFeature(int k)
	{
		this.k = k;
		this.name = "wordBefore" + k;
	}
	
	public String getValue(Object o)
	{
		Chunk c = (Chunk) o;
		return c.context.getAttributeAt("word", k);
	}
}