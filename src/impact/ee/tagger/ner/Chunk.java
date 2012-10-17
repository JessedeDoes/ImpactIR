package impact.ee.tagger.ner;

import impact.ee.tagger.Context;

public class Chunk 
{
	public final static  int MAX_LENGTH=10;
	int length;
	String label;
	Context context;
}
