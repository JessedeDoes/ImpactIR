package lemmatizer;

import java.util.ArrayList;
import java.util.List;

public class IRLexiconEvaluation 
{
	public List<Item> items = new ArrayList<Item>();
	public int averageRank =0;
	public int nCorrect = 0;
	public int nHistoricalExact = 0;
	public int nModernExact = 0;
	public int modernCoverage = 0;
	
	public class Item
	{
		public String partOfSpeech;
		public String lemma;
		public List<String> lemmata;
		public String wordForm;
		public List<WordMatch> matches;
		public int rankOfCorrectSuggestion;
		public boolean hasCorrectMatch;
	};
	
	public Item addItem(String wordform, List<String> lemmata)
	{
		Item n = new Item();
		n.wordForm = wordform;
		n.lemmata = lemmata;
		n.lemma = util.StringUtils.join(lemmata, "|");
		return n;
	}
	
	public int size()
	{
		return items.size();
	}
}
