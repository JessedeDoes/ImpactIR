package jnisvmlight;
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
import java.util.Map.Entry;




import java.io.*;
public class MultiClassifier implements Serializable
{
	private static final long serialVersionUID = 1L;
	public List<SVMLightModel> models = new ArrayList<SVMLightModel>(); // NO!
	public List<String> labels =  new ArrayList<String>();
	public List<Double> thresholds = new ArrayList<Double>();
	
	Map<Integer,ArrayList<Weight>> weightMap = new HashMap<Integer,ArrayList<Weight>>();
	Map<Integer,Weight[]> weightMap2 = new HashMap<Integer,Weight[]>();
	
	private boolean noLists = false;
	
	class  Weight implements Serializable
	{
		private static final long serialVersionUID = 1L;
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
		if (className == null)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("NULL CLASS NAME " + labels.size());
			//System.exit(1);
			className = "NULL";
		}
		int modelNumber = labels.size();
		//models.add(model);
		labels.add(className);
		thresholds.add(model.m_threshold);
		model.compressLinear();
		Map<Integer,Double> weights = model.getLinearWeightsMap(); // dit werkt dus niet, deze zijn nog niet aangemaakt...
		if (weights == null)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("null weights for class " + className);
			return;
		}
		for (int i: weights.keySet())
		{
			if (weights.get(i) != null && weights.get(i) != 0)
			{
				ArrayList<Weight> w = weightMap.get(i);
				if (w == null)
					weightMap.put(i, w = new ArrayList<Weight>());
				w.add(new Weight(modelNumber,weights.get(i)));
			}
		}
	}
	
	public void noLists()
	{
		if (noLists)
			return;
		weightMap2 = new HashMap<Integer,Weight[]>();
		for (Entry<Integer, ArrayList<Weight>> e: weightMap.entrySet())
		{
			ArrayList<Weight> a = e.getValue();
			Weight[] array = new Weight[a.size()];
			array = a.toArray(array);
			//nl.openconvert.log.ConverterLog.defaultLog.println(e.getKey() + "-->" + array);
			weightMap2.put(e.getKey(), array);
		}
		noLists = true;
		weightMap = null;
	}
	
	public String classify(LabeledFeatureVector v)
	{
		double[] delta = new double[this.labels.size()];
		computeLinearScores(delta,v);
		String bestLabel = null;
		double best = -10e6;
		double bestCheck = best;
		String bestLabelCheck = null;
		//nl.openconvert.log.ConverterLog.defaultLog.println("Classify:"  + labels.size());
		for (int i =0;  i < labels.size(); i++)
		{
			//double check = models.get(i).classify(v);
			//nl.openconvert.log.ConverterLog.defaultLog.println("check" + i + "=" +check  + " "  + bestCheck + " " + bestLabelCheck + "?" + bestLabel + "?" +  	labels.get(i));
			
			//if (check != delta[i])
			//{
			  //nl.openconvert.log.ConverterLog.defaultLog.println(labels.get(i) + ":" + check + "!=" + delta[i]);
			//}
			if (delta[i] > best)
			{
				best = delta[i];
				bestLabel = this.labels.get(i);
			}
			//if (check > bestCheck)
			//{
			//	bestCheck = check;
			//	bestLabelCheck = this.labels.get(i);
			//}
		}
		//if (!bestLabelCheck.equals(bestLabel))
		//{
		//	nl.openconvert.log.ConverterLog.defaultLog.println("Oneenigheid! " + bestLabel + "!" + bestLabelCheck);
		//}
		return bestLabel;
	}
	
	public void computeLinearScores(double[] delta, LabeledFeatureVector v)
	{
		//noLists();
		for (int i=0; i < labels.size(); i++)
			delta[i] = 0;
		
		double f = v.m_factor;
		int l = v.m_dims.length;
		for (int i=0; i < l; i++)
		{
			Weight[] weights = weightMap2.get(v.m_dims[i]);
			if (weights != null)
			{
				double x = v.m_vals[i];
				for (int j=0; j < weights.length; j++)
				{
					Weight w = weights[j];
					delta[w.modelNumber] += f * x * w.weight;
				}
			}
		}
		
		for (int i=0; i < labels.size(); i++)
			delta[i] -= thresholds.get(i);
	}
}