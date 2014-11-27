package impact.ee.lexicon;

import java.io.Serializable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WordForm implements Serializable
{
	@XmlElement
	public String lemma = "";
	@XmlElement
	public String wordform = "";
	public String tag = "nil";
	@XmlElement
	public String lemmaPoS = "nil";
	@XmlElement
	public String lemmaID = "nil";
	@XmlElement
	public String neLabel = "nil";
	
	public Integer primaryKey = null; // for databases
	public String modernWordform = "";
	public int wordformFrequency = 0;
	public int lemmaFrequency = 0;

	public String toString()
	{
		String mPart = 
				(modernWordform != null && modernWordform.length() > 0)?
						(", modern: " + modernWordform) 
						:"";
		if (wordformFrequency > 0 || lemmaFrequency > 0)
		{
			return "{" + lemma + "," + wordform + mPart +  ", " +
					tag + ", f(w)=" + wordformFrequency + ", f(l)=" + lemmaFrequency + "}";
		} else
			return "{" + lemma + "," + wordform + mPart + "," + tag + "}";
	}
	
	public String toStringTabSeparated()
	{
		return wordform + "\t" + lemma + "\t" + tag + "\t" + lemmaPoS;
	}
	
	@Override
	public boolean equals(Object other)
	{
		try
		{
			WordForm wf = (WordForm) other;
			return lemma.equals(wf.lemma) && wordform.equals(wf.wordform)
					&& tag.equals(wf.tag) && lemmaPoS.equals(wf.lemmaPoS)
					&& lemmaID.equals(wf.lemmaID);
			
			// OK dit gaat dus fout met de tellertjes...
			// 
		} catch (Exception e)
		{
			return false;
		}
	}

	public int hashCode()
	{
		return lemma.hashCode() + wordform.hashCode() + tag.hashCode();
	}
	
	public WordForm clone()
	{
		WordForm w = new WordForm();
		w.lemma = this.lemma;
		w.wordform = this.wordform;
		w.tag = this.tag;
		w.lemmaPoS = this.lemmaPoS;
		w.lemmaFrequency = this.lemmaFrequency;
		w.lemmaID = this.lemmaID;
		w.modernWordform = this.modernWordform;
		w.neLabel = this.neLabel;
		return w;
	}
}
