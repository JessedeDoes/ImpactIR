package impact.ee.tagger.ner;

import impact.ee.classifier.Feature;

public class WordAfterFeature extends Feature
{
	int k;
	
	public WordAfterFeature(int k)
	{
		this.k = k;
		this.name = "wordAfter" + k;
	}
	
	public String getValue(Object o)
	{
		Chunk c = (Chunk) o;
		return c.context.getAttributeAt("word", k -1 + c.length);
	}
}