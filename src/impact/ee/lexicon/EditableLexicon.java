package impact.ee.lexicon;

public interface EditableLexicon extends ILexicon 
{
	public int addWordform(String wordform); // should return id?
	public int addLemma(String lemma, String PoS, String neLabel, String gloss);
	public int addAnalyzedWordform(int lemmaId, int wordformId, String PoS);
	public int addAttestation(int analyzedWordformId, int documentId, String tokenId);
	public int addDocument();
	public void setField(String tableName, int id, String fieldName, String fieldValue);
}
