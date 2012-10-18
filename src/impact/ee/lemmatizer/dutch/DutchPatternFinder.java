package impact.ee.lemmatizer.dutch;
import impact.ee.lemmatizer.*;

/**
 * Instead of bothering to work out a general word frame model in Wicentowski, style,
 * just implement a PatternFinder for Dutch describing normal inflexion
 * 
 * @author Gebruiker
 *
 */
public class DutchPatternFinder implements PatternFinder 
{
	String[] suffixes =
	{
		"en", "d", "t", "de", "te", "s", "'s", "st", "er", "end", "ende", "enden", "e", "ste", "sten", "ere", "eren"	
	};
	
	static String consonants = "[bcdfghjklmnpqrstvwxz]";
	static String vowels = "[aeiouy]";
	enum RegularStemChange
	{
		VOWEL_DOUBLING,
		VOWEL_DOUBLING_WITH_FINAL_DEVOICING, // azen --> aasde
		VOWEL_DEDOUBLING,
		VOWEL_DEDOUBLING_WITH_FINAL_VOICING,
		FINAL_DEVOICING,
		FINAL_VOICING,
		FINAL_CONSONANT_DOUBLING, // tak --> takken
		FINAL_CONSONANT_DEDOUBLING,
	}
	public static abstract class StemChange
	{
		public abstract String transform(String s);
	}
	
	static StemChange VowelDoubling = new StemChange()
	{
		public String transform(String s)
		{
			if (s.matches(consonants  + vowels + consonants + "$"))
			{
				return s.substring(0,s.length()-2) + s.charAt(s.length()-2) + s.charAt(s.length()-2) + s.charAt(s.length()-1);
			}
			return null;
		}
	};
	@Override
	public Pattern findPattern(String a, String b) //  a is word form, b is lemma in applications
	{
		return null;
	}
	
	public static void main(String[] args)
	{
		System.out.println(DutchPatternFinder.VowelDoubling.transform("kas"));
	}
}
