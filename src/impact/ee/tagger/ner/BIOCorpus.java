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
		Corpus c = new SimpleCorpus(fileName, BasicNERTagger.attributeNames);
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
	/**
	 * this should return something when the corpus is positioned at the start of the entity
	 */
	@Override
	public Chunk getCurrentChunk() 
	{
		// TODO Auto-generated method stub
		//if (lookahead.)
		if (lookahead == null)
		{
			lookahead = next();
			hasLookahead = true;
		}
		if (lookahead != null)
		{
			String t0 = lookahead.getAttributeAt("tag", 0);
			if (t0.startsWith("B-"))
			{
				String[] parts = t0.split("-");
				Chunk c = new Chunk();
				c.length=1;
				c.label = parts[1];
				for (int i=1; i < Chunk.MAX_LENGTH; i++) // this is rather awful.
				{
					String t = lookahead.getAttributeAt("tag", i);
					if (t.startsWith("I-"))
					{
						c.length++;
					}
				}
				return c;
			}
		}
		return null;
	}
	@Override
	public boolean hasNext() 
	{
		// TODO Auto-generated method stub
		return bio.hasNext();
	}
	@Override
	public Context next() 
	{
		// TODO Auto-generated method stub
		if (hasLookahead)
		{
			hasLookahead = false;
			return lookahead;
		}
		if (!bio.hasNext())
			return null;
		lookahead = bio.next();
		hasLookahead = true;
		return lookahead;
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
