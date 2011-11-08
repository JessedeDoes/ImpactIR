package lemmatizer;
import java.util.*;

/**
* straightforward discrete probability distribution on a set of strings
*/

public class Distribution
{
  ArrayList<Item> items = new ArrayList<Item>();
  java.util.HashMap<String,Item> itemMap = new HashMap<String, Item>();
  double N=0;
  
  class Item
  {
    String s;
    Double p;
    int count;
    public Item(String s, double p)
    {
      this.s=s;
      this.p=p;
      this.count = 0;
    }
    public String toString()
    {
    	return s + ": " + p;
    }
  }

  public Distribution()
  {
  	
  }
  
  public String toString()
  {
    String z= "[";
    for (int i=0; i < items.size(); i++)
    {
    	Item it = items.get(i);
    	if (it.p == 0)
    		continue;
      z += it;
      if (i < items.size()-1)
      {
        z+= ", ";
      }
    }
    z+= "]";
    return z;
  };

  /**
   * The (higher order) distribution <i>d</i> is smoothed with the lower order model <i>this</i>.<br>
   * The result is merged into <i>this</i> according to<br/><br/>
   *  &nbsp;&nbsp; p<sub>new</sub>(x) = (&#x3d1;  * p<sub>this</sub> (x) + p<sub>d</sub>(x))  / ( 1 + &#x3d1; )
   *  <br/><br/>
   * Assumes that the outcome space of 'this' is larger than that of d<br/>
   *
   * @param d
   * @param theta
   */
  
  public void mergeHigherOrderDistribution(Distribution d, double theta)
  {
  	for (Item i: items)
  	{
  		i.p =( theta * i.p + d.getProbability(i.s)) / (1 + theta); 
  	}
  }
  
  double getProbability(String s)
  {
  	Item i = itemMap.get(s);
  	if (i == null) return 0;
  	return i.p;
  }
  
  public void incrementCount(String s)
  {
  	N++;
  	Item i = itemMap.get(s);
  	if (i == null)
  	{
  		i = new Item(s,0);
  		itemMap.put(s,i);
  		items.add(i);
  		i.count = 1;
  	} else
  		i.count++;
  }
  
  public void computeProbabilities()
  {
  	for (Item i: items)
  	{
  		i.p = i.count / N;
  	}
  }
  
  public Distribution(int N)
  {
  }

  int size()
  {
    return items.size();
  }

  public Item get(int i)
  {
    return items.get(i);
  }

  public void addItem(String s, double p)
  {
    items.add(new Item(s,p));
  }

  class ItemComparator implements java.util.Comparator<Item>
  {
    public int compare(Item r1, Item r2) { return r2.p.compareTo(r1.p); }
    public boolean equals(Item r1, Item r2) { return r2.p.equals(r1.p); }
    public ItemComparator() { } ;
  }

  public void sort()
  {
    java.util.Collections.sort(items,Distribution.comparator);
  }

  public Comparator<Item> getComparator()
  {
    return new ItemComparator();
  }

  public static Comparator<Item> comparator = new Distribution().getComparator();

	public Distribution(Set<String> allClasses)
	{
		// TODO Auto-generated method stub
		for (String s: allClasses)
		{
			Item i = new Item(s,0.0);
			this.items.add(i);
			this.itemMap.put(s,i);
		}
	}
	
	public void resetToZero()
	{
		for (Item i: items)
		{
			i.count=0;
			i.p=0.0;
		}
	}
}
