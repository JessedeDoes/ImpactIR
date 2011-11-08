package lemmatizer;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collections;

public class Dataset
{
  String name;
  FeatureSet features = new FeatureSet();
  Vector<Item> items = new Vector<Item>();

  public Dataset(String name)
  {
     this.name=name;
  }

  public void reduceItems(int max)
  {
    if (max <=0) return;
    if (max >= items.size()) return;
    Collections.shuffle(items);
    items.setSize(max);
  }

  void addItem(String s, String classLabel)
  {
    Item i = features.makeItem(s,classLabel);
    items.add(i);
  }

  public int size()
  {
    return items.size();
  }

  public static void main(String[] args)
  {
    Dataset d = new Dataset("testje");
    Feature p1 = new Feature("prefix" + 1) { public  String getValue(String s) { return s.substring(0,1); } };
    Feature p2 = new Feature("prefix" + 2) { public  String getValue(String s) { return s.substring(0,2); } };
    //d.addFeature(p1);
    //d.addFeature(p2);
    //d.addItem("hond","ja");
    //d.addItem("slapen","nee");
    //d.createInstances();
  }
}
