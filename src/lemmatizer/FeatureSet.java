package lemmatizer;
import java.util.*;

public class FeatureSet implements Iterable<Feature>
{
  boolean finalized=false;
  ArrayList<Feature> features = new ArrayList<Feature>(); 
  Feature classFeature = new Feature();

  public FeatureSet()
  {
    classFeature.name="class";
  }

  public Iterator<Feature> iterator()
  {
    return features.iterator();
  }

  public void finalize() // en nog iets??
  {
    if (!finalized)
    {
      for (Feature f: features) f.pruneValues();
      features.add(classFeature); // moet niet gepruned
      finalized=true;
    }
  }

  public Item makeItem(String s, String label)
  {
    Item i = new Item();
    i.classLabel = label;
    for (Feature f: features)
    {
      i.values.add(f.storeValue(s));
    }
    i.values.add(label);
    classFeature.addValue(label);
    // System.err.println(i);
    return i;
  }

  public int size()
  {
    return features.size();
  }

  public Feature get(int i)
  {
    return features.get(i);
  }

  public void addFeature(Feature f)
  {
    features.add(f);
  }

  public Item makeTestItem(String s)
  {
    Item item = new Item();
    //System.err.println("fs: " + features.size());
    for (int i=0; i < features.size()-1; i++)
    {
      item.add(features.get(i).getValue(s));
    }
    //System.err.println(item);
    return item;
  }
  /**
   * Ths is a fake featureset with one feature which is just the whole string
   * <p>
   * @author taalbank
   *
   */
  public static class Dummy extends FeatureSet
  {
  	public Dummy()
  	{
  		this.addFeature(new WholeStringFeature());
  	}
  }
  
  public static class ReversedDummy extends FeatureSet
  {
  	public ReversedDummy()
  	{
  		this.addFeature(new ReversedStringFeature());
  	}
  }
}
