package impact.ee.tagger.ner;

import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;
import java.util.*;
public class NETaggedCorpus implements ChunkedCorpus, Iterator<Context>
{
	Iterator<Context> bio = null;
	boolean hasLookahead = false;
	Context lookahead = null;
	
	public NETaggedCorpus(Corpus BIOCorpus)
	{
		bio = BIOCorpus.enumerate().iterator();
	}
	@Override
	public Iterable<Context> enumerate() // neen, dit werkt dus niet bah....
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Chunk getCurrentChunk() 
	{
		// TODO Auto-generated method stub
		//if (lookahead.)
		return null;
	}
	@Override
	public boolean hasNext() 
	{
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public Context next() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void remove() 
	{
		// TODO Auto-generated method stub
		
	}
}
