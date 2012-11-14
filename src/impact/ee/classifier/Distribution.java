package impact.ee.classifier;
import java.util.*;

/**
 * Straightforward discrete probability distribution on a set of strings.
 */

public class Distribution implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Outcome> outcomes = new ArrayList<Outcome>();
	private java.util.HashMap<String,Outcome> outcomeMap = new HashMap<String, Outcome>();
	double N=0;
	boolean existential = false;

	public void setExistential(boolean b)
	{
		existential = b;
	}
	
	public class Outcome implements java.io.Serializable
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String label;
		public Double p;
		int count;
		public Outcome(String s, double p)
		{
			this.label=s;
			this.p=p;
			this.count = 0;
		}
		public String toString()
		{
			return "<" + label + ": " + p + ">";
		}
	}

	public Distribution()
	{

	}

	public String toString()
	{
		String z= "[";
		for (int i=0; i < outcomes.size(); i++)
		{
			Outcome it = outcomes.get(i);
			//if (it.p == 0) continue;
			z += it;
			if (i < outcomes.size()-1)
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
		//if (theta == 0)
		//	this.outcomes = d.outcomes;
		for (Outcome i: outcomes)
		{
			i.p =( theta * i.p + d.getProbability(i.label)) / (1 + theta); 
		}
	}

	double getProbability(String s)
	{
		Outcome i = outcomeMap.get(s);
		if (i == null) return 0;
		return i.p;
	}

	public void incrementCount(String s)
	{
		N++;
		Outcome i = outcomeMap.get(s);
		if (i == null)
		{
			i = new Outcome(s,0);
			outcomeMap.put(s,i);
			outcomes.add(i);
			i.count = 1;
		} else
			i.count++;
	}

	public void computeProbabilities()
	{
		for (Outcome i: outcomes)
		{
			i.p = this.existential?(i.count>0?1:0):i.count / N;
		}
	}

	public void pruneRareValues() // hm: keep only up to 90% cumulative?
	{
		computeProbabilities();
		
	}
	
	public Distribution(int N)
	{
	}

	public int size()
	{
		return outcomes.size();
	}

	public Outcome get(int i)
	{
		return outcomes.get(i);
	}

	public void addOutcome(String s, double p)
	{
		outcomes.add(new Outcome(s,p));
	}

	class OutcomeComparator implements java.util.Comparator<Outcome>
	{
		public int compare(Outcome r1, Outcome r2) { return r2.p.compareTo(r1.p); }
		public boolean equals(Outcome r1, Outcome r2) { return r2.p.equals(r1.p); }
		public OutcomeComparator() { } ;
	}

	public void sort()
	{
		java.util.Collections.sort(outcomes, Distribution.comparator);
	}

	public Comparator<Outcome> getComparator()
	{
		return new OutcomeComparator();
	}

	public static Comparator<Outcome> comparator = new Distribution().getComparator();

	public Distribution(Set<String> allClasses)
	{
		// TODO Auto-generated method stub
		for (String s: allClasses)
		{
			Outcome i = new Outcome(s,0.0);
			this.outcomes.add(i);
			this.outcomeMap.put(s,i);
		}
	}

	public void resetToZero()
	{
		for (Outcome i: outcomes)
		{
			i.count=0;
			i.p=0.0;
		}
	}
	
	@Override
	public boolean equals(Object other)
	{
		try
		{	
			Distribution d  = (Distribution) other;
			return (this.toString().equals(d.toString())); // ugly..
		} catch (Exception e)
		{
			return false;
		}
	}
}
