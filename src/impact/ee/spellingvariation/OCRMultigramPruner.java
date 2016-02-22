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
public class OCRMultigramPruner implements MultigramPruner
{

  public boolean lengthOneOrTwo(String s, String t)
  {
    return (s != null && t != null && ((s.length()==1 && t.length()==1) || (s.length() ==1 && t.length() == 2 || t.length() ==1 && s.length() ==2)));
  }


  public boolean isContextPattern(String s, String t)
  {
    if (s.length()==1 && t.length()==2)
     return (t.startsWith(s) || t.endsWith(s));
   
     if (t.length()==1 && s.length()==2)
     return (s.startsWith(t) || s.endsWith(t));

    return false;
  }

  public boolean isOK(JointMultigram m)
  {
    boolean x =  m.isSingleton() || lengthOneOrTwo(m.lhs,m.rhs);
    boolean y = isContextPattern(m.lhs,m.rhs);
    nl.openconvert.log.ConverterLog.defaultLog.println("x=" + x + ", y=" + y + ", m= " + m);
    return x && !y;
  }

	//@Override
  public void applyAbstraction(MultigramSet set)
  {
// TODO Auto-generated method stub
  }
}
