package impact.ee.morphology;

import impact.ee.util.TabSeparatedFile;

import java.util.HashSet;
import java.util.Set;

public class SyllableData 
{
	public Set<MorphologicalWord> words = new HashSet<MorphologicalWord>();
	String[] fields = {"word"};

	public void readFromFile(String fileName)
	{
		TabSeparatedFile tsf = new TabSeparatedFile (fileName, fields);
		String[] line;
		while (tsf.getLine() != null)
		{
			String ana1 = tsf.getField("word");
			if (ana1 != null)
				words.add(parse(ana1));
		}
	}

	public MorphologicalWord parse(String ana)
	{
		MorphologicalWord w = new MorphologicalWord();
		try
		{
			String text = "";
			String[] parts = ana.split("/");
			int position=0;
			for (String part: parts)
			{
				
					String morpheme = part;
					text += morpheme;
					String label = "S";
					for (int i = 0; i < morpheme.length(); i++)
					{
						Position p = new Position(w,position + i);
						if (i == morpheme.length() -1)
							p.label = label;
						else
							p.label = Position.dummyLabel;
						w.positions.add(p);
					}
					position += morpheme.length();
				
			}
			w.text = text;
		} catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("!Error parsing " + ana);
		}
		return w;
	}
}
