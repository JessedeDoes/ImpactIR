package impact.ee.tagger.ner;

import java.io.IOException;

import impact.ee.classifier.Classifier;
import impact.ee.util.*;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.FeatureSet;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.tagger.Context;
import impact.ee.util.Options;

/**
 * This class just implements the bare skeleton for a NE classifier.<br>
 * In end, we hope to use some non-local features classification,  which are probably less relevant in segmentation...
 * 
 * @author Gebruiker
 *
 */
public class NEClassifier implements java.io.Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Classifier classifier = null;
	FeatureSet features = null;

	public NEClassifier()
	{
		features = new FeatureSet();
		classifier = new SVMLightClassifier();
		NEClassifierFeatures.addBasicFeatures(features);
	}

	public void train(ChunkedCorpus corpus)
	{
		Dataset d = new Dataset("trainingCorpus");
		d.features = features;

		for (Context context: corpus.enumerate())
		{
			Chunk chunk = corpus.getCurrentChunk(); 
			if (chunk != null)
			{
				d.addInstance(chunk, chunk.label);
			}
		}
		features.finalize();
		classifier.train(d);
	}

	public void test(ChunkedCorpus testCorpus)
	{
		int nItems=0; int nErrors=0;
		long startTime =  System.currentTimeMillis();

		for (Context c: testCorpus.enumerate())
		{
			Chunk chunk = testCorpus.getCurrentChunk(); 
			if (chunk != null)
			{
				impact.ee.classifier.Instance instance = features.makeTestInstance(c);
				String outcome = classifier.classifyInstance(instance);
				if (!outcome.equalsIgnoreCase(chunk.label))
					nErrors++;
				nItems++;
			}
		}
		System.err.println("nItems: " + nItems + 
				" errors: "  + nErrors / (double) nItems);
	}

	public static class Trainer
	{
		public static void main(String[] args)
		{
			NEClassifier nec = new NEClassifier();
			BIOCorpus bio = new BIOCorpus(args[0]);
			nec.train(bio);
			try 
			{
				new Serialize<NEClassifier>().saveObject(nec, args[1]);
			} catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public static class Tester
	{
		public static void main(String[] args)
		{
			NEClassifier nec = new Serialize<NEClassifier>().loadFromFile(args[0]);
			BIOCorpus bio = new BIOCorpus(args[0]);
			nec.test(bio);
		}
	}
	
	public static void main(String[] args)
	{

	}
}
