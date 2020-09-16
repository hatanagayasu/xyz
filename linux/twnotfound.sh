#!/bin/bash

IFS='
'
declare -i n
declare -a a
n=0
for s in `cat /etc/tripwire/twnotfound.txt`
do
  a[$n]=${s##* }
  n=$n+1
done

mark()
{
  for ((i=0; i<$n; i++))
  do
    if [ $3 == ${a[$i]} ]; then
      echo $1\#$2
      return
    fi
  done
  echo $1$2
}

for s in `cat /etc/tripwire/twpol.txt`
do
  if echo $s | grep '^ \+/' 1> /dev/null; then
    s1=${s%%/*}
    s2=/${s#*/}
    s3=${s2%% *}
    mark $s1 $s2 $s3
  else
    echo $s
  fi
done
