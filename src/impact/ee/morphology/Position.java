package impact.ee.morphology;

public class Position 
{
	transient MorphologicalWord baseWord;
	String label;
	int position;
	public static String dummyLabel = "O";
	
	public Position(MorphologicalWord word, int i) 
	{
		this.baseWord = word;
		this.position  = i;
	}
}
