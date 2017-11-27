#!/bin/bash
export VM_HOST=boot2docker

# Wait for a certain service to become available

wait() {
while true; do
  if ! nc64 -z $VM_HOST $1
  then
    echo "$2 not available, retrying..."
    sleep 1
  else
    echo "$2 is available"
    break;
  fi
done;
}

echo "Launching movie-booking-app, Cassandra and Elasticsearch"
echo "This can take a couple of seconds"

docker-compose up -d
wait 9200 Elasticsearch
wait 9042 Cassandra