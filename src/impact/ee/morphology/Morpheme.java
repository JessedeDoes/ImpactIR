package impact.ee.morphology;

public class Morpheme 
{
	MorphologicalWord baseWord;
	String text="";
	String label;
	int firstPosition;
	int lastPosition;
	
	public Morpheme(MorphologicalWord w)
	{
		this.baseWord = w;
	}
	public String toString()
	{
		return "(" + text + ","  + label + ")";
	}
}
