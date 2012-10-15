package impact.ee.tagger.ner;

import impact.ee.classifier.Classifier;
import impact.ee.classifier.Dataset;
import impact.ee.classifier.Feature;
import impact.ee.classifier.FeatureSet;
import impact.ee.tagger.Context;

/**
 * We hope to use some non-local features classification.
 * The relevance of which is less obvious in segmentation...
 * 
 * @author Gebruiker
 *
 */
public class NEClassifier 
{
    Classifier classifier = null;
    FeatureSet features = null;
    
    
    public NEClassifier()
    {
    	
    }
    
    public void train(ChunkedCorpus corpus)
    {
    	Dataset d = new Dataset("trainingCorpus");
		d.features = features;
		
    	for (Context context: corpus.enumerate())
    	{
    		Chunk chunk = corpus.getCurrentChunk();
    		if (chunk != null)
    		{
				d.addInstance(chunk, chunk.label);
    		}
    	}
    	features.finalize();
    	classifier.train(d);
    }
    
    public Feature promoteTaggerFeature(Feature f)
    {
    	Feature f1 = new Feature()
    	{
    		public String getValue(Object o)
    		{
    			return getValue(((Chunk) o).context);
    		}
    	};
    	f1.name = "chunked_" + f.name;
    	return f1;
    }
}
