package impact.ee.tagger.ner;

import impact.ee.tagger.BasicNERTagger;
import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;
import impact.ee.tagger.SimpleCorpus;

import java.util.*;
public class BIOCorpus implements ChunkedCorpus, Iterator<Context>, Iterable<Context>
{
	Iterator<Context> bio = null;
	boolean hasLookahead = false;
	Context lookahead = null;
	
	public BIOCorpus(String fileName)
	{
		Corpus c = new SimpleCorpus(fileName, BasicNERTagger.defaultAttributeNames);
		bio = c.enumerate().iterator();
	}
	
	public BIOCorpus(Corpus BIOCorpus)
	{
		bio = BIOCorpus.enumerate().iterator();
	}
	@Override
	
	public Iterable<Context> enumerate() // neen, dit werkt dus niet bah....
	{
		// TODO Auto-generated method stub
		return this;
	}
	
	public Chunk getChunkFromContext(Context context)
	{
		String t0 = context.getAttributeAt("tag", 0);
		if (t0 == null)
			return null;
		if (t0.startsWith("B-"))
		{
			String[] parts = t0.split("-");
			Chunk c = new Chunk();
			c.length = 1;
			c.label = parts[1];
			c.context = context;
			for (int i=1; i < Chunk.MAX_LENGTH; i++) // this is rather awful.
			{
				String t = context.getAttributeAt("tag", i);
				if (t != null && t.startsWith("I-"))
				{
					c.length++;
				} else
				{
					break;
				}
			}
			return c;
		}
		return null;
	}
	/**
	 * this should return something when the corpus is positioned at the start of the entity
	 */

	
	@Override
	public boolean hasNext() 
	{
		// TODO Auto-generated method stub
		return bio.hasNext();
	}
	@Override
	public Context next() 
	{
		return bio.next();
	}
	@Override
	public void remove() 
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public Iterator<Context> iterator() 
	{
		// TODO Auto-generated method stub
		return this;
	}
}
