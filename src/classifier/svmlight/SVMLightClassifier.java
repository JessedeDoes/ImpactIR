package classifier.svmlight;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jnisvmlight.KernelParam;
import jnisvmlight.LabeledFeatureVector;
import jnisvmlight.SVMLightModel;
import jnisvmlight.SVMLightInterface;
import jnisvmlight.TrainingParameters;
import libsvm.svm_node;

import classifier.*;
import classifier.Distribution.Outcome;
import classifier.libsvm.LibSVMClassifier;
import de.bwaldvogel.liblinear.Model;
import java.io.*;

import util.Pair;

/*
 * Does not work very well.
 * Maybe bad multiclass combination is the reason, though it did not give problems in the old ocaml tagger.
 *  svm_multilearn -z m training.smaller.svmproblem  Models/
 */
public class SVMLightClassifier implements Classifier, Serializable
{
	transient SVMLightModel model = null;
	transient Dataset currentData = null;
	Map<String,Integer> featureMap = new HashMap<String,Integer>();
	transient Map<Integer,String> inverseFeatureMap = new HashMap<Integer,String>();
	Map<String,Double> labelMap = new HashMap<String,Double>();
	Map<Double,String> inverseLabelMap = new HashMap<Double,String>();
	// classification with training on subsets needs:
	Map<String, Integer> highFeatureMap =  new HashMap<String, Integer>();
	public String targetClass = "pipo";
	Map<String, SVMLightModel> modelMap = new HashMap<String, SVMLightModel>();
	Map<Pair<String,String>,SVMLightModel> AllvsAllMap = new HashMap<Pair<String,String>,SVMLightModel>();
	
	enum TrainingMethod { ONE_VS_ALL, ALL_VS_ALL, EXTERNAL_PROGRAM };
	
	public TrainingMethod trainingMethod = TrainingMethod.ALL_VS_ALL;
	
	public class Problem
	{
		LabeledFeatureVector[] problemData;
		double[] classLabels;
		
		public int size() { return problemData.length; };
		
		public void print(String filename)
		{
			try
			{
				PrintWriter p = new PrintWriter(new FileWriter(filename));
				for (LabeledFeatureVector v:  problemData)
				{
					p.print(inverseLabelMap.get(v.getLabel()) + " ");
					for (int i=0; i < v.size(); i++)
					{
						//p.print(inverseFeatureMap.get(v.getDimAt(i)) + "-->");
						p.print(v.getDimAt(i) + ":" + v.getValueAt(i) + " ");
					}	
					p.println();
				}
				p.close();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String classifyInstance(Instance instance) 
	{
		switch (trainingMethod)
		{
			case ALL_VS_ALL: return classifyAllVsAll(instance); 
			default: return classifyOneVsAll(instance);
		}
	}
	
	public String classifyOneVsAll(Instance instance) 
	{
		// TODO Auto-generated method stub
		LabeledFeatureVector lfv = makeLabeledFeatureVector(instance,false);
		lfv.setLabel(-666);
		double bestScore = Integer.MIN_VALUE;
		String bestLabel = "NONE";
		
		for (String label: modelMap.keySet())
		{
			SVMLightModel m = modelMap.get(label);
			double d = m.classify(lfv);
			
			// d = svmlight.classifyNative(lfv);
			
			if (d > bestScore)
			{
				bestLabel = label;
				bestScore = d;
			}
		}
		return bestLabel;
	}

	@Override
	public Distribution distributionForInstance(Instance i) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void train(Dataset d) 
	{
		// TODO Auto-generated method stub
		currentData = null;
		SVMLightInterface trainer = new SVMLightInterface();
		Problem p = makeProblem(d);

		TrainingParameters tp = new TrainingParameters();
		tp.getKernelParameters().kernel_type = KernelParam.LINEAR;
		// Switch on some debugging output
		tp.getLearningParameters().verbosity = 0; // kan beter uit om gezever te vermijden?
		SVMLightInterface.SORT_INPUT_VECTORS = true;
		switch (trainingMethod)
		{
			case ONE_VS_ALL: trainOneVsAll(trainer, p, tp); break;
			case ALL_VS_ALL: trainAllVsAll(trainer, p, tp); break;
			case EXTERNAL_PROGRAM: trainUsingExternalExecutable(trainer, p, tp);
		}
	}


	public void trainUsingExternalExecutable(SVMLightInterface trainer, Problem p, TrainingParameters tp)
	{
		try 
		{
			File[] modelFiles = SVMLightExec.runTrainingProgram(p);
			for (File mf : modelFiles) 
			{
				try 
				{
					SVMLightModel model = SVMLightModel
							.readSVMLightModelFromURL(mf.toURI().toURL());
					String name = mf.getName();
					name = name.split("\\.")[0];
					modelMap.put(name, model);
					validateModel(model, p, labelMap.get(name));
				} catch (ParseException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) 
		{	
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	public void trainOneVsAll(SVMLightInterface trainer, Problem p,
			TrainingParameters tp) 
	{
		for (String l: this.labelMap.keySet())
		{
			double targetClass = labelMap.get(l);
			trainOne(trainer, p, tp, l, null, targetClass);
		}
	}

	public void trainOne(SVMLightInterface trainer, Problem p,
			TrainingParameters tp, String targetLabel, String otherLabel, double targetClass) 
	{
		for (int j=0; j < p.problemData.length; j++)
			p.problemData[j].setLabel(p.classLabels[j] == targetClass?1:-1);
		System.err.println("Training SVM-light model for " + targetLabel + " " + otherLabel);
		model = trainer.trainModel(p.problemData, tp);
		if (otherLabel != null)
			AllvsAllMap.put(new Pair(targetLabel,otherLabel), model);
		else
			modelMap.put(targetLabel, model);
		for (int j=0; j < p.problemData.length; j++)
			p.problemData[j].setLabel(p.classLabels[j]);
		validateModel(model, p, targetClass); // validate on the training data
		System.err.println("Training done.....");
	}

	public void validateModel(SVMLightModel model, Problem p, double target)
	{
		int errors = 0;
		for (LabeledFeatureVector v: p.problemData)
		{
			double x = model.classify(v);
			boolean shouldBePositive = v.getLabel() == target;
			if (shouldBePositive != (x  >= 0))
			{
				errors++;
			}
		}
		System.err.println("errors for " + inverseLabelMap.get(target) + " = " + errors);
		System.err.println("fitting: "  + (p.size() - errors) / (double) p.size());
	}
	
	public void trainAllVsAll(SVMLightInterface trainer, Problem p,
			TrainingParameters tp)
	{
		for (String l1: this.labelMap.keySet())
		{
			for (String l2: this.labelMap.keySet())
			{
				if (l1.compareTo(l2) > 0)
				{
					List<LabeledFeatureVector> l = new ArrayList<LabeledFeatureVector>();
					double d1 = labelMap.get(l1);
					double d2 = labelMap.get(l2);
					for (LabeledFeatureVector v: p.problemData)
					{
						if (v.getLabel() == d1 || v.getLabel() == d2)
							l.add(v);
					}
					Problem p1 = new Problem();
					p1.problemData = new LabeledFeatureVector[l.size()];
					p1.classLabels = new double[l.size()];
					p1.problemData = l.toArray(p1.problemData);
					for (int i=0; i < p1.size(); i++)
						p1.classLabels[i] = p1.problemData[i].getLabel();
					String combiLabel = l1 + "--" + l2;
					System.err.println("problem size " + p1.size());
					trainOne(trainer,p1,tp,l1,l2,d1); // problem: higher features than seen in restricted set may be found at training time
					for (int i=0; i < p1.size(); i++)
						p1.problemData[i].setLabel(p1.classLabels[i]);
				}
			}
		}
	}
	
	public String classifyAllVsAll(Instance instance)
	{
		LabeledFeatureVector v = makeLabeledFeatureVector(instance,false);
	    double[] scores = new double[this.labelMap.size()+1];
	    for (int i=0; i < scores.length; i++) scores[i] = 0;
	    //System.err.println(instance);
		for (String l1: this.labelMap.keySet())
		{
			for (String l2: this.labelMap.keySet())
			{
				if (l1.compareTo(l2) > 0)
				{
					SVMLightModel m  = AllvsAllMap.get(new Pair<String,String>(l1,l2));
					if (m != null)
					{
						double d = m.classify(v);
						//System.err.println(l1 + " vs "  + l2 + " : " + d);
						int l = (int) Math.round(labelMap.get(d>0?l1:l2));
						scores[l]++;
					}
				}
			}
		}
		double best = -10e6;
		int bestLabel=0;
		for (int i=0; i < scores.length; i++)
		{
			if (scores[i] > best)
			{
				best = scores[i];
				bestLabel = i;
			}
		}
		return inverseLabelMap.get( (double) bestLabel);
	}
	
	public Problem makeProblem(Dataset d) 
	{
		Problem p = new Problem();
		p.problemData = new LabeledFeatureVector[d.size()];
		int i=0;
		for (Instance instance: d.instances)
		{
			LabeledFeatureVector v = makeLabeledFeatureVector(instance,true);
			p.problemData[i] = v;
			i++;
		}
		p.classLabels = new double[p.problemData.length];
		for (int j=0; j < p.problemData.length; j++)
			p.classLabels[j] = p.problemData[j].getLabel();
		return p;
	}

	public LabeledFeatureVector makeLabeledFeatureVector(Instance instance, boolean training) 
	{
		List<svm_node> nodes = new ArrayList<svm_node>();
		double label = handleItem(instance, nodes, training);
		int[] dims = new int[nodes.size()];
		double[] values = new double[nodes.size()];
		for (int j=0; j < nodes.size(); j++)
		{
			svm_node n = nodes.get(j);
			dims[j] = n.index;
			values[j] = n.value;
		}
		LabeledFeatureVector v = new LabeledFeatureVector(label, dims, values);
		// v.normalizeL2(); // misschien moet dit wel niet??
		return v;
	}

	@Override
	public void train(Dataset d, int MAX_ITEMS_USED) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(String fileName) throws IOException 
	{
		// TODO Auto-generated method stub
		try
		{
			new util.Serialize().saveObject(this, fileName);
		}catch(IOException i)
		{
			i.printStackTrace();
		}
	}

	public static SVMLightClassifier loadFromFile(String fileName)
	{
		try
		{
			FileInputStream fileIn =
					new FileInputStream(fileName);
			ObjectInputStream in =
					new ObjectInputStream(fileIn);
			SVMLightClassifier fs = (SVMLightClassifier) in.readObject();
			in.close();
			fileIn.close();
			return fs;
		}catch(Exception i)
		{
			i.printStackTrace();
			return null;
		}
	}
	public void load(String fileName)
	{
		try 
		{
			SVMLightClassifier x = loadFromFile(fileName);
			//this.model = x.model;
			this.featureMap = x.featureMap;
			this.inverseLabelMap = x.inverseLabelMap;
			this.labelMap = x.labelMap;
			this.modelMap = x.modelMap;
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void setType(String classifierType) 
	{
		// TODO Auto-generated method stub
		
	}
	
	// alle svm classifiers gebruiker nu zoiets.
	// gedeeld maken?
	
	public int highFeature(String classLabel)
	{
		Integer i = highFeatureMap.get(classLabel);
		if (i == null)
			return 0;
		return i;
	}
	
	private Double handleItem(Instance instance, List<svm_node> x, boolean training) 
	{
		int m = instance.values.size();
		Double labelN = labelMap.get(instance.classLabel);
		
		if (training && labelN == null)
		{
			double N = labelMap.size();
			labelMap.put(instance.classLabel,N+1);
			inverseLabelMap.put(N+1,instance.classLabel);
			labelN = N+1;
		}
		if (labelN == null)
			labelN = -999.0;
		
		int k=0;

		for (int j=0; j < m; j++) 
		{
			String s = instance.values.get(j);
			String key = makeKey(j, s);
			Integer index = featureMap.get(key);
			if (index == null && training) // FOUT: tijdens predictie alleen bekende features meenemen...
			{
				int N = featureMap.size();
				featureMap.put(key, N+1);
				inverseFeatureMap.put(N+1, key);
				index = N+1;
				if (index > highFeature(instance.classLabel))
					highFeatureMap.put(instance.classLabel, index);
			}
			if (index != null)
			{
				svm_node n = new svm_node();
				x.add(n);

				n.index = index;
				n.value = 1; // TODO do something about real-valued features
				k++;
			}
		}

		//k = 0;
		m = instance.stochasticValues.size();
		for (int j=0; j < m; j++)
		{
			Distribution d = instance.stochasticValues.get(j);
			if (d != null)
			{
				for (Outcome o: d.outcomes)
				{
					String key = makeKeyForStochasticFeature(j,o.label);
					Integer index = featureMap.get(key);
					if (index == null && training) // FOUT: tijdens predictie alleen bekende features meenemen...
					{
						int N = featureMap.size();
						featureMap.put(key, N+1);
						inverseFeatureMap.put(N+1, key);
						index = N+1;
						if (index > highFeature(instance.classLabel))
							highFeatureMap.put(instance.classLabel, index);
					}
					if (index != null)
					{
						svm_node n = new svm_node();
						x.add(n);
						n.index = index;
						n.value = 1; // TODO werkt o.p nou helemaal niet? (pfft dit klopte dus niet!)
						k++;
					}
				}
			}
		}
		Comparator<svm_node> nodeComparator = new Comparator<svm_node>() 
		{
			@Override public int compare(svm_node arg0, svm_node arg1) { return arg0.index - arg1.index; }
		};

		Collections.sort(x, nodeComparator);
		return labelN;
	}

	public String makeKey(int j, String s) 
	{
			return j + ":" + s;
	}
	
	public String makeKeyForStochasticFeature(int j, String s) 
	{
			return j + "/" + s;	
	}
}
