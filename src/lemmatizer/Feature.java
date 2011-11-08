package lemmatizer;
import java.util.HashMap;
import java.util.HashSet;

import weka.core.FastVector;

public class Feature
{
  public String name;
  FastVector values = new FastVector();
  java.util.HashSet<String> nominalValues = new HashSet<String>();
  HashMap<String,Integer> valueCounts = new HashMap<String,Integer>();

  int THRESHOLD=4;
 
  public Feature()
  {
  }

  public Feature(String nm)
  {
    name=nm;
  }

  public String getValue(String s)
  { 
    return null;    
  }

  public String storeValue(String instance)
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

class SuffixFeature extends Feature
{
  int k;

  public SuffixFeature(int x)
  {
    k=x;
    name = "s_" + k;
  }
  public String getValue(String s)
  {
    if (s.length() >= k)
    {
       return s.substring(s.length()-k);
    }
    return "";
  }
}


class PrefixFeature extends Feature
{
  int k;

  public PrefixFeature(int x)
  {
    k=x;
    name = "p_" + k;
  }

  public String getValue(String s)
  {
    if (s.length() >= k)
    {
       return s.substring(0,k);
    }
    return "";
  }
}

class  WholeStringFeature extends Feature
{
	int THRESHOLD=0;
	public WholeStringFeature()
	{
	}
	
	public String getValue(String s) // never called???
	{
		//System.err.println(s);
		return s;
	}
}

class  ReversedStringFeature extends Feature
{
	int THRESHOLD=0;
	public ReversedStringFeature()
	{
	}
	
	public String getValue(String s) // never called???
	{
		//System.err.println(s);
		return new StringBuffer(s).reverse().toString();
	}
}