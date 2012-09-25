package lemmatizer;

import classifier.Feature;
import classifier.FeatureSet;


public class SimpleFeatureSet extends FeatureSet
{
  public SimpleFeatureSet()
  {
    for (int i=1; i < 6; i++)
    {
      addFeature(new Feature.SuffixFeature(i));
      addFeature(new Feature.PrefixFeature(i));
    }
  } 
}
