package lemmatizer;
import weka.core.*;
import weka.classifiers.*;
import java.io.FileWriter;

public class WekaClassifier implements Classifier
{
  Instances instances; // moeten er ook tijdens classificatie zijn...
  FeatureSet features;
  String name;
  weka.classifiers.Classifier wekaClassifier;
  FastVector wekaAttributes = new FastVector();
  Class classifierClass;

  public WekaClassifier(String classifierType)
  {
    try
    {
      classifierClass = Class.forName("weka.classifiers." + classifierType);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  public void setType(String t)
  {
  	 try
     {
       classifierClass = Class.forName("weka.classifiers." + t);
     } catch (Exception e)
     {
       e.printStackTrace();
     }
  }

  public void train(Dataset d)
  {
    train(d,-1);
  } 

  public void train(Dataset d, int MAX_ITEMS_USED)
  {
    name = d.name;
    features = d.features;
    d.reduceItems(MAX_ITEMS_USED);
    createInstances(d.items);
    try
    {
      wekaClassifier = (weka.classifiers.Classifier) classifierClass.newInstance();
      wekaClassifier.buildClassifier(instances);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public String classifyString(String s)
  {
    Item i = features.makeTestItem(s);
    return classifyItem(i);
  }

  public String classifyItem(Item i)
  {
    return null;
  }

  public Distribution distributionForItem(Item i)
  {
    Instance wekaInstance = makeWekaInstance(i);
    try
    {
      double[] P = wekaClassifier.distributionForInstance(wekaInstance);
      Distribution outcomes = new Distribution(P.length);
      for (int j=0; j < P.length; j++)
      {
        String classId = (String) features.classFeature.values.elementAt(j);
        outcomes.addItem(classId,P[j]);
      }
      outcomes.sort();
      return outcomes;
    } catch (Exception e)
    {
      return null;
    }
  }

  public Instance makeWekaInstance(Item i)
  {
    Instance instance = new Instance(features.size());
    // System.err.println("make instance for " + i);
    for (int j=0; j < features.size(); j++)
    {
      if (i.values.size() > j)
      {
        String  val = i.values.get(j);
        try
        {
          instance.setValue( (Attribute) wekaAttributes.elementAt(j), features.get(j).pruneValue(val));
        } catch (Exception e)
        {
          //e.printStackTrace();
        }
      }
    }
    instance.setDataset(this.instances);
    return instance;
  }

  void createInstances(Iterable<Item> items)
  {
    features.finalize();

    for (Feature fi: features)
    {
      if (fi == features.classFeature && fi.values.size() ==1) // er moet meer dan een mogelijke waarde voor class zijn!
      {
        System.err.println("Voeg dummy waarde toe voor " + name);
        fi.values.addElement("DUMMY_DUMMY");
      }
      Attribute a = new Attribute(fi.name,fi.values);
      wekaAttributes.addElement(a);
    }

    instances = new Instances(name, wekaAttributes ,0);

    for (Item i: items)
    {
      Instance z = makeWekaInstance(i);
      instances.add(z);
    }
    instances.setClassIndex(instances.numAttributes()-1);
    //System.err.println(instances);
  }

  public void save(String fileName) throws java.io.IOException
  {
     FileWriter out = new FileWriter(fileName);
     out.write(this.toString()); // This is not OK, works only for weka classifiers
     out.close();
  }

  public String toString()
  {
    return wekaClassifier.toString();
  }
}
