package impact.ee.lemmatizer;

import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.features.PrefixFeature;
import impact.ee.classifier.features.SuffixFeature;


public class SimpleFeatureSet extends FeatureSet
{
  public SimpleFeatureSet()
  {
    for (int i=1; i < 6; i++)
    {
      addFeature(new SuffixFeature(i));
      addFeature(new PrefixFeature(i));
    }
  } 
}
