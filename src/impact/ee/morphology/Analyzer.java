package impact.ee.morphology;

import impact.ee.classifier.*;
import impact.ee.classifier.libsvm.LibSVMClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.morphology.features.CharacterContextFeature;

import java.io.IOException;
import java.util.*;
import impact.ee.util.*;

/**
 * Simpele implementatie van de classificeer-mogelijke-splitsingen aanpak. (Van den Bosch, basisidee uiteindelijk teruggaand op de lettergreepsplitser
 * in TeX) <br>
 * Bedoeling is gebruik in andere modules.<br>
 * Ik denk dat een morfologische analyse misschien beter nog met CRF zou kunnen (of eventueel SVM-struct, maar hoe scalable is dat?) 
 *
 * @author Gebruiker
 *
 */
public class Analyzer implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	Classifier classifier = new SVMLightClassifier();
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
			MorphologicalWord w1 = new MorphologicalWord(w.text);
			for (Position p: w1.positions)
			{
				Instance inst = features.makeTestInstance(p);
				String label = classifier.classifyInstance(inst);
				p.label = label;
			}
			if (w1.toString().equals(w.toString()))
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("VERHIP: " + w1);
			}
			System.out.print(w1.toString().equals(w.toString())? "+ ": "- ");
			System.out.println(w1.toString() + " truth: "  + w.toString());
		}
	}
	
	public void saveToFile(String fileName)
	{
		try 
		{
			new Serialize<Analyzer>().saveObject(this, fileName);
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static Analyzer loadFromFile(String fileName)
	{
		return new Serialize<Analyzer>().loadFromFile(fileName);
	}

	public static void main(String[] args)
	{
		CelexFile c0 = new CelexFile();
		c0.readFromFile(args[0]);
		CelexFile c1 = new CelexFile();
		c1.readFromFile(args[1]);
		Analyzer a = new Analyzer();
		a.train(c0.words);
		try
		{
			new Serialize<Analyzer>().saveObject(a, args[0] + ".trainedAnalyzer");
		} catch (Exception e)
		{
			
		}
		a.test(c1.words);
	}
}
