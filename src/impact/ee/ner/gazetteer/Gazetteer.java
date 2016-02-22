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
	private Map<String, Integer> stringTable = new HashMap<String, Integer>();
	private Set<String> allEntries = new HashSet<String>();
	private impact.ee.trie.Trie trie = new Trie();
	private String fileName = null;
	
	public Gazetteer(String fileName)
	{
		this.fileName = fileName;
		readFromFile(fileName);
	}
	
	/**
	 * The scanning stuff does not contribute much if
	 * we want partial matches as features anyway..
	 * @author does
	 *
	 */
	class ScanState
	{
		TrieNode node;
		Set<ScanState> predecessors = new HashSet<ScanState>();
		
		boolean isFinal()
		{
			return node.isFinal;
		}
		
		Set<String> prolongMatches(String[] sentence, int position, Set<String> soFar)
		{
			if (node==trie.root)
				return soFar;
			Set<String> prolongations = new HashSet<String>();
			for (String s: soFar)
			{
				prolongations.add(sentence[position] + " " + s);
			}
			Set<String> V = new HashSet<String>();
			for (ScanState s: predecessors)
			{
				V.addAll(s.prolongMatches(sentence, position-1, prolongations));
			}
			return V;
		}
	}
	
	public boolean hasWord(String w)
	{
		return stringTable.containsKey(w);
	}
	
	public void readFromFile(String fileName)
	{
		nl.openconvert.log.ConverterLog.defaultLog.println("reading gazetteer: " + fileName);
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
		nl.openconvert.log.ConverterLog.defaultLog.println("finished reading gazetteer: " + fileName);
	}
	
	public void insert(String line)
	{
		allEntries.add(line);
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
		Trie.TrieNode node = trie.root;
		for (int i=0; i < codes.length; i++)
		{ 
			Map<Integer,Trie.TrieNode> transitionMap = (Map<Integer,Trie.TrieNode>) 
					node.data;
			if (transitionMap == null)
				node.data = transitionMap = new TreeMap<Integer,Trie.TrieNode>();
			Trie.TrieNode next = transitionMap.get(codes[i]);
			if (next == null)
			{
				next = trie.new TrieNode();
				node.addTransition(codes[i], next);
				transitionMap.put(codes[i], next);
			}
			node=next;
		}
		node.isFinal = true;
	}
	
	private int word2Code(String w)
	{
		Integer i = stringTable.get(w);
		if (i == null)
		{
			i = stringTable.size();
			stringTable.put(w,i);
		}
		return i;
	}
	
	private TrieNode delta(TrieNode n, String w)
	{
		Integer c = stringTable.get(w);
		if (c != null && n.data != null)
		{
			Map<Integer,Trie.TrieNode> transitionMap = (Map<Integer,Trie.TrieNode>) n.data;
			return transitionMap.get(c);
		}
		return null;
	}
	
	private Map<TrieNode,ScanState> delta(Map<TrieNode,ScanState> in, String w)
	{
		Integer c = stringTable.get(w);
		Map<TrieNode,ScanState> out = new HashMap<TrieNode,ScanState>();
		if (c != null)
		{
			for (ScanState s: in.values())
			{
				TrieNode n  = s.node;
				TrieNode next = delta(n,w);
				if (next != null)
				{
					ScanState snext = out.get(next);
					if (snext == null)
					{
						snext = new ScanState();
						snext.node = next;
						out.put(next,snext);
					}
					snext.predecessors.add(s);
				}
			}
		}
		
		ScanState s = new ScanState();
		s.node = trie.root;
		out.put(trie.root,s);
		
		return out;
	}
	
	public void findMatches(String sentence)
	{
		findMatches(sentence.split("\\s+"));
	}
	
	public void findMatches(String[] words)
	{
		Map<TrieNode,ScanState> start = new HashMap<TrieNode,ScanState>();
		ScanState s = new ScanState();
		s.node = trie.root;
		start.put(trie.root, s);
		Map<TrieNode,ScanState> state = start;
		for (int i=0; i < words.length; i++)
		{
			Map<TrieNode,ScanState> next = delta(state,words[i]);
			// nl.openconvert.log.ConverterLog.defaultLog.println(i + " " + next.size());
			for (ScanState snext: next.values())
			{
				if (snext.isFinal())
				{
					Set<String> V = new HashSet<String>();
					V.add("#");
					Set<String> V1 = snext.prolongMatches(words, i, V);
					nl.openconvert.log.ConverterLog.defaultLog.println("Complete Match " + V1);
				} 
			}
			state = next;
		}
	}
	
	public int nEntries()
	{
		return allEntries.size();
	}
	
	public static void main(String[] args)
	{
		Gazetteer g = new Gazetteer(args[0]);
		nl.openconvert.log.ConverterLog.defaultLog.println(g.nEntries());
		g.findMatches("Ik ken Edsger W. Dijkstra heel goed gelukkig !");
	}
}
