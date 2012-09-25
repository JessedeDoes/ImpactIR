package clitics;

import java.util.HashMap;
import java.util.Map;

import util.TabSeparatedFile;
import lemmatizer.SimpleFeatureSet;
import classifier.Classifier;
import classifier.Dataset;
import classifier.FeatureSet;
import classifier.Instance;
import classifier.libsvm.LibSVMClassifier;
import classifier.weka.WekaClassifier;

public class CliticFinder 
{
	// SMO best so far.. (0.18)
	
	// WekaClassifier weka = new WekaClassifier("functions.SMO", true);
	Classifier classifier = new LibSVMClassifier();
	Dataset d;
	Map<String,String> explanations = new HashMap<String,String>();
	String[] fieldsInTrainingData = {"word", "classLabel", "classDescription"};
	FeatureSet features =  new SimpleFeatureSet();
	
	public void train(Dataset d)
	{
		classifier.train(d);
	}
	
	public void train(String fileName)
	{
		Dataset d = new Dataset("my.data");
		d.features = this.features = new SimpleFeatureSet();
		
		TabSeparatedFile t = new TabSeparatedFile(fileName, fieldsInTrainingData);
		
		while (t.getLine() != null)
		{
			// System.err.println(t.getField("word"));
			d.addInstance(t.getField("word"), t.getField("classLabel"));
			explanations.put( t.getField("classLabel"),  
					t.getField("classDescription"));
		}
		
		train(d);
	}
	
	public void test(String fileName)
	{
		TabSeparatedFile t = new TabSeparatedFile(fileName, fieldsInTrainingData);
		int nItems=0;
		int nErrors=0;
		
		while (t.getLine() != null)
		{
			String s = t.getField("word");
			Instance i = this.features.makeInstance(s, "UNKNOWN");
			String answer = classifier.classifyInstance(i);
			String truth = t.getField("classLabel");
			if (!truth.equals(answer))
			{
				nErrors++;
				System.out.println(t.getField("word") + "\t" + answer + "\t"  +
						explanations.get(answer) + "\t" + t.getField("classDescription"));
			}
			nItems++;
		}
		System.err.println("foutpercentage: " + (nErrors / (double) nItems));
		//train(d);
	}
	
	public static void main(String[] args)
	{
		CliticFinder c = new CliticFinder();
		c.train(args[0]);
		System.err.println("done training......");
		c.test(args[1]);
	}
}
