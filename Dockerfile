FROM openjdk:17
EXPOSE 8080
ADD target/search-app.jar search-app.jar
ENTRYPOINT ["java","-jar","/search-app.jar"]

