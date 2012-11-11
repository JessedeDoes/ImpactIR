package nl.namescape.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.namescape.Entity;
import nl.namescape.util.XML;


/*
 * Evaluation is easy on:
 * 1) Vertical files with identical tokenization
 * 2) XML tokenized files with identical word id's
 */

public class NEREvaluation 
{
	Counter<String> trueMap = new Counter<String>();
	Counter<String> taggedMap = new Counter<String>();
	Counter<String> truePositiveCounter = new Counter<String>();
	Counter<String> falsePositiveCounter = new Counter<String>();
	Counter<String> falseNegativeCounter = new Counter<String>();
	Set<String> neTypes = new HashSet<String>();
	
	static String ANY = "Segmentation";
	static String ALL = "Overall";
	
	public double getPrecision(String type)
	{
		return 0;
	}
	
	public double getRecall(String type)
	{
		return 0;
	}
	
	public void evaluate(NETaggedDocument truth, NETaggedDocument tagged)
	{
		List<Entity> trueEntities = truth.getEntities();
		List<Entity> taggedEntities = tagged.getEntities();
		
		neTypes.add(ANY); // add a type for segmentation evaluation
		neTypes.add(ALL); // add a type for overall evaluation
		
		for (Entity e: trueEntities)
		{
			String type = e.type;
			neTypes.add(type);
			String location = e.getLocationKey();
			
			trueMap.increment(type);
			Entity e1 = tagged.getEntityAt(location);
			if (e1 != null)
			{
				System.err.println("Found " + e1);
				truePositiveCounter.increment(ANY);
				(e1.type.equals(e.type)?truePositiveCounter:falseNegativeCounter).increment(e.type);
				(e1.type.equals(e.type)?truePositiveCounter:falseNegativeCounter).increment(ALL);
			} else
			{
				System.err.println("Missed: " + e +  " at " + location);
				falseNegativeCounter.increment(ANY);
				falseNegativeCounter.increment(ALL);
				falseNegativeCounter.increment(e.type);
			}
		}
		
		for (Entity e: taggedEntities)
		{
			String type = e.type;
			neTypes.add(type);
			String location = e.getLocationKey();
			taggedMap.increment(type);
			Entity e1 = truth.getEntityAt(location);
			
			if (e1 != null)
			{
				if (!e1.type.equals(e.type)) { falsePositiveCounter.increment(e.type);	falsePositiveCounter.increment(ALL); }	
			} else
			{
				System.err.println("False positive: " + e +  " at " + location);
				falsePositiveCounter.increment(ANY);
				falsePositiveCounter.increment(ALL);
				falsePositiveCounter.increment(e.type);
			}
		}
		printResults();
	}

	public void printResults() 
	{
		 
		List<String> types = new ArrayList<String>();
		types.addAll(neTypes);
		Collections.sort(types);
		
		for (String t: types)
		{
			double precision = truePositiveCounter.get(t) == 0 ? 0 : truePositiveCounter.get(t) 
					/ (double) (truePositiveCounter.get(t) + falsePositiveCounter.get(t));
			double recall = truePositiveCounter.get(t) == 0 ? 0 : truePositiveCounter.get(t) 
					/ (double) (truePositiveCounter.get(t) + falseNegativeCounter.get(t));
			double f1 = 2 * (precision * recall) / (precision + recall);
			System.err.println(t + ": precision "  + precision + ", recall " + recall + " f1: " + f1);
		}
		double classificationAccuracy = truePositiveCounter.get(ALL) / (double) truePositiveCounter.get(ANY);
		System.err.println("Classification accuracy:" + classificationAccuracy);
	}
	
	public static void main(String[] args)
	{
		try
		{
			BIOFile d1 = new BIOFile(args[0]); // new TEIDocument(XML.parse(args[0]));
			BIOFile d2 = new BIOFile(args[1]); // new TEIDocument(XML.parse(args[1]));
			new NEREvaluation().evaluate(d1, d2);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
