package impact.ee.tagger.ner;

import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;

public interface ChunkedCorpus extends Corpus
{
	/*
	public Chunk getCurrentChunk();
	*/
	public Chunk getChunkFromContext(Context context);
}
