package impact.ee.morphology;

import impact.ee.util.Serialize;

public class SyllableSplitter 
{
	
	
	public static void main(String[] args)
	{
		SyllableData c0 = new SyllableData();
		c0.readFromFile(args[0]);
		SyllableData c1 = new SyllableData();
		c1.readFromFile(args[1]);
		Analyzer a = new Analyzer();
		a.train(c0.words);
		try
		{
			new Serialize<Analyzer>().saveObject(a, args[0] + ".trainedAnalyzer");
		} catch (Exception e)
		{
			
		}
		a.test(c1.words);
	}
}
