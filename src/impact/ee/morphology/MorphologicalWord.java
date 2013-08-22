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

	void addMorphemes() // ?? last morpheme
	{
		if (this.closed)
			return;
		morphemes.clear();
		Morpheme current = new Morpheme(this);
		morphemes.add(current);
		current.firstPosition = 0;
		
		
		for (int i=0; i < positions.size(); i++)
		{
			Position p = positions.get(i);
			current.text += text.charAt(i);
			if (!p.label.equals(Position.dummyLabel))
			{
				linkMorphemeToPosition(current, p);
				
				if (i < positions.size() -1)
				{
					current = new Morpheme(this);
					morphemes.add(current);
					current.firstPosition = p.position;
					if (i < positions.size() - 1)
						linkMorphemeToPosition(current, positions.get(i+1));
				}
			} else
			{
				p.positionInMorpheme = p.position - current.firstPosition;
				p.morpheme = current;
			}
		}
		closed = true;
		System.out.println(this);
	}

	protected void linkMorphemeToPosition(Morpheme current, Position p) {
		
		current.label = p.label;
		current.lastPosition = p.position;
		p.morpheme = current;
	}
	
	public String toString()
	{
		if (!closed)
			addMorphemes();
		String w = this.text;
		List<String> ana = new ArrayList<String>();
		for (Morpheme m: this.morphemes)
		{
			ana.add(m.toString() + "/" +  m.firstPosition + "-" + m.lastPosition);
		}
		return w + ":  " + StringUtils.join(ana, ", ");
	}
	
	public MorphologicalWord() 
	{
	}
}
