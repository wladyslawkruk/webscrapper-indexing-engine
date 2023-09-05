The program provides you a website indexing engine with a laconic frontend : 

General technology stack:
- Spring boot
- PostgreSql
- Lucene morphology
- Hibernate
- Thymeleaf



## Environment

The necessary environment of the app consists of PostgreSql and pgAdmin. Please run the docker-compose file with :
```
$sudo docker-compose up -d
```
pgAdmin shell will be available at http://localhost:5050.

In order to establish the connection with pgAdmin, you should type in the proper localhost name which is located at:
```
$sudo docker inspect 1518ff981f69
```
where 1518ff981f69 is the container ID (could be different in your case)
Your container ID also presents the hostname in the container settings.

"Config": {
"Hostname": "1518ff981f69",
"Domainname": "",
"User": "",.....

## Run app

Now you can run application from your IDE. It can be accessed at 
http://localhost:8080/


## Basic interactions
1) http://localhost:8080/api/statistics   GET

Returns statistics for the site list from application.yml

2) http://localhost:8080/api/startIndexing   GET
Run indexing 

3) http://localhost:8080/api/stopIndexing   GET
Terminate indexing

4) http://localhost:8080/api/indexPage?url=    POST
Run single page indexing

5) http://localhost:8080/api/search?query=...&site=..&offset=..&limit=..  GET
Search pages by query. Parameters site/offset/limit are optional and considered null/0/20 when not provided







