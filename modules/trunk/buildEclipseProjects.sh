#!/bin/bash

for module in */pom.xml
do 
   mvn -P eclipse eclipse:eclipse -f $module 
   if [ $? != 0 ] 
   then
       exit $?
   fi
done

echo "Modules built succesfully"
