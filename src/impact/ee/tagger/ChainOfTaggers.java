package impact.ee.tagger;

import java.util.*;

public class ChainOfTaggers implements Tagger
{
	private List<Tagger> taggers = new ArrayList<Tagger>();
	
	@Override
	public HashMap<String, String> apply(Context c) 
	{
		// TODO Auto-generated method stub
		// this should not be invoked....
		System.err.println("Never invoke apply on a chain of taggers....");
		System.exit(1);
		return null;
	}

	public void addTagger(Tagger t)
	{
		this.taggers.add(t);
	}
	
	@Override
	
	public Corpus tag(Corpus inputCorpus) 
	{
		// TODO Auto-generated method stub
		Corpus c = inputCorpus;
		for (Tagger t: taggers)
		{
			c = t.tag(c);
		}
		return c;
	}
	
	public static void main(String[] args)
	{
		
	}
}
