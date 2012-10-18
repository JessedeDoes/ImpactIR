package impact.ee.lemmatizer;

import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.features.PrefixFeature;
import impact.ee.classifier.features.SuffixFeature;
import impact.ee.classifier.features.*;

public class SimpleFeatureSet extends FeatureSet
{
	public SimpleFeatureSet()
	{
		addFeature(new WholeStringFeature()); // hackje voor suffix guesser..
		for (int i=1; i < 6; i++)
		{
			addFeature(new SuffixFeature(i));
			addFeature(new PrefixFeature(i));
		}
		/*
		for (int i=2; i < 5; i++)
			addFeature(new CharAtFeature(-i));
		*/
		addFeature(new WholeStringFeature());
	} 
}
