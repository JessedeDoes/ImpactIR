package impact.ee.tagger;

import java.util.Iterator;

public interface Corpus
{
	public Iterable<Context> enumerate();
}
