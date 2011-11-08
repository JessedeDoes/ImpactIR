package lemmatizer;
class SimpleFeatureSet extends FeatureSet
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
