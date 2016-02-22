package impact.ee.classifier.weka;
import impact.ee.classifier.Classifier;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.Distribution;
import impact.ee.classifier.Feature;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.Instance;

import java.io.FileWriter;

import weka.classifiers.meta.MultiClassClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

public class WekaClassifier implements Classifier
{
	weka.core.Instances wekaInstances; // moeten er ook tijdens classificatie zijn...
	FeatureSet features;
	String name;
	weka.classifiers.Classifier wekaClassifier;
	FastVector wekaAttributes = new FastVector();
	Class<?> classifierClass;
	boolean doMulti = false;
	//weka.classifiers.functions.SMO
	
	public WekaClassifier()
	{
		
	}
	
	public WekaClassifier(String classifierType, boolean wrapInMultiClassifier)
	{
		try
		{
			classifierClass = Class.forName("weka.classifiers." + classifierType);
			doMulti = wrapInMultiClassifier;
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
		createInstances(d.instances);
		
		try
		{
			if (doMulti)
			{
				weka.classifiers.Classifier binary =  (weka.classifiers.Classifier)
						classifierClass.newInstance();
				weka.classifiers.meta.MultiClassClassifier multi  = new MultiClassClassifier();
				multi.setClassifier(binary);
				
				wekaClassifier = multi;
				wekaClassifier.buildClassifier(wekaInstances);
			} else
			{
				wekaClassifier = (weka.classifiers.Classifier) classifierClass.newInstance();
				wekaClassifier.buildClassifier(wekaInstances);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String classifyString(String s)
	{
		Instance i = features.makeTestInstance(s);
		return classifyInstance(i);
	}

	public String classifyInstance(Instance i)
	{
		Distribution d = distributionForInstance(i);
		return d.get(0).label;
	}

	public Distribution distributionForInstance(Instance i)
	{
		weka.core.Instance wekaInstance = makeWekaInstance(i);
		try
		{
			double[] P = wekaClassifier.distributionForInstance(wekaInstance);
			Distribution outcomes = new Distribution(P.length);
			for (int j=0; j < P.length; j++)
			{
				String classId = (String) features.classFeature.values.elementAt(j);
				outcomes.addOutcome(classId,P[j]);
			}
			outcomes.sort();
			return outcomes;
		} catch (Exception e)
		{
			return null;
		}
	}

	public weka.core.Instance makeWekaInstance(Instance i)
	{
		weka.core.Instance instance = new weka.core.Instance(features.size());
		// nl.openconvert.log.ConverterLog.defaultLog.println("make instance for " + i);
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
		instance.setDataset(this.wekaInstances);
		return instance;
	}

	void createInstances(Iterable<Instance> instances)
	{
		features.finalize();

		for (Feature fi: features)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println(fi + " " + fi.values);
			if (fi == features.classFeature && fi.values.size() ==1) // er moet meer dan een mogelijke waarde voor class zijn!
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("Voeg dummy waarde toe voor " + name);
				fi.values.addElement("DUMMY_DUMMY");
			}
			Attribute a = new Attribute(fi.name,fi.values);
			wekaAttributes.addElement(a);
		}

		wekaInstances = new Instances(name, wekaAttributes ,0);

		for (Instance i: instances)
		{
			weka.core.Instance z = makeWekaInstance(i);
			wekaInstances.add(z);
		}
		wekaInstances.setClassIndex(wekaInstances.numAttributes()-1);
		//nl.openconvert.log.ConverterLog.defaultLog.println(instances);
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

	@Override
	public void load(String filename) {
		// TODO Auto-generated method stub
		
	}
}
