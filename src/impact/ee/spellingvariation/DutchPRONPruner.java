package impact.ee.spellingvariation;


/**
 * A very simple attempt to arrive at a "reasonable" set of OCR patterns.
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
public class DutchPRONPruner implements MultigramPruner
{

  public boolean lengthOneOrTwo(String s, String t) // hier zijn veel uitzonderingen
  {
    return (s != null && t != null && 
    		((s.length()==1 && t.length()==1) || 
    				(s.length() ==1 && t.length() == 2 || 
    				t.length() ==1 && s.length() ==2)));
  }

  public boolean isOK(JointMultigram m) // TODO: lhs all vowels <-> rhs all vowels
  {
     boolean x =  m.isSingleton() ||
    		 lengthOneOrTwo(m.lhs,m.rhs) 
    		 || m.rhs.equals("eau") ||  m.rhs.equals("ieu")  || 
    		 m.rhs.equals("eeu")   || m.rhs.equals("ill") ;
     
     System.err.println(x + ": " + m);
     
     return x;
  }

	//@Override
	public void applyAbstraction(MultigramSet set)
	{
		// TODO Auto-generated method stub
		
	}
}
