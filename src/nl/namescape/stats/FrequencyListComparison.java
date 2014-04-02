package nl.namescape.stats;
import java.util.*;

import nl.namescape.stats.WordList.TypeFrequency;


/*
 * Vergelijk twee woordenlijsten ("simple maths for keywords")
 * ToDo: iets met documentfrequenties...
 */
public class FrequencyListComparison 
{
	List<Counter> counts = new ArrayList<Counter>();
	boolean includeAll= false;
	static class Counter
	{
		long f1;
		double n1;
		long f2;
		double n2;
		double weight;
		String type;
		double getWeight(int par)
		{
			double r =  ((n1+par)/(n2+par));
			return r;
		}
	}
	
	public static class WeightComparator implements Comparator<Counter> 
	{
		int par=5;
		public WeightComparator(int p) 
		{
			par=p;
		}
		public int compare(Counter a, Counter b) 
		{
			if (a.weight < b.weight)
				return 1;
			if (a.weight == b.weight)
				return 0;
			return -1;
		}
	}
	
	
	public void compare(WordList w1, WordList w2, int par, int threshold)
	{
		List<TypeFrequency> l1 = w1.keyList(true);
		List<TypeFrequency> l2 = w2.keyList(true);
		System.err.println("W1 " + w1.nTokens +  "  W2 " + w2.nTokens);
		 // even een vies hackje omdat w1 niet volledig is?
		for (TypeFrequency t:l1)
		{
			Counter c = new Counter();
			counts.add(c);
			c.f1 = t.frequency;
			c.n1 = 1e6 * c.f1 / (double) w1.nTokens;
			c.f2 = w2.getFrequency(t.type, true);
			c.n2 = 1e6 * c.f2 / (double) w2.nTokens;
			c.type = t.type;
			c.weight = c.getWeight(par);
		}
		
		if (includeAll) for (TypeFrequency t:l2)
		{
			if (w1.getFrequency(t.type, true) > 0)
				continue;
			Counter c = new Counter();
			c.type = t.type;
			c.f1 = 0;
			c.n1 = 0;
			c.f2 = t.frequency;
			c.n2 = 1e6 * t.frequency / (double) w2.nTokens;
			c.weight = c.getWeight(par);
			counts.add(c);
		}
		
		Collections.sort(counts, new WeightComparator(par));
		for (Counter c : counts)
		{
			if (c.f1 > threshold)
				System.out.println(c.type + "\t" + f(c.weight) + "\t" + c.f1 + "=" + f(c.n1) + "\t" + c.f2 + "=" + f(c.n2));
		}
	}
	
	public static String f(double d)
	{
		return String.format("%.1f", d);
	}
	
	public static void main(String[] args)
	{
		WordList w1 = new WordList(args[0]);
		//w1.nTokens = (int) 8e6; // FOEI! een HACK!
		WordList w2 = new WordList(args[1]);
		System.err.println("oink (?) ....");
		FrequencyListComparison fc = new FrequencyListComparison();
		fc.compare(w1, w2, 10, 10); // voor surinaams corpusje nu 10, 10 gebruikt
	}
}

