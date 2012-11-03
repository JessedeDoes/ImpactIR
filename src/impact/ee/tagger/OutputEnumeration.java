package impact.ee.tagger;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implement the tagger as a filter, corpus in, corpus out
 * @author does
 *
 */
public class OutputEnumeration implements Enumeration<Map<String,String>>
{
	private final Tagger tagger;
	Corpus testCorpus;
	Iterator<Context> inputIterator;

	public OutputEnumeration(Tagger tagger, Corpus crp)
	{
		this.tagger = tagger;
		testCorpus = crp;
		inputIterator= crp.enumerate().iterator();
	}

	@Override
	public boolean hasMoreElements() 
	{
		// TODO Auto-generated method stub
		return inputIterator.hasNext();
	}

	@Override
	public Map<String, String> nextElement() 
	{
		// TODO Auto-generated method stub
		try
		{
			Context c = inputIterator.next();
			HashMap<String, String> m = tagger.apply(c);
			return m;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}