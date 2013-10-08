source setClassPath.sh
LEXICON=resources/exampledata/molexDump.txt
java -Djava.library.path=./lib -Xmx4g 'impact.ee.tagger.BasicTagger$Trainer' -l $LEXICON $1 $2
