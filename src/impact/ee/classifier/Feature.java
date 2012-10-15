package impact.ee.classifier;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import weka.core.FastVector;

public class Feature implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;
	public FastVector values = new FastVector();
	java.util.HashSet<Object> nominalValues = new HashSet<Object>();
	HashMap<Object,Integer> valueCounts = new HashMap<Object,Integer>();
	
	public int THRESHOLD=2;

	public Feature()
	{
	}

	public Feature(String nm)
	{
		name=nm;
	}

	public String getValue(Object s)
	{ 
		return null;    
	}

	public String storeValueOf(Object instance)
	{
		String s = getValue(instance); 
		addValue(s);
		return s;
	}

	public void addValue(String value) 
	{
		if (!nominalValues.contains(value))
		{
			nominalValues.add(value);
			values.addElement(value);
			valueCounts.put(value,1);
		} else
		{
			valueCounts.put(value,valueCounts.get(value)+1);
		}
	}

	public void pruneValues()
	{
		FastVector newValues = new FastVector();
		for (int i=0; i < values.size(); i++)
		{
			String v = (String) values.elementAt(i);    
			if (valueCounts.get(v) >= THRESHOLD)
			{
				newValues.addElement(v);
			} else
			{
				valueCounts.remove(v);
				// System.err.println("pruning away:" + this.name + "=" + v + " " + valueCounts.get(v));
				newValues.addElement("_UNK"); // logischer toch, anders features die er wel zijn onvergelijkbaar (?? maar waarom maakt het niets uit?)
			}
		}
		values = newValues;
	}

	public String pruneValue(String v)
	{
		if (valueCounts.get(v) >= THRESHOLD)
			return v;
		else return null;
	}
}