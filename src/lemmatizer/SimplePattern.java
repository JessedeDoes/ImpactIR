package lemmatizer;
import java.io.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
* A simple pattern consists of a suffix substitution and a prefix substitution
*/

public class SimplePattern implements Pattern
{
  String leftPrefix, rightPrefix, leftSuffix, rightSuffix;
  SimplePattern converse=null;
  boolean finalOnly = true;
  
  public SimplePattern converse()
  {
    if (converse != null)
    {
       return converse;
    }
    SimplePattern p =  new SimplePattern(rightPrefix,rightSuffix,leftPrefix,leftSuffix);
    p.converse = this;
    this.converse = p;
    return p;
  }

  public SimplePattern()
  {
    leftPrefix=rightPrefix=leftSuffix=rightSuffix="";
  }

  public SimplePattern(String s1, String s2, String s3, String s4)
  {
    leftPrefix=s1; leftSuffix=s2; rightPrefix=s3; rightSuffix=s4;
    finalOnly = (leftPrefix =="" && rightPrefix=="");
  }
 
  public String toString()
  {
  	String pre="";
  	if (leftPrefix.equals("") && rightPrefix.equals(""))
  		pre="";
  	else pre = String.format("[%s/%s]",leftPrefix,rightPrefix);
  	String post="";
  	if (leftSuffix.equals("") && rightSuffix.equals(""))
  		post="";
  	else post = String.format("[%s/%s]",leftSuffix,rightSuffix);
    return String.format("%s-%s", pre,post);
  }

  public boolean equals(Object o1)
  {
    if (o1 instanceof SimplePattern)
    {
      SimplePattern o = (SimplePattern) o1;
      try
      {
        return (leftPrefix.equals(o.leftPrefix) && rightPrefix.equals(o.rightPrefix) && 
                leftSuffix.equals(o.leftSuffix) && rightSuffix.equals(o.rightSuffix));
       } catch (Exception e)
       {
         System.err.println(this);
         System.err.println(o);
         System.exit(1);
       }
       return false;
    } else
    {
      return false;
    }
  }

  public int hashCode()
  {
    // return 1;
    try
    {
      return leftPrefix.hashCode()+ rightPrefix.hashCode() + leftSuffix.hashCode() + rightSuffix.hashCode();
    } catch (Exception e)
    {
       System.err.println(this);
       System.exit(1);
    }
    return 0;
  }

  public String apply(String s)
  {
    String image = null;
    if (leftPrefix.length() >= 0)
    {
      if (s.startsWith(leftPrefix))
      {
        image = this.rightPrefix + s.substring(leftPrefix.length());
      } else
      {
        return null;
      }
    } else
    {
      image = s;
    }
    if (leftSuffix.length() >= 0)
    {
      if (s.endsWith(leftSuffix))
      {
        image = image.substring(0,image.length()-leftSuffix.length()) + rightSuffix;
      } else
      {
        return null;
      }
    }
    return image;
  }
 
  public String applyConverse(String s)
  {
    return this.converse().apply(s);
  }
}
