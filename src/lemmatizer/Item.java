package lemmatizer;
import java.util.*;

public class Item
{
  ArrayList<String> values = new ArrayList<String>();
  String classLabel;
  public Item()
  {
    classLabel=null; 
  }

  public void add(String s)
  {
    values.add(s);
  }

  public String toString()
  {
    String z= "{";
    for (int i=0; i < values.size(); i++)
    {
      z += values.get(i);
      if (i < values.size()-1)
      {
        z+= ", ";
      }
    }
    z+= "}";
    if (classLabel != null)
    {
      z += " -> " + classLabel;
    }
    return z;
  }
}
