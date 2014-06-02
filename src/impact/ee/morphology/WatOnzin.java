package impact.ee.morphology;

import impact.ee.trie.Trie;
import java.io.*;
import java.util.*;

import impact.ee.util.*;

public class WatOnzin
{
	Trie t = new Trie();

	public WatOnzin(String wordList)
	{
		t.loadWordlist(wordList);
		checkWords(wordList);
	}

	public Set<String>  analyses(String l, int minLength)
	{
		 Set<String> A = new HashSet<String>();
		 Trie.TrieNode n = t.root;
		 for (int i=0; i < l.length() -1; i++)
		 {
			 int c = l.charAt(i);
			 n = n.delta(c);
			 if (n == null)
				 break;
			 if (n.isFinal)
			 {
				 String suffix = l.substring(i+1);
				 if (t.hasWord(suffix))
				 {
					 String prefix = l.substring(0,i+1);
					 //System.out.println(prefix + "|" + suffix);
					
					 if (suffix.length() >= minLength && prefix.length() >= minLength)
					 {
						
					     A.add(prefix + "_" + suffix);
					 }
				 }
			 }
		 }
		 return A;
	}
	
	public void checkWords(String  fileName)
	{
		try
		{
			BufferedReader b = new BufferedReader(new FileReader(fileName));
			String l;
			 while ((l = b.readLine()) != null)
			 {
				 //if (l.matches(".*(^|[^aoeiujy])[^aeijuoy]*i[^aeioujy]*i[^aeiujoy]*i[^aeiojuy]*i([^aeiujy]|$).*"))
				 {
					// System.err.println( l);
				 }
				 Trie.TrieNode n = t.root;
				 int nAnalyses = 0;
				 Set<String> A = this.analyses(l,3);
				 if (A.size() > 1)
				 {
					 Set<String> AA = new HashSet<String>();
				
					 for (String  a: A)
					 {
						 boolean ok = true;
						 String[] parts = a.split("_");
						 for (String p: parts)
						 {
							 //System.err.println(p);
							 Set<String> Ap = this.analyses(p,2);
							 if (Ap.size() != 0)
							 {
							   //System.err.println("Dump " + a  + "  : "  + Ap);
							   ok = false;
							 }
						 }
						 if (ok)
							 AA.add(a);
					 }
					 if (AA.size() > 1)
						 System.out.println(l + " " + StringUtils.join(AA, ","));
				 }
			 }
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		String arg = (args.length == 0)?"s:/jesse/nouns.txt":args[0];
		new WatOnzin(arg);
	}
}
