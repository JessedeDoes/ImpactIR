source setClassPath.sh
java -Djava.library.path=./lib -Xmx4g 'impact.ee.tagger.BasicTagger$Tester' $1 $2
