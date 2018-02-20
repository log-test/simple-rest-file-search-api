# File Search Services

This is a simple RESTful services for searching File.

## Dependencies:
* [JDK 1.8.x](http://www.oracle.com/technetwork/java/javase/overview/index.html)
* [Maven 3.x](https://maven.apache.org/index.html)
* [Lucene 5.5.x](https://lucene.apache.org/core/5_5_0/) for Lucene Index
* Java IDE, [Spring Tool Suite](https://spring.io/tools) is heavily recommended for best Spring integration

## Setup:

1) Import the project into an IDE as "Existing Maven Project"

2) Create an application.properties file in the config folder with your Lucene index location and port number. Refer to [application.properties](config/application.properties)



###Example 
	#Path to Lucene index directory
	lucene.index.location=D:\\files\\  
	And copy the example files Test1.txt and Test2.txt to the above filder for testing. In production it will be the real files.
	 

3) Run -> mvn clean package

4) The build should run successfully and generate a runnable jar in the target folder. This can be run via terminal, or in Spring Tool Suite click Run As "Spring Boot App"

## Using Services
The services may be accessed via HTTP requests. They return data in JSON format.

### Search query for Index records

* Type: GET

* Path: /search?query=<Lucene_Query>&count=<150|all> - Query string is a lucene format.
 
https://lucene.apache.org/core/2_9_4/queryparsersyntax.html

* Example Tests:

Single word:

http://localhost:9090/search?query=contents:bloganathan

Multiple Word:

http://localhost:9090/search?query=contents:"Test23" contents:"Test1"
