package com.radialpoint.word2vec;

import java.util.List;



public class TestVectors
{
	public static void main(String[] args)
	{
		Vectors v = ConvertVectors.readVectors(args[0]);
		try
		{
			float[] aap = v.getVector("aap");
			for (float f: aap)
				System.out.println(f);

			for (String z: v.getVocabulary().keySet())
			{
				String[] aapjes = {z};

				List<Distance.ScoredTerm> scored = Distance.measure(v, 4, aapjes);
				for (Distance.ScoredTerm st: scored)
					System.out.println(z + "\t" + st.getTerm() + "\t" + st.getScore());
			}
		} catch (OutOfVocabularyException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
