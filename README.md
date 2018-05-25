# Usage
```
// If your project uses kafka, you must use this version of kafka-clients.
compile("org.apache.kafka:kafka-clients:1.0.1") 
 
// The project must be included in your testCompile.
testCompile("com.gci:junit5:1.0.17")
 
// Your gradle version must be 4.7 or higher.
task wrapper(type: Wrapper) {
    gradleVersion = '4.7'
}
 
// You must use gradle's JUnit 5 functionality.
test {
    useJUnitPlatform()
}
```
## Kafka
First, include the following at the top of your unit test.
```
@ExtendWith(EmbeddedKafka.class)
public class UnitTest
```
This will start up an embedded Kafka instance.  You can access it using the following properties.
```
# In Spring:
${spring.embedded.kafka.brokers}
# Via system properties:
System.getProperty(KafkaEmbedded.SPRING_EMBEDDED_KAFKA_BROKERS)
``` 

## Mongo
First, include the following at the top of your unit test.
```
@ExtendWith(EmbeddedMongo.class)
@ContextConfiguration(initializers = RandomPortInitailizer.class)
public class UnitTest
```
This will start up an embedded Mongo instance.  You can access it on localhost using the following properties.
```
# In Spring:
${com.gci.junit5.mongo.port}
# Via a static property:
RandomPortInitailizer.mongoPort
```

## ActiveMQ
First, include the following at the top of your unit test.
```
@ExtendWith(EmbeddedActiveMQBroker.class)
public class UnitTest
```
This will start up an embedded ActiveMQ instance.  You can access it using the broker url `vm://embedded-broker?create=false`.

## MySQL
First, include the following at the top of your unit test.
```
@ExtendWith(EmbeddedMysql.class)
@ContextConfiguration(initializers = RandomPortInitailizer.class)
public class UnitTest
```
This will start up an embedded MySQL instance.  You can access it on localhost using the following properties.
```
# In Spring:
${com.gci.junit5.mysql.port}
# Via a static property:
RandomPortInitailizer.mysqlPort
```

## WireMock
First, include the following at the top of your unit test.
```
import static com.github.tomakehurst.wiremock.client.WireMock.*;
@ExtendWith(WireMock.class)
@ContextConfiguration(initializers = RandomPortInitailizer.class)
public class UnitTest
```
This will start up WireMock.  You can access it on localhost using the following properties.
```
# In Spring:
${com.gci.junit5.wiremock.port}
# Via a static property:
RandomPortInitailizer.wireMockPort
```
At this point, all you need to do is to create stubs.  For examples, see [the WireMock docs](http://one.wiremock.org/stubbing.html).
