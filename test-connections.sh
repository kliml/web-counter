#!/bin/bash

test_connection=$(curl -s -d "end testConnection" http://localhost:8080/web-counter/)

if echo "$test_connection" | grep -q "testConnection"
then
	echo Servlet online
else
	echo Could not connect to the servlet
	exit -1
fi

# Test 1 - 10 active connections
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

# Test 2 - 1000 active connections
for i in {1..1000}
do
	curl -s -d "1" http://localhost:8080/web-counter/ > /dev/null &
done
sleep 1 # Give some time for curl to make requests
test2=$(curl -s -d "end test2" http://localhost:8080/web-counter/)
if [ "$test2" == "1000 test2" ]
then 
	echo Test 2 passed
else
	echo Test 2 failed
	exit -1
fi

# Test 3 - invalid requests
invalid1=$(curl -s -d "endd testConnection" http://localhost:8080/web-counter/)
invalid2=$(curl -s -d "end all testConnection" http://localhost:8080/web-counter/)
invalid3=$(curl -s -d "0.0" http://localhost:8080/web-counter/)
invalid4=$(curl -s -d "1 testConnection" http://localhost:8080/web-counter/)

answers=("$invalid1"
	"$invalid2"
	"$invalid3"
	"$invalid4")

for answer in "${answers[@]}"
do
	if ! echo "$answer" | grep -q "Status 400"
	then
		echo Test 3 failed
 		exit -1
	fi
done
echo Test 3 passed
