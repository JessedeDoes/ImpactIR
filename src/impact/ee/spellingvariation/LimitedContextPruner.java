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

/*
 maybe: DO allow patterns for (single?) insertion and deletion with some context
*/

public class LimitedContextPruner implements MultigramPruner
{
  public boolean lengthOneOrTwo(String s, String t)
  {
    return (s != null && t != null && ((s.length()==1 && t.length()==1) || (s.length() ==1 && t.length() == 2 || t.length() ==1 && s.length() ==2)));
  }


  public boolean isSimpleInsertionOrDeletionWithContext(String s, String t)
  { 
     // single insertion or deletion with one context letter

/*
     if (lengthOneOrTwo(s,t) && s.length() > 0 && t.length() > 0)
     {
       if (t.length() == 1 && s.length()  == t.length()+1 && (s.startsWith(t) || s.endsWith(t)))
          return true
       if (s.length() == 1 && t.length()  == s.length()+1 && (t.startsWith(s) || t.endsWith(s)))
          return true;
     }
*/
     // insertions and deletions are always allowed context

     if ((!s.equals(t)) && s.length() > 0 && t.length() > 0)
     {
        String leftContext = commonPrefix(s,t);
        String rightContext = commonSuffix(s,t);
        int ls = s.length() - leftContext.length() - rightContext.length();
        int lt = t.length() - leftContext.length() - rightContext.length();
        //nl.openconvert.log.ConverterLog.defaultLog.println("left: " + leftContext + " right: " + rightContext + " in (" + s + ", " + t + ")");
        if ((ls==0 || lt==0) && leftContext.length() <= 1 && rightContext.length() <=1)
          return true;
     }

     return false; 
  }

    public static String commonPrefix(String s, String t) 
    {
        int n = Math.min(s.length(), t.length());
        for (int i = 0; i < n; i++)
        {
            if (s.charAt(i) != t.charAt(i))
                return s.substring(0, i);
        }
        return s.substring(0, n);
    }

    public static String commonSuffix(String s, String t)
    {
      StringBuffer sr = new StringBuffer(s).reverse();
      StringBuffer tr = new StringBuffer(t).reverse();
      return new String(new StringBuffer(commonPrefix(new String(sr), new String(tr))).reverse());
    }

  public boolean isContextPattern(String s, String t)
  {
    if (s.length() > 0 && t.length() > 0)
    {
       if (s.charAt(0) == t.charAt(0)) return true;
       if (s.charAt(s.length()-1) == t.charAt(t.length()-1)) return true;
    }
    return false;
  }

  public boolean isOK(JointMultigram m)
  {
    boolean x =  m.isSingleton(); // || lengthOneOrTwo(m.lhs,m.rhs);
    boolean y = isContextPattern(m.lhs,m.rhs);
    boolean z = isSimpleInsertionOrDeletionWithContext(m.lhs,m.rhs);
    //nl.openconvert.log.ConverterLog.defaultLog.println("x=" + x + ", y=" + y + ", m= " + m);
    return x || !y || z;
  }

	//@Override
  public void applyAbstraction(MultigramSet set)
  {
// TODO Auto-generated method stub
  }
}
