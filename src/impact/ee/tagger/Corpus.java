package impact.ee.tagger;

import java.util.Iterator;
import java.util.List;

public interface Corpus
{
	public Iterable<Context> enumerate();
}
