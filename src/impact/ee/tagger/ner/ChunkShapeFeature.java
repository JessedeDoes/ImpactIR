package impact.ee.tagger.ner;

import impact.ee.classifier.Feature;
import impact.ee.tagger.Context;
import impact.ee.tagger.features.WordShapeClassifier;
import impact.ee.util.StringUtils;

import java.util.*;


public class ChunkShapeFeature extends Feature 
{
	private static final long serialVersionUID = 1L;
	int shapeId;

	public ChunkShapeFeature(int shapeId)
	{
		this.shapeId = shapeId;
		name = "chunkshape_" + shapeId;
	}

	public String getValue(Object o)
	{
		Chunk chunk  = (Chunk) o;
		List<String> shapes = new ArrayList<String>();
		for (int k=0; k < chunk.length; k++)
		{
			String s = chunk.context.getAttributeAt("word", k);	
			String shape = WordShapeClassifier.wordShape(s,shapeId);
			shapes.add(shape);
		}
		return StringUtils.join(shapes,"_");
	}
}