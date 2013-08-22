package impact.ee.morphology;

import impact.ee.classifier.Distribution;
import impact.ee.classifier.Distribution.Outcome;
import impact.ee.morphology.features.CharacterContextFeature;

import java.util.*;
public class CRFSuite 
{
	public void CRFSuiteOutput(Set<MorphologicalWord> words)
	{
		CharacterContextFeature f = new CharacterContextFeature();
		for (MorphologicalWord w: words)
		{
			int k=0;
			System.out.println("#" + w);
			for (Position p: w.positions)
			{
				String label = p.label;
				Distribution d  = f.getValue(p);
				if (p.morpheme == null)
					continue;
				if (p.morpheme.firstPosition == p.position)
				{
					label = "b-" + p.morpheme.label;
				} else
				{
					label = "i-" + p.morpheme.label;
				}
				System.out.print(label);
				for (Outcome v: d.outcomes)
				{
					System.out.print("\t" + v.label);
				}
				if (k==0)
					System.out.print("\t" + "__BOS__");
				if (k == w.positions.size()-1)
					System.out.print("\t" + "__EOS__");
				k++;
				System.out.print("\n");
			}
			System.out.print("\n\n");
		}
	}
	
	public static void Celex2CRFSuite(String celexFile)
	{
		CelexFile cf = new CelexFile();
		cf.readFromFile(celexFile);
		CRFSuite cso = new CRFSuite();
		cso.CRFSuiteOutput(cf.words);
	}
	
	public static void main(String[] args)
	{
		Celex2CRFSuite(args[0]);
	}
}
