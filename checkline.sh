#!/bin/bash

line=0

maxLine=-1
maxLineFile=""

#for i in `find . -type f | grep -E --color '^(./).*\.(cc|java|cpp|c|h|hpp|gradle|mk|S)$'`;do
for i in `find . -type f | grep -E --color '^(./).*\.(java)$'`;do
	result=`wc -l $i | awk '{print $1}'`
	echo "Doing lines: $result, $i ."
	if [ $result -gt $maxLine ];then
		maxLine=$result
		maxLineFile=$i
	fi
	((line+=result))
done

#for i in `find . -name "*.h"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done
#for i in `find . -name "*.cc"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done
#for i in `find . -name "*.cpp"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done
#for i in `find . -name "*.c"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done

echo "***********************Result*************************"
echo ""
echo "Max   line: $maxLine, file: $maxLineFile"
echo "Total line: $line"
echo ""
echo "******************************************************"
