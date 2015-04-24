package impact.ee.tagger.features;

import java.io.Serializable;

import impact.ee.classifier.Distribution;
import impact.ee.tagger.Context;
import java.util.*;
import com.radialpoint.word2vec.*;

/**
 * Try to use word2vec word representations as features
 * 
 * @author does
 *
 */
public class WordVectorFeature extends impact.ee.classifier.StochasticFeature implements Serializable
{
	static transient Vectors vectors = null;
	int k=0;
	String vectorFileName = null;
	public static String SonarVectors = "/datalokaal/Corpus/PlainTextDumps/sonar.vectors.bin";
	
	public WordVectorFeature(String vectorFileName, int k)
	{
		readVectors(vectorFileName);
		this.k = k;
		this.name = 	"vectors" + k;
		this.vectorFileName = vectorFileName;
	}
	
	public WordVectorFeature(int k)
	{
		this(SonarVectors,k);
	}

	static synchronized void readVectors(String vectorFileName)
	{
		if (WordVectorFeature.vectors == null)
		{
			System.err.println("reading vectors from " + vectorFileName);
			vectors = ConvertVectors.readVectors(vectorFileName);
		}
	}
	
	private static final long serialVersionUID = 1L;

	@Override
	public Distribution getValue(Object o)
	{
		Context c = (Context) o;
		Distribution d = new Distribution();
		String w = c.getAttributeAt("word", k);
		
       readVectors(SonarVectors); // ugly change, but otherwise model is not transportable without rebuilding
		
		if  (w != null)
		{
			try
			{
				float[] v = vectors.getVector(w);
				for (int i=0; i < v.length; i++)
				{
					float f = v[i];
					d.addOutcome("v" + i, f);
				}
			} catch (Exception e)
			{
				//e.printStackTrace();
			}
		}
		
		return d;
	}
}
