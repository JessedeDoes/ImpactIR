create temporary table part_of_group 
select distinct 
	analyzed_wordforms.analyzed_wordform_id, documents.persistent_id
from
	wordform_groups,
	token_attestations,
	analyzed_wordforms,
	documents
where
	wordform_groups.document_id = token_attestations.document_id and
	wordform_groups.onset = token_attestations.start_pos and
	analyzed_wordforms.analyzed_wordform_id = token_attestations.analyzed_wordform_id and
	documents.document_id=token_attestations.document_id;
alter table part_of_group add index(analyzed_wordform_id);
drop table if exists simple_analyzed_wordforms;
create table simple_analyzed_wordforms 
select * from analyzed_wordforms 
where not (analyzed_wordform_id in (select analyzed_wordform_id from part_of_group));
alter table simple_analyzed_wordforms add index(analyzed_wordform_id);
alter table simple_analyzed_wordforms add index(lemma_id);
alter table simple_analyzed_wordforms add index(wordform_id);