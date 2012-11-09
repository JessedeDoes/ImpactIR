package impact.ee.spellingvariation;

import java.util.HashMap;

/**
 * Allows for language or situation-dependent pruning of the possible multigrams
 * (for instance initialize with a set of patterns already known, 
 * in order to reestimate parameters on a new dataset)
 * 
 * Possible approach
 * @author jesse
 *
 */

/**
 * A very simple attempt to arrive at a "reasonable" set of French patterns for g2p (Brulex).
 * <p>
 * The main point is to prevent the pattern weight estimation from arriving at absurd alignments 
 * which will result in bad patterns. 
 * <p>
 * @author jesse
 *
 * TODO: also ok is: any [bdgchjklmpqrstvwz]h
 8 TODO is one side is allvowels, so should other side
 *<pre>
 *Still going wrong:

 *M072933: uitstekendheid  #uitstekendheden#       #uitsteekentheên#      
 *So never allow for consonant <-> vowel pattern
 *vowel  <-> vowel - consonant - vowel should be allowed perhaps
 * [#][ui][t][s][t][e][→e][k][e][n][d→t][h][e][d→ê][e→][n][#]
 * </pre>
 */

public class FrenchG2PMultigramPruner implements MultigramPruner
{

	static String possibleVowels = 
		"AEIOUYaeiouyÀÁÂÃÄÅÆÈÉÊËÌÍÎÏÒÓÔÕÖÙÚÛÜÝàáâãäåæèéêëìíîïòóôõöøùúûüýÿvjVJ";
	
	static String phoneticPossibleVowels =
		"AEIOUYaeiouyÀÁÂÃÄÅÆÈÉÊËÌÍÎÏÒÓÔÕÖÙÚÛÜÝàáâãäåæèéêëìíîòóôõöøùúûüýÿ^_";
	
  public String[] extraGraphemes =
  {
    "gh", "ph", "ch", "ck", "ks", "cks", "sch", "qu", "kw", "kx", "ckx", "cx", "ct", "cs", "sc",
    "bh", "dh", "jh", "kh", "lh", "mh", "qh", "rh", "sh", "th", "" +
     "vh", "wh", "zh", "ng", "nk", "nck",
    "dt"
  };
  
	java.util.Map<Character,Character> vowelMap = new HashMap<Character,Character>();
	java.util.Map<Character,Character> phoneticVowelMap = new HashMap<Character,Character>();
  
  public FrenchG2PMultigramPruner()
  {
    for (int i=0; i < possibleVowels.length(); i++)
    {
      vowelMap.put(possibleVowels.charAt(i), possibleVowels.charAt(i));
    }
    for (int i=0; i < phoneticPossibleVowels.length(); i++)
    {
      phoneticVowelMap.put(phoneticPossibleVowels.charAt(i), phoneticPossibleVowels.charAt(i));
    }
  }

  public boolean isOK(JointMultigram m) // TODO: lhs all vowels <-> rhs all vowels
  {
	if (m.lhs.contains(Alphabet.initialBoundaryString) || m.rhs.contains(Alphabet.finalBoundaryString))
  	{
  		return m.lhs.equals(m.rhs) &&
  		 (m.rhs.equals(Alphabet.initialBoundaryString) || m.rhs.equals(Alphabet.finalBoundaryString)) ;
  	}
    if (m.isSingleton()) 
    	return true;
    if (m.rhs.length() ==1 || m.rhs.equals("ks") || m.rhs.equals("gz")) return true;
    if (m.rhs.length() ==2 && (m.rhs.charAt(0) == 'ü' && isPhoneticVowel(m.rhs.charAt(1))))
     return true;
   return false;
/*
    if (isPossibleGrapheme(m.lhs, true) && isPossibleGrapheme(m.rhs, false)) return 
      (allVowels(m.lhs,true) == allVowels(m.rhs,false));
    return false;
*/
  }

  public boolean allVowels(String s, boolean modern)
  {
    int l = s.length();
    boolean allVowels = true;
    for (int i=0; i < l; i++)
    {
    	if ((modern && (s.charAt(i) == 'j' || s.charAt(i) == 'h')) || s.charAt(i) == 'h' || s.charAt(i) == 'w')

    	{
    		if (! ( (i == l -1)  &&  l > 1 && isPhoneticVowel(s.charAt(i-1))))
    			allVowels=false;
    	} else
      if (!(modern?isPhoneticVowel(s.charAt(i)):isVowel(s.charAt(i))))
      	allVowels=false;
    }    
    if (allVowels) 
    	return true;
    return false; 
  }
 
  public boolean isPossibleGrapheme(String s, boolean modern)
  {
    String sLower = s.toLowerCase();
    if (s.length() == 1 || s.length() == 0) return true;
    if (s.length() == 2 && sLower.charAt(0) == sLower.charAt(1)) return true;
  
    for (int i=0; i < extraGraphemes.length; i++)
    {
    	if (sLower.equals(extraGraphemes[i]))  return true;
    }
    if (allVowels(s,modern)) return true;
    return false;
  }
 

  public boolean isVowel(char s)
  {
    return (vowelMap.containsKey(s));
  }

  public boolean isPhoneticVowel(char s)
  {
    return (phoneticVowelMap.containsKey(s));
  }

	//@Override
	public void applyAbstraction(MultigramSet set)
	{
		// TODO Auto-generated method stub
		
	}
}
