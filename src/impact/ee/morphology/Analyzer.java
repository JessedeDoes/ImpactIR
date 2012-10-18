package impact.ee.morphology;

import impact.ee.classifier.*;
import impact.ee.classifier.libsvm.LibSVMClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.morphology.features.CharacterContextFeature;

import java.util.*;

/**
 * Simpele implementatie van de classificeer-mogelijke-splitsingen aanpak. <br>
 * Bedoeling is gebruik in andere modules.<br>
 * Ik denk dat een morfologische analyse misschien beter met CRF zou kunnen (of eventueel SVM-struct, maar hoe scalable is dat?) 
 *
 * @author Gebruiker
 *
 */
public class Analyzer 
{
	Classifier classifier = new LibSVMClassifier();
	FeatureSet features = new FeatureSet();
	
	public Analyzer()
	{
		features.addStochasticFeature(new CharacterContextFeature());
	}
	
	public void train(Collection<MorphologicalWord> words)
	{
		Dataset d = new Dataset("morphology");
		d.features = features;
		for (MorphologicalWord w: words)
		{
			for (Position p: w.positions)
			{
				d.addInstance(p, p.label);
			}
		}
		features.finalize();
		classifier.train(d);
	}
	
	public void test(Collection<MorphologicalWord> words)
	{
		for (MorphologicalWord w: words)
		{
			for (Position p: w.positions)
			{
				Instance inst = features.makeTestInstance(p);
				String label = classifier.classifyInstance(inst);
				p.label = label;
			}
			System.err.println(w.toString());
		}
	}
	
	public static void main(String[] args)
	{
		CelexFile c0 = new CelexFile();
		c0.readFromFile(args[0]);
		CelexFile c1 = new CelexFile();
		c1.readFromFile(args[1]);
		Analyzer a = new Analyzer();
		a.train(c0.words);
		a.test(c1.words);
	}
}
