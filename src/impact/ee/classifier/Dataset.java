package impact.ee.classifier;
//import java.util.HashSet;
import java.util.Vector;
//import java.util.Iterator;
//import java.util.HashMap;
import java.util.Collections;



public class Dataset
{
  public String name;
  public FeatureSet features = new FeatureSet();
  public Vector<Instance> instances = new Vector<Instance>();

  public Dataset(String name)
  {
     this.name=name;
  }

  public void reduceItems(int max)
  {
    if (max <=0) return;
    if (max >= instances.size()) return;
    Collections.shuffle(instances);
    instances.setSize(max);
  }

  public void addInstance(Object s, String classLabel)
  {
    Instance i = features.makeInstance(s,classLabel);
    instances.add(i);
  }

  public int size()
  {
    return instances.size();
  }

  public void pruneInstances()
  {
	  for (Instance i: instances)
	  {
		  features.pruneInstance(i);
	  }
  }
  
  public static void main(String[] args)
  {
    //Dataset d = new Dataset("testje");
    //Feature p1 = new Feature("prefix" + 1) { public  String getValue(String s) { return s.substring(0,1); } };
    //Feature p2 = new Feature("prefix" + 2) { public  String getValue(String s) { return s.substring(0,2); } };
    //d.addFeature(p1);
    //d.addFeature(p2);
    //d.addItem("hond","ja");
    //d.addItem("slapen","nee");
    //d.createInstances();
  }
}
