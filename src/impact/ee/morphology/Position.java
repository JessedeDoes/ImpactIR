package impact.ee.morphology;

public class Position 
{
	public transient MorphologicalWord baseWord;
	public String label;
	public int position;
	public int positionInMorpheme;
	public Morpheme morpheme=null;
	public static String dummyLabel = "O";
	
	public Position(MorphologicalWord word, int i) 
	{
		this.baseWord = word;
		this.position  = i;
	}
	
	public String toString()
	{
		String w = baseWord.text;
		return w.substring(0,position) + "|" + w.substring(position);
	}
}
