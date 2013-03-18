package nl.namescape.nelexicon;


public class NEAttestation // use group attestations (?)
{
	public Integer primaryKey;
	public Integer analyzedWordformKey;
	public Integer documentKey;
	public String quotation;
	public String tokenID;
	public NEDocument document;
	public NEAnalyzedWordform awf;	
}