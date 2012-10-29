package impact.ee.classifier.svmlight;
import impact.ee.classifier.*;
import impact.ee.classifier.Distribution.Outcome;
import impact.ee.classifier.libsvm.LibSVMClassifier;
import impact.ee.util.Pair;

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

import de.bwaldvogel.liblinear.Model;
import java.io.*;
public class MultiClassifier implements Serializable
{
	private static final long serialVersionUID = 1L;
	public List<SVMLightModel> models = new ArrayList<SVMLightModel>(); // NO!
	public List<String> labels =  new ArrayList<String>();
	Map<Integer,List<Weight>> weightMap = new HashMap<Integer,List<Weight>>();
	
	class  Weight
	{
		int modelNumber;
		double weight;
		
		public Weight(int modelNumber, double weight)
		{
			this.modelNumber = modelNumber;
			this.weight = weight;
		}
	}
	
	public void addModel(SVMLightModel model, String className)
	{
		int modelNumber = labels.size();
		//models.add(model);
		labels.add(className);
		double[] weights = model.getLinearWeights();
		for (int i=0; i < weights.length; i++)
		{
			if (weights[i] != 0)
			{
				List<Weight> w = weightMap.get(i);
				if (w == null)
					weightMap.put(i, w = new ArrayList<Weight>());
				w.add(new Weight(modelNumber,weights[i]));
			}
		}
	}
	
	public String classify(LabeledFeatureVector v)
	{
		double[] delta = new double[this.labels.size()];
		for (int i=0; i < labels.size(); i++)
			delta[i] = 0;
		classify(delta,v);
		String bestLabel=null;
		double best=Double.MIN_VALUE;
		for (int i =0;  i < labels.size(); i++)
		{
			if (delta[i] > best)
			{
				best = delta[i];
				bestLabel = this.labels.get(i);
			}
		}
		return bestLabel;
	}
	
	public void classify(double[] delta, LabeledFeatureVector v)
	{
		for (int i=0; i < v.size(); i++)
		{
			double x = v.getValueAt(i);
			List<Weight> weights = weightMap.get(i);
			if (weights != null)
			{
				for (Weight w: weights)
				{
					delta[w.modelNumber] += v.getFactor() * x;
				}
			}
		}
	}
}