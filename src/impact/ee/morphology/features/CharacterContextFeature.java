package impact.ee.morphology.features;

import impact.ee.classifier.*;
import impact.ee.morphology.Position;

public class CharacterContextFeature extends ExistentialFeature 
{
	int maxLength=5;
	int minLeft=3;
	int maxLeft=0;
	
	public CharacterContextFeature()
	{
		this.name = "charcontext";
	}
	
	public Distribution getValue(Object o)
	{
		Position p = (Position) o;
		String w = p.baseWord.text;
		Distribution d = new Distribution();
		d.setExistential(true);
		int N = w.length();
		for (int l=p.position - minLeft; l <= p.position+maxLeft && l < N; l++)
		{
			if (l >= 0)
			{
				for (int r=p.position; r - l <= maxLength && r <= N; r++)
				{
					if (r != l)
					{
						String leftPart = w.substring(l,p.position);
						String rightPart = w.substring(p.position,r);
						d.incrementCount(leftPart + "_" + rightPart);
					}
				}
			}
		}
		d.computeProbabilities();
		//System.err.println(p + " -->  " + d);
		return d;
	}
}
