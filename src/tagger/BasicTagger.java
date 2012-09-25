package tagger;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Iterator;

import tagger.features.LexiconBasedFeature;
import tagger.features.TaggerFeatures;
import util.Pair;
import util.Serialize;

import classifier.Classifier;
import classifier.Dataset;
import classifier.FeatureSet;
import classifier.Instance;
import classifier.libsvm.LibSVMClassifier;
import classifier.svmlight.SVMLightClassifier;


/*
 * BasicTagger
 * Het lijkt erop dat de libsvm implementatie te langzaam is (?)
 * Vooral tijdens test zou het sneller moeten
 * Dus: terug naar svm-light zoals bij de ocaml tagger? 
 * 
 * The basic tagger is not intended to replace state-of-the art PoS taggers.
 * Its purpose:
 * - use of lexical data in various ways
 * - 
 */
public class BasicTagger implements Serializable
{
	/**
	 * Training op 10000 setje:
	 * SVM: light, all vs all: 0.0929 error; one vs all 0.0857
	 * LibSVM: 0.0735 0.0521
	 * 
	 * Training op 100000 setje
	 * SVM Light all vs all: 
	 */
	
	private static final long serialVersionUID = 1L;
	
	FeatureSet features = new FeatureSet();
	Classifier classifier = new SVMLightClassifier(); // .svmlight.SVMLightClassifier();
	boolean useFeedback = true;
	boolean useLexicon = true;
	
	double proportionOfTrainingToUse = 1;
	
	String[] attributeNames = {"word", "tag"};
	
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
	
	public BasicTagger()
	{
		features = TaggerFeatures.getMoreFeatures(useFeedback);
		if (useLexicon)
		{
			features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(0));
			// context potential PoS does not appear to contribute much
			//features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(1));
			//features.addStochasticFeature(new LexiconBasedFeature.HasPoSFeature(-1));
		}
	}
	
	public void examine(Corpus statsCorpus)
	{
		features.gatherStatistics((Iterator<Object>) statsCorpus);
	}
	
	public void train(Corpus trainingCorpus)
	{		
		Dataset d = new Dataset("trainingCorpus");
		d.features = features;
		
		for (Context c: trainingCorpus.enumerate())
		{
			if (Math.random() <= proportionOfTrainingToUse)
			{
				String answer = c.getAttributeAt("tag", 0);
				d.addInstance(c, answer);
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
	
	public void test(Corpus testCorpus)
	{
		int nItems=0; int nErrors=0;
		int nPoSErrors=0;
		for (Context c: testCorpus.enumerate())
		{
			classifier.Instance instance = features.makeTestInstance(c);
			// System.err.println(features.itemToString(item));
			String outcome = classifier.classifyInstance(instance);
			String truth = c.getAttributeAt("tag", 0);
			
			String word = c.getAttributeAt("word", 0);
			if (useFeedback)
			{
				c.setAttributeAt("tag", outcome, 0);
			}
			if (!truth.equals(outcome))
				nErrors++;
			String truePoS = TaggerFeatures.extractPoS(truth);
			String guessedPoS = TaggerFeatures.extractPoS(outcome);
			if (!truePoS.equals(guessedPoS))
				nPoSErrors++;
			// System.err.println(c.getAttributeAt("word", 0) + " " + outcome);
			nItems++;
			if (nItems % 100 ==0)
			{
				System.err.println(features.itemToString(instance));
				System.err.println("nItems: " + nItems + " errors: "  + nErrors / (double) nItems);
			}
			Boolean correct = truth.equals(outcome);
			System.out.println(word + "\t" + outcome + "\t" + truth + "\t"  + correct);
		}
		System.err.println("nItems: " + nItems + 
				" errors: "  + nErrors / (double) nItems +  
				" PoS errors: "  + nPoSErrors / (double) nItems );
	}
	
	public static void main(String[] args)
	{
		BasicTagger t = new BasicTagger();
		// 
		t.useFeedback = true;
		t.useLexicon = true;
		
		boolean doTraining = true;
		
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
