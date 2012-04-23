#!/bin/bash
# Note: tmpwatch is configured to clean out output folder after some time
if [ "$(whoami)" != "root" ]; then
        echo "Sorry, you are not root."
        exit 1
fi
if [ $# -lt 1 ]
then
  echo "Usage: `basename $0` <folder to watch relative to /var/www/html/generator> [nodeploy]"
  exit 1
fi
JAVA_HOME=/var/jdk
TOPLEVEL=/var/www/html/generator
WATCHDIR=$TOPLEVEL/$1
mkdir $WATCHDIR
chown apache.ci $WATCHDIR
while [ 1 ]; do
WATCH=`inotifywait -e close_write  --format '%w,%f' $WATCHDIR`
lockfile $WATCHDIR/WORKING
DIR=`echo $WATCH | cut -f 1 -d ','`
FILE=`echo $WATCH | cut -f 2 -d ','`
rm $DIR/output -rf
rm -f $DIR/status.html $DIR/WAITING-FOR-NEW-FILE $DIR/ARTIFACT-EXISTS-NOT-DEPLOYING $DIR/SNAPSHOT-ARTIFACT-DEPLOYED-* $DIR/ARTIFACT-DEPLOYED-* $DIR/MAVEN-ERROR-CHECK-STATUS
cd /var/cruisecontrol/projects/HbnPojoGen
TEMP=`mktemp -d`
echo "<html><pre>`date`" > $DIR/status.html
java -jar ./hbnPojoGen.jar $DIR/$FILE $TEMP >> $DIR/status.html 2>&1 
if [ $# -eq 1 ]; then 
## Now check if the generated pom will overwrite an existing artifact and if so bail out.
GROUPID=`xmllint --format $DIR/$FILE  | grep "<maven" | sed 's/ /\n/ig'  | grep groupId  | cut -f 2 -d '"' | sed 's/\./\//ig'`
ARTIFACTID=`xmllint --format $DIR/$FILE | grep "<maven" | sed 's/ /\n/ig'  | grep artifactId  | cut -f 2 -d '"'`
VERSION=`xmllint --format $DIR/$FILE   | grep "<maven" | sed 's/ /\n/ig'  | grep version  | cut -f 2 -d '"'`
VERSNAPSHOT=`echo $VERSION | grep -e "-SNAPSHOT$"`
if [ "$?" -ne "0" ]; then
# Let's check to see if it already exists
echo "Checking if artifact $GROUPID/$ARTIFACTID/$VERSION already exists" >> $DIR/status.html
if [ -d /var/archiva/data/repositories/repo/$GROUPID/$ARTIFACTID/$VERSION ]; then
	echo "Artifact already exists - not deploying!" >> $DIR/status.html
	touch $DIR/ARTIFACT-EXISTS-NOT-DEPLOYING
else
	touch $DIR/MAVEN-IS-BUILDING
	mvn -f $TEMP/pom.xml deploy >> $DIR/status.html 2>&1 
	if [ "$?" -ne "1" ]; then
		touch $DIR/ARTIFACT-DEPLOYED-$ARTIFACTID-$VERSION 
		rm $DIR/MAVEN-IS-BUILDING
	else 
		touch $DIR/MAVEN-ERROR-CHECK-STATUS
	fi
fi
else
	touch $DIR/MAVEN-IS-BUILDING
        mvn -f $TEMP/pom.xml deploy >> $DIR/status.html 2>&1 
        if [ "$?" -ne "1" ]; then
                touch $DIR/SNAPSHOT-ARTIFACT-DEPLOYED-$ARTIFACTID-$VERSION
                rm $DIR/MAVEN-IS-BUILDING
        else
                touch $DIR/MAVEN-ERROR-CHECK-STATUS
        fi

fi


fi
find $TEMP -name '*.java' | xargs unix2dos
mv $TEMP $DIR/output
chmod ogu-w $DIR/status.html
echo "</pre></html>" >> $DIR/status.html
# Make it windows friendly
unix2dos $DIR/status.html
rm $DIR/$FILE -f
chmod o+rwx -R $DIR/output
rm -f $DIR/WORKING
touch $DIR/WAITING-FOR-NEW-FILE
done
