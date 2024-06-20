#!/bin/bash
i=54454047
while [ $i -lt 54457890 ]
do
  echo $i
    curl --location --request POST 'http://localhost:8085/api/syncBlock' --header 'Content-Type: application/json' --data '{"blockNum": '60845328'}'
  let i++
done