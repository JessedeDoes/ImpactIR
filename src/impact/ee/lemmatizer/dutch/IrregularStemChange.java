package impact.ee.lemmatizer.dutch;

import impact.ee.trie.Trie;

import java.util.*;

/*
 * Within one class, there should be only one way to reduce.
 * So it is OK to lump, but....
 */
public class IrregularStemChange extends StemChange 
{
	Trie suffixTrie = new Trie();
	
	Map<String,Set<String>> baseForms = new HashMap<String,Set<String>>();
	private String[][] data = null;
	public IrregularStemChange() 
	{
		super(RegularStemChange.IRREGULAR_STEM_CHANGE);
		// TODO Auto-generated constructor stub
	}

	public IrregularStemChange(String[][] data) 
	{
		super(RegularStemChange.IRREGULAR_STEM_CHANGE);
		setData(data);
		// TODO Auto-generated constructor stub
	}

	public void setData(String[][] data)
	{
		this.data = data;
		for (String[] line: data)
		{
			String lemma = line[0];
			for (int i=1; i < line.length; i++)
			{
				addToMap(line[i],line[0]);
			}
		}
		for (String s:  baseForms.keySet())
		{
			String rev = new StringBuffer(s).reverse().toString();
			//nl.openconvert.log.ConverterLog.defaultLog.println(rev + "-->"  + baseForms.get(s));
			suffixTrie.root.putWord(rev, baseForms.get(s));
		}
	}
	
	public void test()
	{
		for (String[] line: data)
		{
			String lemma = line[0];
			for (int i=1; i < line.length; i++)
			{
				String test = this.transform(line[i]);
				nl.openconvert.log.ConverterLog.defaultLog.println(line[i] + "->" + test +  " truth:  "+ line[0]);
			}
		}
	}
	
	private void addToMap(String key, String value)
	{
		Set<String> v = baseForms.get(key);
		if (v==null) baseForms.put(key,v = new HashSet<String>());
		v.add(value);
	}
	
	// moet meerdere mogelijkheden kunnen teruggeven....
	
	public String transform(String s)
	{
		return transform(s,null,false);
	}
	
	public String transform(String s, Set<String> result, boolean multi)
	{
		Trie.TrieNode node = suffixTrie.root;
		Trie.TrieNode deepestNode = null;
		int i=0;
		int suffixLength = 0;
		List<Integer> suffixLengths = new ArrayList<Integer>();
		List<Trie.TrieNode> finalNodes = new ArrayList<Trie.TrieNode>();
		for (i=0; i < s.length() && node != null; i++)
		{
			
			if (node.isFinal)
			{
				suffixLength = i;
				suffixLengths.add(i);
				deepestNode = node;
				finalNodes.add(deepestNode);
			}
			//nl.openconvert.log.ConverterLog.defaultLog.println(i + ":" + deepestNode);
			node = node.delta(s.charAt(s.length()-i-1));
		}
		//nl.openconvert.log.ConverterLog.defaultLog.println(s + ":" + suffixLength);
		if (node != null && node.isFinal)
		{
			deepestNode = node;
			finalNodes.add(deepestNode);
			
			suffixLength = s.length();
			suffixLengths.add(s.length());
		}	
		if (multi)
		{
			
		}
		if (deepestNode != null)
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println(i +  " " + s + deepestNode.data);
			String suffix = s.substring(s.length()-suffixLength);
			Set<String> candidates = (Set<String>) deepestNode.data;
			if (candidates != null)
			{
				String replacement = candidates.iterator().next();
				//nl.openconvert.log.ConverterLog.defaultLog.println(s + " suffix:"  + suffix);
				return s.replaceAll(suffix + "$", replacement);
			} else
			{
				//nl.openconvert.log.ConverterLog.defaultLog.println("DIT KAN DUS NIET" + s);
			}
		}
		return null;
	}
	
	public boolean appliesToPoS(String PoS) { return PoS.contains("VRB"); }
	
	public static void main(String[] args)
	{
		IrregularStemChange c = new IrregularStemChange();
		c.setData(StemAlternations.strongVerbs);
		c.transform("zwang");
		c.transform("beschoten");
		
		//c.test();
	}
}
