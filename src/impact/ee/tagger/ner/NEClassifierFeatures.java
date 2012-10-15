package impact.ee.tagger.ner;

import impact.ee.classifier.Feature;
import impact.ee.classifier.FeatureSet;

public class NEClassifierFeatures 
{

	public Feature promoteTaggerFeature(Feature f)
	{
		Feature f1 = new Feature()
		{
			public String getValue(Object o)
			{
				return getValue(((Chunk) o).context);
			}
		};
		f1.name = "chunked_" + f.name;
		return f1;
	}
	
	public static class WordInFeature extends Feature
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
	
	
	public void addBasicFeatures(FeatureSet f)
	{
		for (int i=0; i < 10; i++)
		{
			f.addFeature(new WordInFeature(i));
		}
	}
}
