package impact.ee.ner.gazetteer;



import impact.ee.trie.Trie;
import impact.ee.trie.Trie.TrieNode;

import java.util.*;
import java.io.*;

/*
 * Features: gazetteerSupport
 * Start
 */

public class Gazetteer 
{
	Map<String, Integer> stringTable = new HashMap<String, Integer>();
	
	impact.ee.trie.Trie trie = new Trie();
	String fileName = null;
	
	public Gazetteer(String fileName)
	{
		this.fileName = fileName;
		readFromFile(fileName);
	}
	
	public boolean hasWord(String w)
	{
		return stringTable.containsKey(w);
	}
	
	public void readFromFile(String fileName)
	{
		System.err.println("reading gazetteer: " + fileName);
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(new File(fileName)));
			String l;
			while ((l = b.readLine()) != null)
			{
				insert(l);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		System.err.println("finished reading gazetteer: " + fileName);
	}
	
	public void insert(String line)
	{
		System.err.println(line);
		String[] words = line.trim().split("\\s+");
		insert(words);
	}
	
	public void insert(String[] words)
	{
		int[] codes = new int[words.length];
		for (int i=0; i < codes.length; i++)
		{
			codes[i] = word2Code(words[i]);
		}
		//trie.root.putWord(codes, null);
	}
	
	public int word2Code(String w)
	{
		Integer i = stringTable.get(w);
		if (i == null)
		{
			i = stringTable.size();
			stringTable.put(w,i);
		}
		return i;
	}
	
	public TrieNode delta(TrieNode n, String w)
	{
		Integer c = stringTable.get(w);
		if (c != null)
		{
			return n.delta(c);
		}
		return null;
	}
	
	public Set<TrieNode> delta(Set<TrieNode> in, String w)
	{
		Integer c = stringTable.get(w);
		Set<TrieNode> out = new HashSet<TrieNode>();
		if (c != null)
		{
			for (TrieNode n: in)
			{
				TrieNode next = n.delta(c);
				if (next != null)
				{
					out.add(next);
				}
			}
		}
		TrieNode x = trie.root.delta(c);
		if (x != null)
			out.add(x);
		return out;
	}
}
