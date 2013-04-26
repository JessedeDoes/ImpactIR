package nl.namescape.stats;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

//import nl.namescape.stats.TypeFrequency;
//import nl.namescape.stats.ValueComparator;

@XmlRootElement
public class WordList 
{
	private Map<String,Integer> typeFrequency = new HashMap<String,Integer>();
	
	@XmlElement
	private HashMap<String,Integer> caseInsensitiveTypeFrequency = new HashMap<String,Integer>();
	
	@XmlElement
	private List<TypeFrequency> frequencyList = new ArrayList<TypeFrequency>();
	
	private List<TypeFrequency> caseInsensitiveFrequencyList = new ArrayList<TypeFrequency>();
	
	private ValueComparator comparator = new ValueComparator(typeFrequency);
	private ValueComparator comparatorci = new ValueComparator(caseInsensitiveTypeFrequency);
	
	@XmlElement
	private int nTypes = 0;
	public int nTokens = 0;
	
	boolean sorted = false;
	
	public WordList(String fileName)
	{
		readList(fileName);
	}
	
	public WordList() 
	{
		// TODO Auto-generated constructor stub
	}

	public void readList(String fileName)
	{
		try 
		{
			BufferedReader r = new BufferedReader(new 
					InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			String s;
			while ((s = r.readLine()) != null)
			{	
				String[] columns = s.split("\\t");
				
				if (columns.length > 1)
				{
					String w = columns[0];
					int f = Integer.parseInt(columns[1]);
					incrementFrequency(w, f);
				}
			}
			//sortByFrequency();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void incrementFrequency(String s, int f) 
	{
		sorted = false;
		
		//typeFrequency.put(s,f);
		
		nTokens += f;
		Integer x = typeFrequency.get(s);
		int y = (x != null)?x:0;
		typeFrequency.put(s,f+y);
		
		String w = s.toLowerCase();
		x = caseInsensitiveTypeFrequency.get(w);
		y = (x != null)?x:0;
		caseInsensitiveTypeFrequency.put(w,f+y);
	}
	
	public int getFrequency(String w)
	{
		Integer f = caseInsensitiveTypeFrequency.get(w.toLowerCase());
		if (f == null)
			return 0;
		else
			return f;
	}
	
	public int getFrequency(String w, boolean sensitive)
	{
		Map<String,Integer> h = sensitive?this.typeFrequency:caseInsensitiveTypeFrequency;
		Integer f = h.get(sensitive?w:w.toLowerCase());
		if (f == null)
			return 0;
		else
			return f;
	}
	
	public  void sortByFrequency()
	{	
		if (sorted)
		  return;
		
		for (String s:typeFrequency.keySet())
		{
			frequencyList.add(new TypeFrequency(s,typeFrequency.get(s)));
		}
		Collections.sort(frequencyList, comparator);
		for (String s:caseInsensitiveTypeFrequency.keySet())
		{
			caseInsensitiveFrequencyList.add(new TypeFrequency(s,caseInsensitiveTypeFrequency.get(s)));
		}
		Collections.sort(caseInsensitiveFrequencyList, comparatorci);
		sorted = true;
	}
	
	public List<TypeFrequency> keyList()
	{
		sortByFrequency();
		return frequencyList;
	}
	
	public List<TypeFrequency> keyList(boolean sensitive)
	{
		sortByFrequency();
		return sensitive?frequencyList:caseInsensitiveFrequencyList;
	}
	
	public Set<String> keySet(boolean sensitive)
	{
		return sensitive?this.typeFrequency.keySet():this.caseInsensitiveTypeFrequency.keySet();
	}
	
	public static class ValueComparator implements Comparator<TypeFrequency> 
	{

		Map<String,Integer> base;
		
		public ValueComparator(Map<String,Integer> _base) 
		{
			//System.err.println(_base);
			this.base = _base;
		}

		public int compare(TypeFrequency a, TypeFrequency b) 
		{
			if (base == null)
				System.err.println("this is not happening!");
			if(a.frequency < b.frequency) 
			{
				return 1;
			} else if(a.frequency == b.frequency) 
			{
				return a.type.compareTo(b.type);
			} else 
			{
				return -1;
			}
		}
	}
	@XmlElement
	public int getSize()
	{
		return this.typeFrequency.size();
	}
	
	@XmlElement
	public  List<TypeFrequency> getTypeFrequencyList()
	{
		return keyList();
	}
	
	@XmlRootElement
	public static class TypeFrequency
	{	
		@XmlElement
		public
		String type;
		@XmlElement
		public
		int frequency;
		TypeFrequency(String type, int frequency)
		{
			this.type=type;
			this.frequency=frequency;
		}
		TypeFrequency()
		{
			type=null;
		}
	}
}
