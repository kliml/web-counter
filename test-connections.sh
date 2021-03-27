#!/bin/bash

test_connection=$(curl -s -d "end testConnection" http://localhost:8080/web-counter/)

if echo test_connection | grep -q "test"
then
	echo Servelt online
else
	echo Could not connect to servlet
	exit -1
fi

# Test 1
for i in {1..10}
do
	curl -s -d "1" http://localhost:8080/web-counter/ > /dev/null &
done
sleep 1 # Give some time for curl to make requests
test1=$(curl -s -d "end test1" http://localhost:8080/web-counter/)
if [ "$test1" == "10 test1" ]
then 
	echo Test 1 passed
else
	echo Test 1 failed
	exit -1
fi
