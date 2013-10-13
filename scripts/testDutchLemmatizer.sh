source setClassPath.sh
java -Xmx3g impact.ee.lemmatizer.dutch.MultiplePatternBasedLemmatizer ~/Data/Lexicon/Extra/molexDump.txt  ~/Data/Lexicon/Extra/Tagset/sonar.mappedWithLemma.utf8.txt > /tmp/test.out
