package impact.ee.morphology;
import java.util.*;

public class MorphologicalWord 
{
	List<Position> positions;
	String baseWord;
	String PoS;
	
	public MorphologicalWord(String w)
	{
		this.baseWord = w;
		for (int i=0; i < w.length(); i++)
		{
			Position p = new Position(this,i);
			p.label = "UNK";
		}
	}

	public MorphologicalWord() 
	{
	}
}
