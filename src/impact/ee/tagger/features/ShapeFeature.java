package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;

import java.util.HashSet;
import java.util.Set;


public class ShapeFeature extends Feature 
{

	int k;
	int shapeId;
	
	public ShapeFeature(int shapeId, int k)
	{
		this.k=k;
		this.shapeId = shapeId;
		name = "shape_" + shapeId + "_" + k;
	}

	public String getValue(Object o)
	{
		String s = ((Context) o).getAttributeAt("word", k);	
		return WordShapeClassifier.wordShape(s,shapeId);
	}
	
	public static Set<Feature> getShapeFeatures()
	{
		Set<Feature> shapeFeatures = new HashSet<Feature>();
		for (int i=0; i < 10; i++) // too much...
		{
			shapeFeatures.add(new ShapeFeature(i,0));
			//shapeFeatures.add(new ShapeFeature(i,1));
		}
		return shapeFeatures;
	}
}
