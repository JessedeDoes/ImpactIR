package impact.ee.classifier;
import impact.ee.classifier.features.ReversedStringFeature;
import impact.ee.classifier.features.WholeStringFeature;

import java.io.Serializable;
import java.util.*;
import java.io.*;


public class FeatureSet implements Iterable<Feature>, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	boolean finalized=false;
	ArrayList<Feature> features = new ArrayList<Feature>(); 
	public ArrayList<StochasticFeature> stochasticFeatures = new ArrayList<StochasticFeature>(); 
	public Feature classFeature = new Feature();

	public FeatureSet()
	{
		classFeature.name="class";
	}

	public void save(String fileName)
	{
		try
		{
			FileOutputStream fileOut =
					new FileOutputStream(fileName);
			ObjectOutputStream out =
					new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		}catch(IOException i)
		{
			i.printStackTrace();
		}
	}
	
	public static FeatureSet load(String fileName)
	{
		try
		{
			FileInputStream fileOut =
					new FileInputStream(fileName);
			ObjectInputStream out =
					new ObjectInputStream(fileOut);
			FeatureSet fs = (FeatureSet) out.readObject();
			out.close();
			fileOut.close();
			return fs;
		} catch(Exception i)
		{
			i.printStackTrace();
			return null;
		}
	}
	
	
	public Iterator<Feature> iterator()
	{
		return features.iterator();
	}

	public void finalize() // en nog iets??
	{
		if (!finalized)
		{
			for (Feature f: features) 
				f.pruneValues();
			//features.add(classFeature); // moet niet gepruned!
										// waarom zit ie er bij????
										// is toch belachelijk?
			for (StochasticFeature sf: stochasticFeatures)
			{
				for (Distribution d: sf.storedValueHash.values())
				{
					d.computeProbabilities();
				}
			}
			finalized=true;
		}
	}

	public Instance makeInstance(Object s, String label)
	{
		Instance i = new Instance();
		i.classLabel = label;
		for (Feature f: features)
		{
			i.values.add(f.storeValueOf(s));
		}
		// i.values.add(label); // waarom???? // niet doen!
		classFeature.addValue(label);
		for (StochasticFeature f: stochasticFeatures)
		{
			i.stochasticValues.add(f.getValue(s));
		}
		// System.err.println(i);
		return i;
	}

	public void pruneInstance(Instance instance)
	{
		for (int i=0; i < features.size(); i++) // never prune class feature
		{
			Feature f = features.get(i);
			if (f == this.classFeature)
			{
				System.err.println("NOPE");
				System.exit(1);
			}
			String v = instance.values.get(i);
			Integer c;
			if ((c = f.valueCounts.get(v)) == null || c < f.THRESHOLD)
			{
				instance.values.set(i,Feature.Unknown);
				//System.err.println(this.itemToString(instance));
			} else
			{
				// System.err.println("Ok, dit is genoeg: " + c);
			}
		}
	}

	public void gatherStatistics(Iterator<Object> evidence)
	{
		while (evidence.hasNext())
		{
			Object o = evidence.next();
			for (StochasticFeature f: this.stochasticFeatures)
			{
				f.examine(o);
			}
		}
		for (StochasticFeature f: this.stochasticFeatures)
		{
			for (Distribution d: f.storedValueHash.values())
			{
				f.pruneDistribution(d);
				d.computeProbabilities();
			}
		}
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

	public void addStochasticFeature(StochasticFeature f)
	{
		stochasticFeatures.add(f);
	}

	public String itemToString(Instance instance)
	{
		String r = "{";
		if (instance.classLabel != null)
		{
			r += "Class=" + instance.classLabel + "; ";
		}
		for (int i=0; i < instance.values.size(); i++)
		{
			Feature f;
			if (i == this.features.size()) 
				f = this.classFeature;
			else 
				f = this.features.get(i);
			r += f.name + "=" + instance.values.get(i);
			if (i < instance.values.size()-1 || instance.stochasticValues.size() > 0)
				r+= ", ";
		}

		for (int i=0; i < instance.stochasticValues.size(); i++)
		{
			StochasticFeature f = this.stochasticFeatures.get(i);
			r += f.name + "=" + instance.stochasticValues.get(i);
			if (i < instance.stochasticValues.size()-1)
				r+= ", ";
		}
		r += "}";
		return r;
	}

	public Instance makeTestInstance(Object s)
	{
		Instance instance = new Instance();
		//System.err.println("fs: " + features.size());
		for (int i=0; i < features.size(); i++)
		{
			instance.addValue(features.get(i).getValue(s));
		}
		for (StochasticFeature f: stochasticFeatures)
		{
			instance.stochasticValues.add(f.getValue(s));
		}
		//System.err.println(item);
		return instance;
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
