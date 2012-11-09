package impact.ee.spellingvariation;

public class DutchMultigramPrunerWithAbstraction extends DutchMultigramPruner
{
	@SuppressWarnings("unused")
	public void applyAbstraction(MultigramSet set)
	{
		AbstractMultigram dd = new AbstractMultigram.DeDoubling(set);
		AbstractMultigram d =  new AbstractMultigram.Doubling(set);
		AbstractMultigram v =  new AbstractMultigram.Voicing(set);
		AbstractMultigram dv =  new AbstractMultigram.DeVoicing(set);
	}
	@Override
	public boolean isOK(JointMultigram m)
	{
		if (m.isAbstract()) 
			return true;
		if (m.groupId >= 0)
			return true;
		if (m.rhs.length() == 1 && m.lhs.length() == 1)
			return true;
		if (m.rhs.length() == 0 ||  m.lhs.length() == 0)
			return true;

		if (m.lhs.endsWith(Alphabet.finalBoundaryString) ||
				m.rhs.endsWith(Alphabet.finalBoundaryString))
		{
			if (!( m.lhs.endsWith(Alphabet.finalBoundaryString) &&
					m.rhs.endsWith(Alphabet.finalBoundaryString)))
				return false;

			String lhsx = m.lhs.substring(0,m.lhs.length()-1);
			String rhsx = m.rhs.substring(0,m.rhs.length()-1);
			JointMultigram mm = m.set.createMultigram(lhsx,rhsx);
			return isOK(mm);
		}

		if (m.lhs.startsWith(Alphabet.initialBoundaryString) ||
				m.rhs.startsWith(Alphabet.initialBoundaryString))
		{
			if (!( m.lhs.startsWith(Alphabet.initialBoundaryString) &&
					m.rhs.startsWith(Alphabet.initialBoundaryString)))
				return false;

			String lhsx = m.lhs.substring(1);
			String rhsx = m.rhs.substring(1);
			JointMultigram mm = m.set.createMultigram(lhsx,rhsx);
			return isOK(mm);
		}

		if (m.rhs.startsWith(m.lhs.substring(0,1)))
			return false;
		if (m.rhs.endsWith(m.lhs.substring(m.lhs.length()-1, m.lhs.length())))
			return false;
		return true;
	}
}
