Code for the movie -booking -app

# How to build
To build and package the movie -booking -app  as a docker application, run the script `docker-build.sh`. This script
will instruct sbt to build the application, package the application and build a docker image, tag it and store
it in the local docker repository.

To launch the project which includes launching the movie -booking -app example, the Cassandra database and Elasticsearch.

If docker native is installed then the following should be added in the host entry for the simplicity.

```
127.0.0.1  boot2docker
```

Now you can reference the docker environment in the URL by `boot2docker` instead of the ip address. 
Additionally ,json files are also given in the root folder to test the endpoints 

To register a movie , simply do:
http -v post boot2docker:8080/api/v1/registration <movie-reg.json
To reserve a movie,the url will be:

```
http -v post boot2docker:8080/api/v1/reservation <movie-reservation.json


```
I have used httpie in my case.Curl or Any rest endpoint tool will work for testing the results.