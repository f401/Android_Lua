#!/bin/bash

line=0

for i in `find . -type f | grep -E --color '^(./).*\.(cc|java|cpp|c|h|gradle|mk)$'`;do
	result=`wc -l $i | awk '{print $1}'`
	echo "Doing lines: $result, $i."
	((line+=result))
done

#for i in `find . -name "*.h"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done
#for i in `find . -name "*.cc"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done
#for i in `find . -name "*.cpp"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done
#for i in `find . -name "*.c"`;do ((line+=`cat -n $i | tail -n 1 | awk '{print $1}'`)) ; done

echo "lines: $line"
