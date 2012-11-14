package impact.ee.morphology;
import impact.ee.util.StringUtils;

import java.util.*;

public class MorphologicalWord 
{
	List<Position> positions = new ArrayList<Position>();
	List<Morpheme> morphemes = new ArrayList<Morpheme>();
	public String text;
	public String PoS;
	boolean closed = false;
	
	public MorphologicalWord(String w)
	{
		this.text = w;
		for (int i=0; i < w.length(); i++)
		{
			Position p = new Position(this,i);
			p.label = "UNK";
			positions.add(p);
		}
	}

	private void addMorphemes()
	{
		morphemes.clear();
		Morpheme current = new Morpheme(this);
		for (int i=0; i < positions.size(); i++)
		{
			Position p = positions.get(i);
			current.text += text.charAt(i);
			if (!p.label.equals(Position.dummyLabel))
			{
				current.label = p.label;
				morphemes.add(current);
				current = new Morpheme(this);
			}
		}
		closed = true;
	}
	
	public String toString()
	{
		if (!closed)
			addMorphemes();
		String w = this.text;
		List<String> ana = new ArrayList<String>();
		for (Morpheme m: this.morphemes)
		{
			ana.add(m.toString());
		}
		return w + ":  " + StringUtils.join(ana, ", ");
	}
	
	public MorphologicalWord() 
	{
	}
}
