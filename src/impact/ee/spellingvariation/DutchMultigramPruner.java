package impact.ee.spellingvariation;

import java.util.HashMap;

/**
 * A very simple attempt to arrive at a "reasonable" set of Dutch patterns.
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
public class DutchMultigramPruner implements MultigramPruner
{

	static String possibleVowels = 
		"AEIOUYaeiouyÀÁÂÃÄÅÆÈÉÊËÌÍÎÏÒÓÔÕÖÙÚÛÜÝàáâãäåæèéêëìíîïòóôõöøùúûüýÿvjVJ";
	
	static String modernPossibleVowels =
		"AEIOUYaeiouyÀÁÂÃÄÅÆÈÉÊËÌÍÎÏÒÓÔÕÖÙÚÛÜÝàáâãäåæèéêëìíîïòóôõöøùúûüýÿ";
	
  public String[] extraGraphemes =
  {
    "gh", "ph", "ch", "ck", "ks", "cks", "sch", "qu", "kw", "kx", "ckx", "cx", "ct", "cs", "sc",
    "bh", "dh", "jh", "kh", "lh", "mh", "qh", "rh", "sh", "th", "" +
     "vh", "wh", "zh", "ng", "nk", "nck", "sch", "dt"
  };
  
	java.util.Map<Character,Character> vowelMap = new HashMap<Character,Character>();
	java.util.Map<Character,Character> modernVowelMap = new HashMap<Character,Character>();
  
  public DutchMultigramPruner()
  {
    for (int i=0; i < possibleVowels.length(); i++)
    {
      vowelMap.put(possibleVowels.charAt(i), possibleVowels.charAt(i));
    }
    for (int i=0; i < modernPossibleVowels.length(); i++)
    {
      modernVowelMap.put(modernPossibleVowels.charAt(i), modernPossibleVowels.charAt(i));
    }
  }

  public boolean isOK(JointMultigram m) // TODO: lhs all vowels <-> rhs all vowels
  {
  	// CHANGE: multigram ending with $ is ok iff it is ok without th e $
  	if (m.lhs.endsWith(Alphabet.finalBoundaryString) || m.rhs.endsWith(Alphabet.finalBoundaryString))
  	{
  		if (!( m.lhs.endsWith(Alphabet.finalBoundaryString) && m.rhs.endsWith(Alphabet.finalBoundaryString)))
   			return false;
 
  		String lhsx = m.lhs.substring(0,m.lhs.length()-1);
  		String rhsx = m.rhs.substring(0,m.rhs.length()-1);
  		JointMultigram mm = m.set.createMultigram(lhsx,rhsx);
  		return isOK(mm);
  	}
  	if (m.lhs.contains(Alphabet.initialBoundaryString) || m.rhs.contains(Alphabet.finalBoundaryString)) // this is no good
  	{
  		return m.lhs.equals(m.rhs) &&
  		 (m.rhs.equals(Alphabet.initialBoundaryString) || m.rhs.equals(Alphabet.finalBoundaryString)) ;
  	}
    if (m.isSingleton()) 
    	return true;
    if (isPossibleGrapheme(m.lhs, true) && isPossibleGrapheme(m.rhs, false)) return true;
    return false;
  }

  public boolean allVowels(String s, boolean modern)
  {
    int l = s.length();
    boolean allVowels = true;
    for (int i=0; i < l; i++)
    {
    	if (modern && s.charAt(i) == 'j' )
    	{
    		if (! ( (i == l -1)  &&  l > 1 && isModernVowel(s.charAt(i-1))))
    			allVowels=false;
    	} else
      if (!(modern?isModernVowel(s.charAt(i)):isVowel(s.charAt(i))))
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
    boolean allVowels = allVowels(s,modern); // misschien toch weer aanzetten?
    if (allVowels) return true;
    for (int i=0; i < extraGraphemes.length; i++)
    {
    	if (sLower.equals(extraGraphemes[i]))  return true;
    }
    return false;
  }
 

  public boolean isVowel(char s)
  {
    return (vowelMap.containsKey(s));
  }
  public boolean isModernVowel(char s)
  {
    return (modernVowelMap.containsKey(s));
  }

	//@Override
	public void applyAbstraction(MultigramSet set)
	{
		// TODO Auto-generated method stub
		
	}
}
