#!/bin/bash

rm -r ~/.m2/repository/org/codehaus/fabric3

for module in */pom.xml 
do 
   mvn $* -f $module 
   if [ $? != 0 ] 
   then
       exit $?
   fi
done

echo "Modules built succesfully"
