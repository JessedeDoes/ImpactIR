package lemmatizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IRLexiconEvaluation 
{

	@XmlElement(name="item")
	public List<Item> items = new ArrayList<Item>();
	
	@XmlElement
	//public int averageRank =0;
	public int nCorrectSuggestions = 0;
	@XmlElement
	public int nHistoricalExact = 0;
	@XmlElement
	public int nModernExact = 0;
	@XmlElement
	public int nHypothetical = 0;
	@XmlElement
	public int nSuggestions=0;
	@XmlElement
	public int sumOfRanks=0;
	@XmlElement
	public int nItemsWithACorrectSuggestion = 0;
	
	public double averageRankOfFirstCorrectSuggestion = 0; 
	public double recall = 0;
	public double historicalLexiconCoverage=0;
	public double hypotheticalLexiconCoverage=0;
	public double modernLexiconCoverage = 0;
	
	PrintStream report=System.out;
	
	Map<MatchType, Integer> typeHash = new HashMap<MatchType, Integer> ();
	
	private void incrementCount(MatchType m)
	{
		Integer z = typeHash.get(m);
		if (z == null)
			typeHash.put(m,1);
		else
			typeHash.put(m,z+1);
	}
	
	public void matchTypeStatistics()
	{
		for (MatchType m: typeHash.keySet())
		{
			System.err.println(m + ": " + typeHash.get(m));
		}
	}
	
	@XmlRootElement
	public static class Item
	{
		public String partOfSpeech;
		public String lemma;
		public Set<String> lemmata;
		public String wordForm;
		@XmlElement(name="match")
		public List<WordMatch> matches;
		public int rankOfCorrectSuggestion;
		public boolean hasCorrectMatch = false;
		
		boolean inModernLexicon=false;
		boolean inHistoricalLexicon=false;
		boolean inHypotheticalLexicon=false; 
		
		public String matchesAsString()
		{
			List<String> l = new ArrayList<String>();
			for (WordMatch w: matches)
			{
				l.add(w.toString());
			}
			return util.StringUtils.join(l, " || ");
		}
	};
	
	public Item addItem(String wordform, Set<String> lemmata)
	{
		Item n = new Item();
		n.wordForm = wordform;
		n.lemmata = lemmata;
		n.lemma = util.StringUtils.join(lemmata, "|");
		items.add(n);
		return n;
	}
	
	public void calculate()
	{
		double N = items.size();
		
		this.averageRankOfFirstCorrectSuggestion = sumOfRanks / (double) nItemsWithACorrectSuggestion;
		recall = nItemsWithACorrectSuggestion / N;
		historicalLexiconCoverage = this.nHistoricalExact / N;
		modernLexiconCoverage = this.nModernExact / N;
		hypotheticalLexiconCoverage = this.nHypothetical / N;
	}

	public void print(PrintStream p) 
	{
		calculate();
		p.println("####\nItems " + items.size() + ", recall:" + recall);
		p.println("Average rank of first correct suggestion: " + this.averageRankOfFirstCorrectSuggestion  +  " total # suggestions " + nSuggestions);
		p.println("\nHistorical lexicon coverage: " + historicalLexiconCoverage);
		p.println("Modern lexicon coverage: " + modernLexiconCoverage);
		p.println("Hypothetical lexicon coverage: " + hypotheticalLexiconCoverage);
	}
	
	public void matchItem(Item item, List<WordMatch> unsimplifiedMatches)
	{
		ArrayList<WordMatch> wordMatchListUnsimplified = new ArrayList<WordMatch>(unsimplifiedMatches);
		Collections.sort(wordMatchListUnsimplified, new WordMatchComparator());
		List<WordMatch> wordMatchList = WordMatch.simplify(wordMatchListUnsimplified, false);
		
		item.matches = wordMatchList;
		
		//incrementCount(wordMatchList.get(0).type); // this is wrong: should increase count for 'correct' match...
		
		int k=1;
		HashSet<String> seenLemmata = new HashSet<String>();
		boolean germanWildCard = item.lemmata.contains("*****") || item.lemmata.contains("*****");
		
		// loop over the unsimplified match list to find out about coverage
		for (WordMatch wordMatch: unsimplifiedMatches)
		{
			//candidateList += "\t" + wf + "\n";
			String lcLemma = wordMatch.wordform.lemma.toLowerCase();
			if (germanWildCard || item.lemmata.contains(lcLemma)) //  && !seenLemmata.contains(lcLemma)))
			{
				
				if (wordMatch.type==MatchType.HistoricalExact)
					item.inHistoricalLexicon = true;
				if (wordMatch.type==MatchType.ModernExact)
					item.inModernLexicon = true;
				if (wordMatch.type==MatchType.ModernWithPatterns)
					item.inHypotheticalLexicon = true;		
				//incrementCount(wordMatch.type);
			}
		}
		
		// to check the average rank of correct matches, we need the simplified list
		
		for (WordMatch wordMatch: wordMatchList)
		{
			//candidateList += "\t" + wf + "\n";
			String lcLemma = wordMatch.wordform.lemma.toLowerCase();
			nSuggestions++;
			if (germanWildCard || item.lemmata.contains(lcLemma)) //  && !seenLemmata.contains(lcLemma)))
			{
				if (!seenLemmata.contains(lcLemma))
				{
					nCorrectSuggestions++; 
					sumOfRanks += k;
				}
				
				wordMatch.correct = true;
				item.hasCorrectMatch = true;
			}
			seenLemmata.add(lcLemma);
			k++;
		}
		
		if (item.inHistoricalLexicon)
			this.nHistoricalExact++;
		if (item.inModernLexicon)
			this.nModernExact++;
		if (item.inHypotheticalLexicon)
			this.nHypothetical++;
		
		if (item.hasCorrectMatch)
			nItemsWithACorrectSuggestion++;
	}
	
	  public void marshal()
      {
              try
              {
                      JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {
                                      IRLexiconEvaluation.class,
                                      Item.class,
                                      WordMatch.class,
                                      lexicon.WordForm.class});


                      Marshaller marshaller=jaxbContext.createMarshaller();
                      marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true);
                      marshaller.setProperty( Marshaller.JAXB_ENCODING,"UTF-8");
                      marshaller.setProperty( Marshaller.JAXB_FRAGMENT, true);

                      report.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                      /*
                      report.println("<?xml-stylesheet type=\"text/xsl\" href=\"" + stylesheetLocation
                                      + "\"?>");
	*/
                      marshaller.marshal( this, report);
              } catch (Exception e)
              {
                      e.printStackTrace();
              }
      }

	public int size()
	{
		return items.size();
	}
	
}
