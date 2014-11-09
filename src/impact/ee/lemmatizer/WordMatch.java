package impact.ee.lemmatizer;
import impact.ee.lemmatizer.IRLexiconEvaluation.Item;
import impact.ee.lexicon.InMemoryLexicon;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
@XmlRootElement
public class WordMatch
{
	@XmlElement
	public impact.ee.lexicon.WordForm wordform;
	@XmlElement
	public double matchScore;
	String target;
	@XmlTransient
	public String alignment = "";
	@XmlElement
	public String getAlignment() { return alignment.replace("->", "/"); }
	@XmlAttribute(name="type")
	public MatchType type;
	
	InMemoryLexicon lexicon;
	int lemmaFrequency=0;
	int wordformFrequency=0;
	@XmlAttribute(name="correct")

	public boolean correct = false;
	@XmlAttribute(name="rank")
	int rank=0;
	
	public String toString()
	{
		if (type == MatchType.ModernWithPatterns)
			//return "{" + wordform + ", " + type + ", " + String.format("%2.2e", matchScore) + ", " + alignment + "}";
			return "{" + wordform + ", " + type + ", " + alignment + "}";
		else
			return "{" + wordform + ", " + type + "}";
	}

	public int hashCode()
	{
		return wordform.hashCode() + alignment.hashCode();
	}

	public boolean equals(Object o)
	{
		try
		{
			WordMatch wm = (WordMatch) (o);
			return (wm.wordform.equals(wordform) && wm.type == type && wm.alignment.equals(alignment));
		} catch (Exception e)
		{
			return false;
		}
	}
	
	/*
	 * remove assignments with identical lemma and part of speech, or
	 * simply with identical lemma (case insensitive)
	 */
	
	public static List<WordMatch> simplify(List<WordMatch> set, boolean usePartOfSpeech)
	{
		List<WordMatch> simple = new ArrayList<WordMatch>();
		for (WordMatch wm: set)
		{
			boolean found = false;
			for (WordMatch wm1: simple)
			{
				if (wm1.wordform.lemma.equalsIgnoreCase(wm.wordform.lemma) && 
						(!usePartOfSpeech || wm1.wordform.lemmaPoS.equals(wm.wordform.lemmaPoS)))
				{
					wm1.lemmaFrequency += wm.lemmaFrequency;
					wm1.wordformFrequency += wm.wordformFrequency;
					found = true;
					break;
				}  
			}
			if (!found)
			{
				simple.add(wm);
			}
		}
		return simple;
	}
	
	public void marshal(PrintStream out)
	{
		try
		{
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] {
					IRLexiconEvaluation.class,
					Item.class,
					WordMatch.class,
					impact.ee.lexicon.WordForm.class});


			Marshaller marshaller=jaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.setProperty( Marshaller.JAXB_ENCODING,"UTF-8");
			marshaller.setProperty( Marshaller.JAXB_FRAGMENT, true);

			// out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

			marshaller.marshal( this, out);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}

