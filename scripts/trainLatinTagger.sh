source setClassPath.sh
PerseusLexicon=resources/exampledata/perseus/perseus.lex.tab
Training=resources/exampledata/proiel/proiel.train
Model=Models/latin.proiel.model
java -Djava.library.path=./lib -Xmx4g 'impact.ee.tagger.BasicTagger$Trainer' -l$PerseusLexicon $Training $Model
