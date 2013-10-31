currentDate=`date`
git add `find src -name "*.java"`
git add `find resources`
git commit -a -m "last commit: $currentDate"
git push  github master

