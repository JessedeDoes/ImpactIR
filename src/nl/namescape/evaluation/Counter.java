package nl.namescape.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.namescape.stats.WordList.TypeFrequency;

public class Counter<T> extends ConcurrentHashMap<T,Integer>
{
	public int getSumOfCounts()
	{
		int x=0;
		for (Integer i: this.values())
			x += i;
		return x;
	}
	
	@Override
	public Integer get(Object key)
	{
		Integer z = super.get(key);
		if (z == null)
			return 0;
		//nl.openconvert.log.ConverterLog.defaultLog.println("Found " + key + " --> " + z);
		return z;
	}
	
	public void increment(T key)
	{
		Integer z = get(key);
		super.put(key, z+1);
	}
	
	public void increment(T key, int amount)
	{
		Integer z = get(key);
		super.put(key, z+amount);
	}
	
	public class CompareCounts implements Comparator<T> 
	{
		public int compare(T a, T b) 
		{
			if (a == null || b == null)
				throw new NullPointerException();
			int cA = get(a);
			int cB = get(b);
			return (int) Math.signum(cB-cA);
		}
	}
	
	public List<T> keyList()
	{
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		List<T> l = new ArrayList<T>();
		l.addAll(this.keySet());
		CompareCounts v =  new CompareCounts();
		Collections.sort(l, v);
		return l;
	}
}
