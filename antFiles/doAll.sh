
for f in build.bulgarian.xml\
	build.czech.1800_1809.xml\
	build.czech.1810_1842.xml\
	build.czech.1843_1849.xml\
 	build.czech.1850.xml\
	build.dutch.xml\
	build.english.xml\
	build.french.xml\
	build.german.16.xml\
	build.german.17.xml\
	build.german.18.xml\
	build.german.19.xml\
	build.german.xml\
	build.polish.xml\
	build.slovene.xml\
	build.spanish.xml ;
do
  echo $f;
  ant -f $f ir.evaluation;
  ant -f $f ir.evaluation.without.historical.lexicon
done
