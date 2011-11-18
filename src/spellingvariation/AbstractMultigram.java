package spellingvariation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import java.util.HashMap;

/**
 * 
 * @author taalbank
 *<p>
 *Purpose:  mitigate data sparsity problems by implementing 
 *somewhat more abstract substitutions like gemination, devoicing, epenthetic vowel insertion , etc
 *this involves "tying" a group of parameters together
 *PM the multigrampruners should allow the 
 */
public class AbstractMultigram extends JointMultigram
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String lhsPattern;
	String rhsPattern;
	List<JointMultigram> instances = new ArrayList<JointMultigram>();
	Matcher matcher;
	HashMap<String,JointMultigram> 
	lhsToMultigramMap = new HashMap<String,JointMultigram>();

	// static int maxId=1;

	private void init(MultigramSet set)
	{
		this.set = set;
		//id = maxId++;
		for (JointMultigram m: set)
		{
			if (this.generalizes(m))
			{
				lhsToMultigramMap.put(m.lhs,m);
				instances.add(m);
				m.groupId = this.id;
			}
		}
	}

	public AbstractMultigram(MultigramSet set)
	{
		super(set);
		init(set);
		System.err.println("created abstract multigram: " + this + " with id " + this.id + " class:  " +  
				this.getClass() + " set " + set + " nitems in set " + set.size());
	}

	public boolean isAbstract()
	{
		return true;
	}

	public String apply(String s)
	{
		if (matcher.find())
		{
			String found = matcher.group();
			JointMultigram m = lhsToMultigramMap.get(found);
			if (m  != null)
			{
				String z = matcher.replaceFirst(m.rhs);
				return z;
			}
		}
		return null;
	}

	public String applyConverse(String s)
	{
		return null;
	}

	public boolean generalizes(JointMultigram m)
	{
		return false; 
	}

	static class Doubling extends AbstractMultigram
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Doubling(MultigramSet set)
		{
			super(set);
			lhs="abstract:X";
			rhs="XX";
			// TODO Auto-generated constructor stub
		}

		public String apply(String s)
		{
			return s+s;
		}

		public String applyConverse(String s)
		{
			return s.substring(0,1); 
		}

		public boolean generalizes(JointMultigram m)
		{
			if (m.lhs.length() != 1)
				return false;
			String z = m.lhs + m.lhs;
			return (z.equals(m.rhs));
		}
	}

	static class DeDoubling  extends AbstractMultigram
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DeDoubling(MultigramSet set)
		{

			super(set);
			lhs="abstract:XX";
			rhs="X";
			// TODO Auto-generated constructor stub
		}

		public String apply(String s)
		{
			return s.substring(0,1); 
		}

		public String applyConverse(String s)
		{
			return s+s;
		}

		public boolean generalizes(JointMultigram m)
		{
			if (m.rhs.length() != 1)
				return false;
			String z = m.rhs + m.rhs;
			return (z.equals(m.lhs));
		}
	}

	static class Voicing extends AbstractMultigram
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Voicing(MultigramSet set)
		{
			super(set);
			lhs="abstract:voiceless";
			rhs="voiced";
			// TODO Auto-generated constructor stub
		}

		public String apply(String s)
		{
			if (s.equals("s"))
				return "z";
			if (s.equals("f"))
				return "v";
			if (s.equals("p"))
				return "b";
			if (s.equals("t"))
				return "d";
			return null;
		}

		public String applyConverse(String s)
		{
			return null; 
		}

		public boolean generalizes(JointMultigram m)
		{
			if (m.rhs.length() != 1 || m.lhs.length() != 1)
				return false;
			String z = apply(m.lhs);
			return m.rhs.equals(z);
		}
	}

	static class DeVoicing extends AbstractMultigram
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DeVoicing(MultigramSet set)
		{
			super(set);
			lhs="abstract:voiced";
			rhs="voiceless";
			// TODO Auto-generated constructor stub
		}

		public String apply(String s)
		{
			if (s.equals("z"))
				return "s";
			if (s.equals("v"))
				return "f";
			if (s.equals("b"))
				return "p";
			if (s.equals("d"))
				return "t";
			return null;
		}

		public String applyConverse(String s)
		{
			return null;
		}

		public boolean generalizes(JointMultigram m)
		{
			if (m.rhs.length() != 1 || m.lhs.length() != 1)
				return false;
			String z = apply(m.lhs);
			return m.rhs.equals(z);
		}
	}
}
