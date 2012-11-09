create temporary table lemma_frequency (lemma_id int(32) primary key, lemma_frequency int);
insert into lemma_frequency
  select l.lemma_id, sum(1) as lemma_frequency from
  lemmata l, simple_analyzed_wordforms a, token_attestations t
  where
   l.lemma_id=a.lemma_id and a.analyzed_wordform_id = t.analyzed_wordform_id
  group by
    l.lemma_id;

create temporary table wordforms_with_frequency
select
  persistent_id,
  modern_lemma,
  wordform,
  lemma_part_of_speech,
  part_of_speech,
  sum(1) as wordform_frequency,
  lemma_frequency,
  max(a.analyzed_wordform_id) as analyzed_wordform_id
from
  token_attestations,
  simple_analyzed_wordforms a,
  lemmata,
  wordforms,
  lemma_frequency
where
  token_attestations.analyzed_wordform_id = a.analyzed_wordform_id and
  lemmata.lemma_id = a.lemma_id and
  wordforms.wordform_id = a.wordform_id and
  lemma_frequency.lemma_id = lemmata.lemma_id 
  group by lemmata.lemma_id, wordforms.wordform_id;

alter table wordforms_with_frequency add index(analyzed_wordform_id);
alter table derivations add index(analyzed_wordform_id);
