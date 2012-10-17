package impact.ee.tagger.ner;

import impact.ee.classifier.Distribution;
import impact.ee.classifier.ExistentialFeature;
import impact.ee.util.StringUtils;
import java.util.*;

public class BagOfCharacterNGramsFeature extends ExistentialFeature 
{
	private static final long serialVersionUID = 1L;
	int maxLength=0;
	int minLength=0;
	
	public BagOfCharacterNGramsFeature(int min, int max)
	{
		this.maxLength = max;
		this.minLength = min;
		this.name = "BagOfCharacterNgramFeature" + min + "-" + max;
	}
	
	public Distribution getValue(Object o)
	{
		Chunk c = (Chunk) o;
		Distribution d = new Distribution();
		d.setExistential(true);
		
		String s = c.getText();
		for (int i=0; i < s.length(); i++)
		{
			for (int j=i+minLength; j < s.length() && j-i < maxLength; j++)
			{
				String value = s.substring(i,j);
				d.incrementCount(value);
			}
		}	
		d.computeProbabilities();
		System.err.println(c + " " + d);
		return d;
	}
}
