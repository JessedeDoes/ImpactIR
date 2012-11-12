package impact.ee.tagger;

import impact.ee.classifier.Classifier;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.Feature;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.Instance;
import impact.ee.classifier.libsvm.LibSVMClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.tagger.features.*;
import impact.ee.util.Pair;
import impact.ee.util.Serialize;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.*;




/**
 * BasicTagger. (now a NER tagger).
 * segmentation is reasonable (best on ned.testa is now 87%)
 * but classification is really miserable.
 * suppose classification is especially bad for multiwords entities as
 * everything is decided by the first one now....
 * so ... first segment,
 * then (re)classify???
 *
 */

public class BasicNERTagger implements Serializable, Tagger
{
	private static final long serialVersionUID = 1L;
	
	FeatureSet features = new FeatureSet();
	Classifier classifier = new SVMLightClassifier(); // .svmlight.SVMLightClassifier();
	boolean useFeedback = true;
	boolean useLexicon = false;
	boolean doeEvenRaar = false;
	boolean useShapes = true;
	Set<String> knownWords = new HashSet<String>();
	double proportionOfTrainingToUse = 1;
	public String taggedAttribute = "tag";
	
	public static String[] defaultAttributeNames = {"word", "tag"};
	public String[] attributeNames = defaultAttributeNames;
	
	public void setClassifier(String className)
	{
		try
		{
			Class c = Class.forName(className);
			this.classifier = (Classifier) c.newInstance();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * why not wrap the row in one object
	 */
	
	public void loadModel(String fileName)
	{
		fileName += "." + this.classifier.getClass().getName();
		Pair<Classifier,FeatureSet> 
			p = new Serialize<Pair<Classifier,FeatureSet>>().loadFromFile(fileName);
		this.classifier = p.first;
		this.features = p.second;
	}
	
	public void saveModel(String fileName)
	{
		try 
		{
			fileName += "." + this.classifier.getClass().getName();
			Pair<Classifier,FeatureSet> p = new Pair<Classifier,FeatureSet>(classifier,features);
			new Serialize<Pair<Classifier,FeatureSet>>().saveObject(p, fileName);
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public BasicNERTagger()
	{
		features = TaggerFeatures.getMoreFeatures(useFeedback);
		if (useLexicon)
		{
			features.addStochasticFeature(new HasTagFeature(0));
			features.addStochasticFeature(new HasTagFeature(-1));
			features.addStochasticFeature(new HasTagFeature(1));
			//context potential PoS does not appear to contribute much
			//features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(1));
			//features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(-1));
		}
		if (useShapes)
		{
			Set<Feature> shapeFeatures = ShapeFeature.getShapeFeatures();
			for (Feature f: shapeFeatures)
				features.addFeature(f);
		}
		// features.addFeature(new GazetteerFeature(GazetteerFeature.LOC));
	}
	
	public void examine(Corpus statsCorpus)
	{
		features.gatherStatistics((Iterator<Object>) statsCorpus);
	}
	
	public void train(Corpus trainingCorpus)
	{		
		Dataset d = new Dataset("trainingCorpus");
		Dataset classificationSet = new Dataset("classification");
		d.features = features;
		
		for (Context c: trainingCorpus.enumerate())
		{
			if (Math.random() <= proportionOfTrainingToUse)
			{
				if (filter(c))
				{
					String answer = c.getAttributeAt(taggedAttribute, 0);
					if (answer == null)
						continue;
					if (doeEvenRaar)
					{
						boolean x = c.getAttributeAt("word",0).matches("^[A-Z]");
						answer = answer + "/" + x;
					}
					d.addInstance(c, answer);
					if (answer.startsWith("B-"))
					{
						
					}
					knownWords.add(c.getAttributeAt("word", 0));
				}
			}
		}
		
		features.finalize(); // oehoeps, dit is niet fijn, dat dat expliciet moet, moet anders...
		
		System.err.println("start training, "  + d.size() + " items");
		
		// hier zou je de dataset moeten prunen om
		// irrelevante features (te weinig voorkomende f,v combinaties) weg te gooien
		// d.pruneInstances();
		
		classifier.train(d);
		System.err.println("finish training...");
	}
	
	protected boolean filter(Context c) 
	{
		// TODO Auto-generated method stub
		return true;
	}
	
	public SimpleCorpus tag(Corpus testCorpus)
	{
		 Enumeration<Map<String,String>> output = new OutputEnumeration(this, testCorpus);
		 EnumerationWithContext<Map<String,String>> ewc = 
				 new EnumerationWithContext(Map.class, output, new DummyMap());
		 return new SimpleCorpus(ewc);
	}
	
	public void test(Corpus testCorpus)
	{
		int nItems=0; int nErrors=0;
		
		int nUnknownItems=0; int nUnknownErrors=0;
		
		long startTime =  System.currentTimeMillis();
		for (Context c: testCorpus.enumerate())
		{
			if (!filter(c))
				continue;
			impact.ee.classifier.Instance instance = features.makeTestInstance(c);
			// System.err.println(features.itemToString(item));
			String truth = c.getAttributeAt(taggedAttribute, 0);
			if (truth == null)
			{
				System.out.print("\n");
				continue;
			}
			String word = c.getAttributeAt("word", 0);
			boolean known = knownWords.contains(word);
		
			String outcome = classifier.classifyInstance(instance);
			if (doeEvenRaar)
				outcome = outcome.replaceAll("/.*",  "");
			
			if (useFeedback)
			{
				c.setAttributeAt(taggedAttribute, outcome, 0);
			}
					
			if (!truth.equals(outcome))
			{
				nErrors++;
				if (!known) nUnknownErrors++;
			}
			
		
			// System.err.println(c.getAttributeAt("word", 0) + " " + outcome);
			nItems++;
			if (nItems % 100 ==0)
			{
				System.err.println(features.itemToString(instance));
				System.err.println("nItems: " + nItems + " errors: "  + nErrors / (double) nItems);
			}
			if (!known)
			{
				nUnknownItems++;
			}
			Boolean correct = truth.equals(outcome);
			System.out.println(word + "\t" + outcome + "\t" + truth + "\t"  + correct);
		}
		System.err.println("nItems: " + nItems + 
				" errors: "  + nErrors / (double) nItems);
		// System.err.println("n unknown tems: " + nUnknownItems + 
		//	" errors: "  + nUnknownErrors / (double) nUnknownItems);
		
		long endTime = System.currentTimeMillis();
		long interval = endTime - startTime;
		double secs = interval / 1000.0;
		double wps = nItems / secs;
		System.err.println("tokens " + nItems);
		System.err.println("seconds " + secs);
		System.err.println("tokens per second " + wps);
	}
	
	public static class Trainer
	{
		public static void main(String[] args)
		{
			BasicNERTagger t = new BasicNERTagger();
			SimpleCorpus statsCorpus = new SimpleCorpus(args[0], t.attributeNames);
			t.examine(statsCorpus);
			SimpleCorpus trainingCorpus = new SimpleCorpus(args[0], t.attributeNames);
			t.train(trainingCorpus);
			t.saveModel(args[1]);
		}
	}
	
	public static class Tester
	{
		public static void main(String[] args)
		{
			BasicNERTagger t = new BasicNERTagger();
			SimpleCorpus testCorpus = new SimpleCorpus(args[1], t.attributeNames);
			t.loadModel(args[0]);
			t.test(testCorpus);
		}
	}
	
	@Override
	public HashMap<String, String> apply(Context c) 
	{
		// TODO Auto-generated method stub
		
		HashMap<String,String> m = new HashMap<String,String>();
		//m.put("word", c.getAttributeAt("word", 0));
		
		for (String key: c.getAttributes())
		{
			m.put(key, c.getAttributeAt(key, 0));
		}
		
		if (filter(c))
		{
			impact.ee.classifier.Instance instance = features.makeTestInstance(c);
			String outcome = classifier.classifyInstance(instance);
			m.put(taggedAttribute, outcome);
			if (useFeedback)
			{
				c.setAttributeAt(taggedAttribute, outcome, 0);
			}
		}
		return m;
	}
	
	
	public static void main(String[] args)
	{
		BasicNERTagger t = new BasicNERTagger();
		// 
		t.useFeedback = true;
		t.useLexicon = true;
		
		boolean doTraining = false;
		
		if (doTraining)
		{
			SimpleCorpus statsCorpus = new SimpleCorpus(args[0], t.attributeNames);
			t.examine(statsCorpus);
			SimpleCorpus trainingCorpus = new SimpleCorpus(args[0], t.attributeNames);
			t.train(trainingCorpus);
			t.saveModel("Models/basicTagger");
		}
		
		SimpleCorpus testCorpus = new SimpleCorpus(args[1], t.attributeNames);
		t.loadModel("Models/basicTagger");
		t.test(testCorpus);
	}
}
