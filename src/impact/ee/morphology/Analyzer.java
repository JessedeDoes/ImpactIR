package impact.ee.morphology;

import impact.ee.classifier.*;
import impact.ee.classifier.svmlight.SVMLightClassifier;

import java.util.*;

/**
 * Simpele implementatie van de classificeer-mogelijke-splitsingen aanpak.
 *Ik denk dat een morfologische analyse goed met CRF zou kunnen (of eventueel SVM-struct, maar hoe scalable is dat?) 
 *
 * @author Gebruiker
 *
 */
public class Analyzer 
{
	Classifier classifier = new SVMLightClassifier();
	FeatureSet features = new FeatureSet();
	
	public void train(Set<MorphologicalWord> words)
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
	
	public void test(Set<MorphologicalWord> words)
	{
		for (MorphologicalWord w: words)
		{
			for (Position p: w.positions)
			{
				Instance inst = features.makeTestInstance(p);
				String label = classifier.classifyInstance(inst);
				p.label = label;
			}
		}
	}
}
