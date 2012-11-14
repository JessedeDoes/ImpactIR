
create temporary table myWords as
select
 wordform, modern_lemma, attestation_id, token_attestations.document_id, start_pos
from
  corpusId_x_documentId,
  token_attestations,
  analyzed_wordforms,
  lemmata,
  wordforms
where
  token_attestations.document_id = corpusId_x_documentId.document_id and
  corpusId_x_documentId.corpus_id < 1000000 and
  token_attestations.analyzed_wordform_id = analyzed_wordforms.analyzed_wordform_id and
  lemmata.lemma_id = analyzed_wordforms.lemma_id and
  wordforms.wordform_id = analyzed_wordforms.wordform_id 
  order by token_attestations.document_id, start_pos;
  
alter table myWords add index(document_id, start_pos);

select myWords.*, wordform_groups.wordform_group_id from myWords left join wordform_groups
on myWords.document_id = wordform_groups.document_id and myWords.start_pos = wordform_groups.onset;