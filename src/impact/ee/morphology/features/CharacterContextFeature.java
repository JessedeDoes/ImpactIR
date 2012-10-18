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
	
	private int posplus(Position p)
	{
		return p.position + 1;
	}
	
	public Distribution getValue(Object o)
	{
		Position p = (Position) o;
		String w = "^" + p.baseWord.text + "$";
		Distribution d = new Distribution();
		d.setExistential(true);
		int N = w.length();
		for (int l=posplus(p) - minLeft; l <= posplus(p)+maxLeft && l < N; l++)
		{
			if (l >= 0)
			{
				for (int r=posplus(p); r - l <= maxLength && r <= N; r++)
				{
					if (r != l)
					{
						String leftPart = w.substring(l, posplus(p));
						String rightPart = w.substring(posplus(p), r);
						d.incrementCount(leftPart + "|" + rightPart);
					}
				}
			}
		}
		d.computeProbabilities();
		//System.err.println(p + " -->  " + d);
		return d;
	}
}
