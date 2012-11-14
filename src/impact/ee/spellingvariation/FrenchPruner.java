package impact.ee.spellingvariation;


public class FrenchPruner extends LHSConstrainedMultigramPruner
{
  public FrenchPruner()
  {
    super("patternsets/frenchG2P.graphemes");
  }
}
