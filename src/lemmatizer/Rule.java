package lemmatizer;
import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

class Rule
{
  Pattern pattern;
  String lemmaPoS;
  String PoS;
  int count=0;
  int id;
  ArrayList<Example> examples = new ArrayList<Example>();

  public Rule()
  {
    lemmaPoS = "";
    PoS = "";
  }

  public Rule(Pattern pat, String pos, String lpos)
  {
    pattern=pat; PoS = pos; lemmaPoS = lpos;
  }

  public boolean equals(Object o1)
  {
    if (o1 instanceof Rule)
    {
      Rule o = (Rule) o1;
      return (pattern.equals(o.pattern) && lemmaPoS.equals(o.lemmaPoS) && PoS.equals(o.PoS));
    } else
    {
      return false;
    }
  }

  public String toString()
  {
    return pattern + " " + lemmaPoS + " " + PoS;
  }

  public int hashCode()
  {
    return pattern.hashCode()+ PoS.hashCode() + lemmaPoS.hashCode();
  }
}
