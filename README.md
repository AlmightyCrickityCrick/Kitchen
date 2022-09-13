# Kitchen

The following project is the second half of the laboratory number 1 for Network Programming Course. The objective of the laboratory work is to simulate a restaurant environment within Docker Containers using Threads.

To run this project it is recommended to rebuild the FatJar using the command.
```
 ./gradlew :buildFatJar      
 ```
And then use the commands below to create the image and run the docker container. Beware that before running the kitchen-container, the docker network must be created first, as well as the system variable JAVA_HOME must be configured.
```
docker network create lina-restaurant network
docker build -t kitchen .     
docker run --name kitchen-container --network lina-restaurant-network  -p 8081:8081 kitchen
```
