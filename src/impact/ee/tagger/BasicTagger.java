package impact.ee.tagger;

import impact.ee.classifier.Classifier;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.Feature;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.Instance;
import impact.ee.classifier.libsvm.LibSVMClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.tagger.features.HasPoSFeature;
import impact.ee.tagger.features.HasTagFeature;
import impact.ee.tagger.features.ShapeFeature;
import impact.ee.tagger.features.TaggerFeatures;
import impact.ee.util.Pair;
import impact.ee.util.Serialize;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import nl.namescape.util.Options;



//import org.apache.commons.collections.iterators.IteratorEnumeration;




/**
 * 
 * <p>
 * The "basic tagger" is not intended to replace state-of-the art PoS taggers.
 * Its purpose:
 * <ul>
 * <li> Use of lexical data in various ways
 * <li> For certain applications, we need a tagger which does not need sentence boundaries
 * </ul>
 * <p>
 * Als we voor SVM gaan:
 * Het lijkt erop dat de libsvm implementatie van multiclass SVM (te) langzaam 
 * is maar wel betere resultaten levert. 
 * Vooral vooral tijdens testen zou het sneller moeten.
 * <p>
 * <h5>Resultaten</h5>
 *  	
 * <p> Training op 10000 setje<br>
 * SVM: light, all vs all: 0.0929 error; one vs all 0.0857<br>
 * LibSVM: 0.0735 0.0521
 * <p>
 * Training op 100000 setje<br>
 * SVM Light all vs all: 0.047 (Pos 0.0349), one vs all: 0.0493 0.0361<br>
 * LibSVM: 0.0382 (significant beter dus), PoS 0.0264
 * <p>
 * 
 * Al met al zien we libsvm dus steeds betere resultaten halen, alleen hij is zo traag!!!
 *<p>
 * Ter vergelijking: 
 * <ul>
 * <li>TNT op de 100000 haalt 95.51 correct en is dus nog iets beter dan de
 * SVM light tagger.
 * <li>De oude Ocaml tagger (betere features, etc) doet 96.41 correct en is dus nog iets beter
 * dan de libsvm tagger. Bovendien doet ie ongeveer 10.000 tokens per seconde
 * en is dus veel sneller. 
 * <p>
 * Ergo werk aan de winkel! Ik wil de snelheid van svmlight met de kwaliteit van libsvm!
 * </ul>
 */

public class BasicTagger implements Serializable, Tagger
{
	private static final long serialVersionUID = 1L;
	
	FeatureSet features = new FeatureSet();
	Classifier classifier = new SVMLightClassifier(); // .svmlight.SVMLightClassifier();
	boolean useFeedback = true;
	boolean useLexicon = true;
	boolean stripPoS = false;
	boolean useShapes = true;
	boolean logFeatures = false;
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
	
	public BasicTagger()
	{
		this(false);
	}
	
	public BasicTagger(boolean create) // this loads all features --- which is wrong
	{
		if (create)
		{
			initializeFeatures();
		}
	}
	
	public BasicTagger(Properties p)
	{
		if (p.getProperty("tagLexicon") != null)
		{
			TaggerFeatures.setLexiconFileName(p.getProperty("tagLexicon"));
		}
		this.initializeFeatures();
	}

	private void initializeFeatures() {
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
	}
	
	public void examine(Corpus statsCorpus)
	{
		features.gatherStatistics((Iterator<Object>) statsCorpus);
	}
	
	/**
	 * Wat gebeurt er nu precies met zinsgrenzen???? niets????
	 * attributen over de zinsgrens heen lijken niet door te komen....
	 * dat doen ze in de plain text tester schijnbaar ook niet,
	 * in de XML versie weer wel.....
	 * @param trainingCorpus
	 */
	
	public void train(Corpus trainingCorpus)
	{		
		Dataset d = new Dataset("trainingCorpus");
		d.features = features;
		
		for (Context c: trainingCorpus.enumerate())
		{
			if (Math.random() <= proportionOfTrainingToUse)
			{
				if (filter(c))
				{
					String answer = c.getAttributeAt(taggedAttribute, 0);
					if (answer != null)
					{
						d.addInstance(c, answer);
					}
					knownWords.add(c.getAttributeAt("word", 0));
				}
			}
		}
		
		features.finalize(); // oehoeps, dit is niet fijn, dat dat expliciet moet, moet anders...
		//d.pruneInstances();
		
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
		return (c.getAttributeAt("word",0) != null);
	}

	public void test(Corpus testCorpus)
	{
		int nItems=0; int nErrors=0;
		int nPoSErrors=0;
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
				continue;
			String word = c.getAttributeAt("word", 0);
			boolean known = knownWords.contains(word);
		
			String outcome = classifier.classifyInstance(instance);

			if (useFeedback)
			{
				c.setAttributeAt(taggedAttribute, outcome, 0);
			}
			
			if (stripPoS)
			{
				truth = truth.replaceAll(".*_", "");
				outcome = outcome.replaceAll(".*_", "");
			}
			
			if (truth != null && !truth.equals(outcome))
			{
				nErrors++;
				if (!known) nUnknownErrors++;
			}
			String truePoS = TaggerFeatures.extractPoS(truth);
			String guessedPoS = TaggerFeatures.extractPoS(outcome);
			if (!truePoS.equals(guessedPoS))
				nPoSErrors++;
			// System.err.println(c.getAttributeAt("word", 0) + " " + outcome);
			nItems++;
			if (nItems % 100 ==0)
			{
				//System.err.println(features.itemToString(instance));
				//System.err.println("nItems: " + nItems + " errors: "  + nErrors / (double) nItems);
			}
			if (!known)
			{
				nUnknownItems++;
			}
			Boolean correct = truth.equals(outcome);
			String extra = logFeatures?("\t" + features.itemToString(instance)): "";
			System.out.println(word + "\t" + outcome + "\t" + truth + "\t"  + correct + extra);
		}
		System.err.println("nItems: " + nItems + 
				" errors: "  + nErrors / (double) nItems +  
				" PoS errors: "  + nPoSErrors / (double) nItems );
		System.err.println("n unknown tems: " + nUnknownItems + 
				" errors: "  + nUnknownErrors / (double) nUnknownItems);
		
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
			Options o = new Options(args) 
			{
				public void defineOptions()
				{
					this.options.addOption("l", "lexicon", true, "lexicon");
				}
			};
			args = o.commandLine.getArgs(); // need to do this in more places!
			if (o.getOption("lexicon") != null)
			{
				TaggerFeatures.setLexiconFileName(o.getOption("lexicon"));
			}
			BasicTagger t = new BasicTagger(true);
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
			BasicTagger t = new BasicTagger(false);
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
			//System.err.println(features.itemToString(instance));
			String outcome = classifier.classifyInstance(instance);
			m.put(taggedAttribute, outcome);
			if (useFeedback)
			{
				c.setAttributeAt(taggedAttribute, outcome, 0);
			}
			if (logFeatures)
				m.put("features", features.itemToString(instance));
		} else
		{
			c.setAttributeAt(taggedAttribute, "O", 0);
		}
		return m;
	}


	@Override
	public Corpus tag(Corpus inputCorpus) 
	{
		// TODO Auto-generated method stub
		OutputEnumeration out = new OutputEnumeration(this,inputCorpus);
		EnumerationWithContext ewc = 
				new EnumerationWithContext(Map.class, out, new DummyMap());
		
		return new SimpleCorpus(ewc);
	}
	
	public static void main(String[] args)
	{
		BasicTagger t = new BasicTagger();
		
	
		
		SimpleCorpus testCorpus = new SimpleCorpus(args[1], t.attributeNames);
		t.loadModel(args[0]);
		for (Context c: t.tag(testCorpus).enumerate())
		{
			System.out.println(c.getAttributeAt("word", 0) + "\t" + c.getAttributeAt("tag", 0));
		}
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub
		
	}
}
