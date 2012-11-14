package nl.namescape.evaluation;

import java.util.HashMap;
import java.util.Map;

public class Counter<T> 
{
	private Map<T,Integer> map = new HashMap<T,Integer>();
	
	public int get(T key)
	{
		Integer z = map.get(key);
		if (z==null)
			return 0;
		return z;
	}
	
	public void increment(T key)
	{
		Integer z = get(key);
		map.put(key, z+1);
	}
}
