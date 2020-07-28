# SimpleDrive
SimpleDrive is a fullstack web application where you can upload your files to store them on the cloud so that you can retrieve them anytime from anywhere. You can also share them with other people, add tags to them and more.
It is basically google drive but much simpler (hence the name :-)). The app was built for learning purposes. Here is the [link](https://simpledrive2020.herokuapp.com/) to try it

### Technologies used
1. Java 11
1. Spring Boot
1. Thymeleaf
1. MongoDB

### Further improvements
To make the app more scalable, it should be split into multiple microservices. The microservices will be then be run in docker containers and will communicate with each other using Kafka. Reactive Mongo also can be used instead of synchronous mongo to make the app more responsive in case of overwhelming requests (there is no real need for that however if Kafka is being used). 