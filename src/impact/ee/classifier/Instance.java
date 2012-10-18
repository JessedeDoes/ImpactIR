package impact.ee.classifier;
import java.util.*;

public class Instance
{
	public ArrayList<String> values = new ArrayList<String>();
	public ArrayList<Distribution> stochasticValues = new ArrayList<Distribution>();
	public String classLabel;
	public Instance()
	{
		classLabel=null; 
	}

	public void addValue(String s)
	{
		values.add(s);
	}

	public void addStochasticValue(Distribution d)
	{
		stochasticValues.add(d);
	}

	public String toString()
	{
		String z= "{";
		for (int i=0; i < values.size(); i++)
		{
			z += values.get(i);
			if (i < values.size()-1)
			{
				z+= ", ";
			}
		}
		z+= "}";
		if (classLabel != null)
		{
			z += " -> " + classLabel;
		}
		return z;
	}
}
