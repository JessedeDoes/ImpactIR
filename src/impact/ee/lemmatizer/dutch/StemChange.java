package impact.ee.lemmatizer.dutch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Still missing
 * {azen,aast,VRB(mai,ind,pres,2/3,sg,int)}
 * // denk aan gentiaan en leguaan... spons en sponzen... {denunciëren,denuncieerden,VRB(mai,ind,impf,1/2/3,pl,trs)}
 * {managen,managet,VRB(mai,ind,pres,2/3,sg,trs)}
 * {onbruikbaar,onbruikbaarder,ADJ(quali,com,basic)}
 * 
 * denk aan s-merge-in
 * heest bij hees etc...
 * 
 * 
 * Let ook op gevallen als {opschorten,opschortten,VRB(mai,ind,impf,1/2/3,pl,trs)} 
 * waarbij we niet verdubbeling met -en mogen doen, dus eigenlijk moeten we de suffix goed indelen enzo....
 * @author Gebruiker
 *
 */
public abstract class StemChange 
{
	public abstract String transform(String s);
	static Set<StemChange> PossibleStemChanges = new HashSet<StemChange>();
	static Map<RegularStemChange, StemChange> changeMap = new HashMap<RegularStemChange, StemChange>();
	public RegularStemChange type;
	
	
	public StemChange(RegularStemChange x)
	{
		this.type = x;
		PossibleStemChanges.add(this);
		changeMap.put(type,this);
	}
	
	public boolean appliesToPoS(String PoS) { return true; }
	
	static enum RegularStemChange
	{
		IDENTITY,
		VOWEL_DOUBLING,
		VOWEL_DOUBLING_WITH_FINAL_DEVOICING, // azen --> aasde
		VOWEL_DEDOUBLING,
		VOWEL_DEDOUBLING_WITH_FINAL_VOICING,
		FINAL_DEVOICING,
		FINAL_VOICING,
		FINAL_CONSONANT_DOUBLING, // tak --> takken
		FINAL_CONSONANT_DEDOUBLING,
		VRD_VR,
		IRREGULAR_STEM_CHANGE
	}

	static String anything = ".*";
	static String consonants = "[bcdfghjklmnpqrstvwxz]";
	static String vowels = "[aeiouy]";
	static String voicedConsonants = "[bdvz]";
	static String markedUnvoicedConsonants = "[ptfs]";
	static String possibleUI = "[ui]?";
	
	
	// Let op woordbegin hier en daar bij consonants...
	
	static StemChange Identity = new StemChange(RegularStemChange.IDENTITY)
	{
		public String transform(String s)
		{
			return s;
		}
	};
	
	static StemChange VowelDoubling = new StemChange(RegularStemChange.VOWEL_DOUBLING) 
	{
		public String transform(String s)
		{
			if (s.matches(anything + consonants  + possibleUI +  vowels + consonants + "$"))
			{
				return s.substring(0,s.length()-2) + s.charAt(s.length()-2) + s.charAt(s.length()-2) + s.charAt(s.length()-1);
			}
			return null;
		}
	};
	
	static StemChange VowelDoublingWithFinalDevoicing = new StemChange(RegularStemChange.VOWEL_DOUBLING_WITH_FINAL_DEVOICING)
	{
		public String transform(String s)
		{
			if (s.matches(anything + consonants  + possibleUI + vowels + voicedConsonants + "$"))
			{
				return s.substring(0,s.length()-2) + s.charAt(s.length()-2) + s.charAt(s.length()-2) + devoice(s.charAt(s.length()-1));
			}
			return null;
		}
	};
	
	static StemChange VowelDedoubling = new StemChange(RegularStemChange.VOWEL_DEDOUBLING)
	{
		public String transform(String s)
		{
			if (s.matches(anything + consonants  + possibleUI + vowels + vowels + consonants + "$"))
			{
				char v1 = s.charAt(s.length()-3);
				char v2 = s.charAt(s.length()-2);
				if (v1 == v2)
				{
					return  s.substring(0,s.length()-3) + v1 + (s.charAt(s.length()-1));
				}
			}
			return null;
		}
	};
	
	static StemChange VowelDedoublingWithFinalVoicing = new StemChange(RegularStemChange.VOWEL_DEDOUBLING_WITH_FINAL_VOICING)
	{
		public String transform(String s)
		{
			if (s.matches(anything + consonants  + possibleUI + vowels + vowels + markedUnvoicedConsonants + "$"))
			{
				char v1 = s.charAt(s.length()-3);
				char v2 = s.charAt(s.length()-2);
				if (v1 == v2)
				{
					return s.substring(0,s.length()-3) + v1 + voice(s.charAt(s.length()-1));
				}
			}
			return null;
		}
	};
	
	static StemChange FinalDevoicing = new StemChange(RegularStemChange.FINAL_DEVOICING)
	{
		public String transform(String s)
		{
			if (s.matches(anything + voicedConsonants + "$"))
			{
				return s.substring(0,s.length()-1) + devoice(s.charAt(s.length()-1));
			}
			return null;
		}
	};
	
	static StemChange FinalVoicing = new StemChange(RegularStemChange.FINAL_VOICING)
	{
		public String transform(String s)
		{
			if (s.matches(anything + markedUnvoicedConsonants + "$"))
			{
				return s.substring(0,s.length()-1) + voice(s.charAt(s.length()-1));
			}
			return null;
		}
	};
	
	static StemChange FinalConsonantDoubling = new StemChange(RegularStemChange.FINAL_CONSONANT_DOUBLING)
	{
		public String transform(String s)
		{
			if (s.matches(anything + vowels + consonants + "$"))
			{
				return s.substring(0,s.length()) +s.charAt(s.length()-1);
			}
			return null;
		}
	};
	
	static StemChange FinalConsonantDeDoubling = new StemChange(RegularStemChange.FINAL_CONSONANT_DEDOUBLING)
	{
		public String transform(String s)
		{
			if (s.matches(anything + vowels + consonants + consonants + "$"))
			{
				char c1 = s.charAt(s.length()-1);
				char c2 = s.charAt(s.length()-2);
				if (c1 == c2)
					return s.substring(0,s.length()-1);
			}
			return null;
		}
	};
	
	static StemChange RD_R = new StemChange(RegularStemChange.VRD_VR)
	{
		public String transform(String s)
		{
			if (s.matches(anything + vowels + "rd" + "$"))
			{
				return s.substring(0,s.length()-1);
			}
			return null;
		}
		public boolean appliesToPoS(String PoS) { return PoS.contains("ADJ"); }
	};
	
	static StemChange strongVerbs = new IrregularStemChange(StemAlternations.strongVerbs);
	
	public static char devoice(char c)
	{
		switch (c)
		{
			case 'b': return 'p';
			case 'd': return 't';
			case 'v': return 'f';
			case 'z': return 's';
		}
		return c;
	}
	
	public static char voice(char c)
	{
		switch (c)
		{
			case 'p': return 'b';
			case 't': return 'd';
			case 'f': return 'v';
			case 's': return 'z';
		}
		return c;
	}
	
	public static Set<StemChange> getAll()
	{
		return StemChange.PossibleStemChanges;
	}
	
	public static StemChange getStemChange(RegularStemChange type)
	{
		StemChange r = changeMap.get(type);
		if (r == null)
			nl.openconvert.log.ConverterLog.defaultLog.println("Type " + type + " not known!");
		return changeMap.get(type);
	}
	
	public static void main(String[] args)
	{
		System.out.println(StemChange.FinalDevoicing.transform("kaz"));
		System.out.println(StemChange.VowelDedoubling.transform("kaas"));
		System.out.println(StemChange.VowelDedoublingWithFinalVoicing.transform("kaas"));
		System.out.println(StemChange.FinalConsonantDoubling.transform("kaas"));
	}
}

