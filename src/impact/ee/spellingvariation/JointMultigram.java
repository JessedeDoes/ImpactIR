package impact.ee.spellingvariation;

/**
	 * PM: do not make this class static.
	 * It is better for the multigrams to  intrinsically belong to a certain set.
	 * @author jesse
	 *
	 */
	public class JointMultigram implements java.io.Serializable
	{
		private static final long serialVersionUID = 1417752573542298784L;
		String lhs;
		String rhs;
		int id;
		MultigramSet set; // do we need this? 
		boolean active = true; // used for pruning before segmentation step
		boolean isAbstract = false;
		int groupId = -1;

		
		public boolean isAbstract() 	{ return false; }
		
		public JointMultigram(MultigramSet set)
		{
			this.set = set;
			set.multigramVector.add(this);
			this.id = set.nMultigrams;
			set.nMultigrams++;
			nl.openconvert.log.ConverterLog.defaultLog.println("Boe?? " + this.getClass() + " size now: " + set.nMultigrams);
		}

		public JointMultigram() {		}

		public int hashCode() { return lhs.hashCode() + rhs.hashCode(); }

		public boolean equals(Object other)
		{
			if (other.getClass().equals(JointMultigram.class))
			{
				JointMultigram o = (JointMultigram) other;
				return (lhs.equals(o.lhs)&& rhs.equals(o.rhs));
			}
			return false;
		}
/**
 * Translate a sequence of joint unigrams to a joint multigram
 * @param multigram
 * @param endPosition
 */
		public void initData(int[] multigram, int endPosition)
		{
			lhs = "";
			rhs = "";
			for (int i=0; i < endPosition; i++)
			{
				/* Dit kan je hier nog niet doen - is nog niet gedefinieerd
				if (set.codeToStringPairMapping != null)
				{
					lhs += set.codeToStringPairMapping.getLHS(multigram[i]);
					rhs += set.codeToStringPairMapping.getRHS(multigram[i]);
				} else
				*/
				{
					int l = set.symbolPairs[multigram[i]].lhs;
					int r = set.symbolPairs[multigram[i]].rhs;
					if (l != Alphabet.空)
					{
						lhs += set.inputAlphabet.decode(l);
					}
					if (r != Alphabet.空)
					{
						rhs += set.outputAlphabet.decode(r);
					}
				}
			}
			//nl.openconvert.log.ConverterLog.defaultLog.println(lhs + "->" + rhs);
		}

		public boolean isSingleton()
		{
			return lhs.length() < 2 && rhs.length() < 2;
		}
		
		public boolean isSubstitution() // strange defnition??
		{
			return lhs.length() > 0 && rhs.length() > 0;
		}
	
		public boolean isInsertion()
		{
			return lhs.length()==0;
		}	

		public boolean isDeletion()
		{
			return rhs.length()==0;
		}

		public boolean isSymmetric()
		{
			return rhs.equals(lhs);
		}

		@Override
		public String toString()
		{
			if (lhs != null && lhs.equals(rhs))
				return lhs;
			return lhs + "→" + rhs;
		}
	}
