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
			//System.err.println(rev + "-->"  + baseForms.get(s));
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
				System.err.println(line[i] + "->" + test +  " truth:  "+ line[0]);
			}
		}
	}
	
	private void addToMap(String key, String value)
	{
		Set<String> v = baseForms.get(key);
		if (v==null) baseForms.put(key,v = new HashSet<String>());
		v.add(value);
	}
	
	public String transform(String s)
	{
		Trie.TrieNode node = suffixTrie.root;
		Trie.TrieNode deepestNode = null;
		int i=0;
		int suffixLength = 0;
		for (i=0; i < s.length() && node != null; i++)
		{
			
			if (node.isFinal)
			{
				suffixLength = i;
				deepestNode = node;
			}
			//System.err.println(i + ":" + deepestNode);
			node = node.delta(s.charAt(s.length()-i-1));
		}
		if (node==null)
			i--;
		if (node != null && node.isFinal)
		{
			deepestNode = node;
			suffixLength = s.length();
		}	
			
		if (deepestNode != null)
		{
			//System.err.println(i +  " " + s + deepestNode.data);
			String suffix = s.substring(s.length()-suffixLength);
			Set<String> candidates = (Set<String>) deepestNode.data;
			if (candidates != null)
			{
				String replacement = candidates.iterator().next();
				//System.err.println(s + " suffix:"  + suffix);
				return s.replaceAll(suffix + "$", replacement);
			} else
			{
				//System.err.println("DIT KAN DUS NIET" + s);
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
