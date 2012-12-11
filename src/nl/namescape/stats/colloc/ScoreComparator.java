package nl.namescape.stats.colloc;

import java.util.Comparator;

public class ScoreComparator implements Comparator<WordNGram> 
{

	@Override
	public int compare(WordNGram arg0, WordNGram arg1) 
	{
		// TODO Auto-generated method stub
		int r = (int) Math.signum(arg1.score - arg0.score);
		return r;
	}
}
