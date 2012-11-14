package impact.ee.spellingvariation;

/**
 * Allows for language or situation-dependent pruning of the possible multigrams
 * (for instance initialize with a set of patterns already known,
 * in order to reestimate parameters on a new dataset)
 *
 * Possible approach
 * @author jesse
 *
 */
interface MultigramPruner
{
   public boolean isOK(JointMultigram m);
   public void applyAbstraction(MultigramSet set);
}

