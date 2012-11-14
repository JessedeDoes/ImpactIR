package impact.ee.tagger.features.nonlocal;
import impact.ee.classifier.Distribution;
import impact.ee.tagger.Context;
import impact.ee.tagger.Corpus;
import impact.ee.tagger.ner.BIOCorpus;
import impact.ee.tagger.ner.Chunk;
import impact.ee.tagger.ner.ChunkedCorpus;
import impact.ee.util.WeightMap;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

//@XmlRootElement
public class ContextVectorStore 
{
	private int contextSize = 0;
	Map<String, ContextVector> contextMap = new HashMap<String, ContextVector>();
	WeightMap<String> globalTermFrequencies = new WeightMap<String>();
	private int nItems = 0;
	private double nDocs =  0;
	
	public ContextVectorStore(int contextSize)
	{
		this.contextSize = contextSize;
	}

	public void fillContextStore(Corpus c)
	{
		for (Context context: c.enumerate())
		{
			addContext(context);
		}
		for (ContextVector v: contextMap.values())
			setTFIDFWeights(v);
	}
	
	public void fillContextStore(ChunkedCorpus c)
	{
		for (Context context: c.enumerate())
		{
			Chunk chunk = c.getChunkFromContext(context);
			if (chunk != null)
				addChunkContext(context, chunk);
		}
		for (ContextVector v: contextMap.values())
			setTFIDFWeights(v);
	}
	
	public Distribution getContextDistribution(String s)
	{
		ContextVector v = contextMap.get(s);
		if (v != null)
			return v.getDistribution();
		return null;
	}
	
	public void addChunkContext(Context context, Chunk chunk)
	{
		String s = chunk.getText();
		this.nDocs++;
		
		ContextVector v = contextMap.get(s);
		if (v == null)
		{
			contextMap.put(s,v = new ContextVector(s));
		}
		for (int i=-contextSize; i <= contextSize; i++)
		{
			if (i==0) continue;
			String w;
			if (i < 0)
				w = context.getAttributeAt("word", i);
			else
				w = context.getAttributeAt("word", chunk.length + i);
			if (w != null && w.length() > 0)
			{
				globalTermFrequencies.increment(w,1);
				v.termFrequencies.increment(w,positionWeight(i));
			}
		}
	}
	
	public void addContext(Context c)
	{
		String s = c.getAttributeAt("word", 0);
		globalTermFrequencies.increment(s,1);
		ContextVector v = contextMap.get(s);
		if (v == null)
		{
			contextMap.put(s,v = new ContextVector(s));
		}
		
		for (int i=-contextSize; i <= contextSize; i++)
		{
			if (i==0) continue;
			String w = c.getAttributeAt("word", i);
			v.termFrequencies.increment(w,positionWeight(i));
		}
	}

	public double positionWeight(int i)
	{
		return 1;
	}
	/**
	 * idf(d,s) should be length(d) / number of documents countaining s
	 * @param v
	 * @param s
	 */
	public double TFIDFWeight(ContextVector v, String s) // apply tfidf weighting
	{
		double fs = globalTermFrequencies.get(s);
		double size = globalTermFrequencies.keySet().size();
		if (fs == 0)
			return 0;
		double idf = Math.log(size / fs); // nee dit levert niks op, is altijd hetzelfde...
		if (idf < 0)
			idf = 0;
		double tf = v.termFrequencies.get(s) / v.getMaxTermFrequency() ; // globalTermFrequencies.get(v.focusWord);
		return tf * idf;
	}
	
	public void setTFIDFWeights(ContextVector v)
	{
		v.getMaxTermFrequency();
		for (String s: v.termFrequencies.keySet())
		{
			double d = TFIDFWeight(v,s);
			v.termFrequencies.setWeight(s, d);
		}
	}
	
	public void readFromFile(String fileName)
	{
		
	}
	
	public void saveToFile(String fileName)
	{
		try
		{
			PrintWriter p = new PrintWriter(new FileWriter(fileName));
			for (ContextVector v: this.contextMap.values())
			{
				p.println(v);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		ContextVectorStore s = new ContextVectorStore(2);
		BIOCorpus bio = new BIOCorpus(args[0]);
		s.fillContextStore(bio);
		s.saveToFile(args[1]);
	}
}
