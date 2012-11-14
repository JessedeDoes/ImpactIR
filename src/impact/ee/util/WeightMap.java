package impact.ee.util;

import java.util.HashMap;
import java.util.Map;
import java.util.*;
public class WeightMap<T> 
{
	private Map<T,Double> map = new HashMap<T,Double>();
	
	public Set<T> keySet()
	{
		return map.keySet();
	}
	
	public double get(T key)
	{
		Double z = map.get(key);
		if (z==null)
			return 0;
		return z;
	}
	
	public void increment(T key)
	{
		Double z = get(key);
		map.put(key, z+1);
	}
	
	public void increment(T key, double increment)
	{
		Double z = get(key);
		map.put(key, z+increment);
	}
	
	public void setWeight(T key, double w)
	{
		map.put(key, w);
	}
}
