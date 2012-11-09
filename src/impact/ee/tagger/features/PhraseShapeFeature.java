package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;
import impact.ee.util.StringUtils;

import java.util.*;


public class PhraseShapeFeature extends Feature 
{

	int k1, k2;
	int shapeId;
	
	public PhraseShapeFeature(int shapeId, int k1, int k2)
	{
		this.k1=k1;
		this.k2=k2;
		this.shapeId = shapeId;
		name = "shape_" + shapeId + "_" + k1 + "_" + k2;
	}

	public String getValue(Object o)
	{
		List<String> shapes = new ArrayList<String>();
		for (int k=k1; k <= k2; k++)
		{
			String s = ((Context) o).getAttributeAt("word", k);	
		    String shape = WordShapeClassifier.wordShape(s,shapeId);
		    shapes.add(shape);
		}
		return StringUtils.join(shapes,"_");
	}
}