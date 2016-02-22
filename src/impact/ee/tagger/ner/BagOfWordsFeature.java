package impact.ee.tagger.ner;

import impact.ee.classifier.Distribution;
import impact.ee.classifier.ExistentialFeature;
import impact.ee.util.StringUtils;
import java.util.*;

public class BagOfWordsFeature extends ExistentialFeature 
{
	private static final long serialVersionUID = 1L;
	int k=0;
	
	public BagOfWordsFeature(int k)
	{
		this.k = k;
		this.name = "BagOfWordsFeature" + k;
	}
	
	public Distribution getValue(Object o)
	{
		Chunk c = (Chunk) o;
		Distribution d = new Distribution();
		d.setExistential(true);
		//nl.openconvert.log.ConverterLog.defaultLog.println(c);
		for (int i=0; i < c.length; i++)
		{
			ArrayList<String> w = new ArrayList<String>();
			for (int j=i; j < c.length && j-i < k; j++)
			{
				w.add(c.context.getAttributeAt("word", j));
				String value = StringUtils.join(w, "_");
				//nl.openconvert.log.ConverterLog.defaultLog.println(value);
				d.incrementCount(value);
			}
		}	
		d.computeProbabilities();
		//nl.openconvert.log.ConverterLog.defaultLog.println(d);
		return d;
	}
}
