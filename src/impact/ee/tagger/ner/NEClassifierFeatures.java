package impact.ee.tagger.ner;

import impact.ee.classifier.Feature;
import impact.ee.classifier.FeatureSet;
import impact.ee.tagger.features.WordShapeClassifier;

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
	
	public static void addBasicFeatures(FeatureSet f)
	{
		for (int i=0; i < 10; i++)
		{
			f.addFeature(new WordInFeature(i));
		}
		for (int i=-1; i >= -2; i--)
		{
			f.addFeature(new WordBeforeFeature(i));
		}
		for (int i= 1; i <=2; i++ )
		{
			f.addFeature(new WordAfterFeature(i));
		}
		f.addStochasticFeature(new BagOfWordsFeature(2));
		f.addStochasticFeature(new BagOfCharacterNGramsFeature(3,4));
		f.addFeature(new ChunkShapeFeature(WordShapeClassifier.WORDSHAPECHRIS1));
	}
}
