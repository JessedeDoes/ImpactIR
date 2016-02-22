package nl.namescape.nelexicon;

import java.util.HashMap;
import java.util.Map;

import nl.namescape.nelexicon.database.ObjectRelationalMapping;


public class DatabaseMapping 
{

	ObjectRelationalMapping lemmaMapping = 
			new ObjectRelationalMapping(NELemma.class, "lemmata");
	ObjectRelationalMapping wordformMapping = 
			new ObjectRelationalMapping(NEWordform.class, "wordforms");
	ObjectRelationalMapping attestationMapping = 
			new ObjectRelationalMapping(NEAttestation.class, "token_attestations");
	ObjectRelationalMapping documentMapping = 
			new ObjectRelationalMapping(NEDocument.class, "documents");
	ObjectRelationalMapping awfMapping = 
			new ObjectRelationalMapping(NEAnalyzedWordform.class, "analyzed_wordforms");
	ObjectRelationalMapping containmentMapping = 
			new ObjectRelationalMapping(NEContainment.class, "analyzed_wordform_groups");
	

	Map<Object, Object> lemmaMap = new HashMap<Object, Object>();
	Map<Object, Object> wordformMap = new HashMap<Object, Object>();
	Map<Object, Object> awfMap = new HashMap<Object, Object>();
	Map<Object, Object> attestationMap = new HashMap<Object, Object>();
	Map<Object, Object> containmentMap = new HashMap<Object, Object>();
	
	public void init()
	{
		String[][] lemmaMappingData = 
		{
			{"primaryKey"}, // primary key field
			{"modern_lemma", "lemma"},
			{"lemma_part_of_speech", "lemmaPoS"},
			{"persistent_id", "lemmaID"},
			{"ne_label", "neLabel"},
			{"gloss", "gloss"}
		};
		
		lemmaMapping.addFields(lemmaMappingData);
		
		String[][] wordformMappingData = 
		{
				{"primaryKey"},
				{"wordform", "wordform"}
		};
		
		wordformMapping.addFields(wordformMappingData);
		
		String[][] awfMappingData = 
		{
				{"primaryKey"},
				{"wordform_id", "wordformKey"},
				{"lemma_id", "lemmaKey"},
		};
		awfMapping.addFields(awfMappingData);
		
		
		String[][] documentMappingData =
		{
				{"primaryKey"},
				{"title", "title"},
				{"author", "author"},
				{"persistent_id", "documentID"},
				{"pub_year", "publicationYear"},
				{"url", "url"},
				{"isbn", "isbn"}
		};
		
		documentMapping.addFields(documentMappingData);
	
		String[][] attestationMappingData = 
		{
				{"primaryKey"},
				{"document_id", "documentKey"},
				{"analyzed_wordform_id", "analyzedWordformKey"},
				{"quote", "quotation"},
				{"token_id", "tokenID"}
		};
		
		attestationMapping.addFields(attestationMappingData);
		
		String[][] containmentMappingData = 
		{
				{"primaryKey"},
				{"parent_id", "parentKey"},
				{"child_id", "childKey"},
				{"part_number", "partNumber"}
		};
		containmentMapping.addFields(containmentMappingData);
	}

	static Object canonical(Map<Object,Object> m, Object o)
	{
		if (!m.containsKey(o))
		{
			m.put(o, o);
			return o;
		} else
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println("Ah.. seen before: o");
			return m.get(o);
		}
	}

	static boolean equal(Object o1, Object o2)
	{
		if (o1==null) return o2==null;
		if (o2==null) return false;
		return o1.equals(o2);
	}
	
}
