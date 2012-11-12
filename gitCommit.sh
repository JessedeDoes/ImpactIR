currentDate=`date`
git add `find src -name "*.java"`
git commit -a -m "last commit: $currentDate"
git push  -u github master

