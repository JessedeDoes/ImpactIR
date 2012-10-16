package impact.ee.lemmatizer;

import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.features.PrefixFeature;
import impact.ee.classifier.features.SuffixFeature;
import impact.ee.classifier.features.*;

public class SimpleFeatureSet extends FeatureSet
{
	public SimpleFeatureSet()
	{
		for (int i=1; i < 6; i++)
		{
			addFeature(new SuffixFeature(i));
			addFeature(new PrefixFeature(i));
		}
		addFeature(new WholeStringFeature());
	} 
}
