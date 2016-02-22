package nl.inl.syntax;

import java.util.*;

import nl.namescape.evaluation.Counter;

public class ConstructionExamples
{
	public static void main(String[] args)
	{
		AlpinoTreebank atb = new AlpinoTreebank(args[0]);
	
		
		Counter<String> c = atb.getProductionVocabulary();
		//List<String> l = c.keyList();
		nl.openconvert.log.ConverterLog.defaultLog.println("search exampes for: <" + args[1] + ">");
		Set<String> examples = atb.allExamples.get(args[1]);
		if (examples == null)
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("none found...");
			System.exit(0);
		}
		
		for (String s: examples)
		{
			System.out.println(s);
			//System.out.println(s + "\t"  + c.get(s) + "\t" + atb.exampleMap.get(s));
		}
	}
}
