package pw.eit.junit5.test;

import com.mongodb.client.model.Filters;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pw.eit.junit5.TestConfig;
import pw.eit.junit5.extension.*;
import pw.eit.junit5.mysql.domain.MySQLTestObj;
import pw.eit.junit5.mysql.repository.MySQLTestObjRepository;
import pw.eit.junit5.util.*;

import javax.jms.JMSException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ExtendWith(EmbeddedMongo.class)
@ExtendWith(EmbeddedKafka.class)
@ExtendWith(EmbeddedActiveMQBroker.class)
@ExtendWith(EmbeddedMysql.class)
@ExtendWith(WireMock.class)
@ActiveProfiles("unit")
@Tag("unit")
@ContextConfiguration(classes = {TestConfig.class},
                      initializers = RandomPortInitailizer.class)
public class UnitTest
{
    @Autowired
    private KafkaUtils kafkaUtils;

    @Autowired
    private MongoUtils mongoUtils;

    @Autowired
    private ActiveMQUtils activeMQUtils;

    @Autowired
    private MySQLTestObjRepository mySQLTestObjRepository;

    @Test
    public void kafkaTest() throws Exception
    {
        // Pull message from test/resources.
        String messageFromTestResource = TestUtils.getTestResourceAsString("test.json");

        // Send message to kafka.
        kafkaUtils.sendMessage(messageFromTestResource);
        JsonMessageVerifier jsonMessageVerifier = new JsonMessageVerifier("/hello", "world");

        Boolean kafkaSuccess = kafkaUtils.wereMessagesSentToKafka(jsonMessageVerifier);
        Assertions.assertTrue(kafkaSuccess);

        // Pull and send in one step.
        kafkaUtils.sendMessageFromTestResource("test.json");
        kafkaSuccess = kafkaUtils.wereMessagesSentToKafka(jsonMessageVerifier);
        Assertions.assertTrue(kafkaSuccess);
    }

    @Test
    public void mongoTest()
    {
        // Write an object to mongo.
        JSONObject jsonObject = new JSONObject("{\"hello\": \"world\"}");
        mongoUtils.write("test", jsonObject);

        // Verify the object was written.
        Boolean mongoSuccess = mongoUtils.wereNDocumentsAddedInMongo("test", Filters.eq("hello", "world"), 1);
        Assertions.assertTrue(mongoSuccess);
    }

    @Test
    public void activeMQTest() throws JMSException
    {
        // Send message to ActiveMQ.
        activeMQUtils.send("{\"hello\": \"world\"}");

        // Consume message and verify that it is correct.
        JsonMessageVerifier jsonMessageVerifier = new JsonMessageVerifier("/hello", "world");
        Boolean activeMQSuccess = activeMQUtils.wasMessageReceivedByActiveMQ(jsonMessageVerifier);
        Assertions.assertTrue(activeMQSuccess);
    }


    @Test
    public void mySQLTest()
    {
        MySQLTestObj outgoingMySQLTestObj = new MySQLTestObj();
        outgoingMySQLTestObj.setMessage("Hello World");
        outgoingMySQLTestObj = mySQLTestObjRepository.save(outgoingMySQLTestObj);
        MySQLTestObj incomingMySQLTestObj = mySQLTestObjRepository.findById(outgoingMySQLTestObj.getId())
                                                                  .get();
        Assertions.assertNotNull(incomingMySQLTestObj);
    }

    @Test
    public void wireMockTest() throws Exception
    {
        String helloWorld = "Hello world!";
        stubFor(get(urlEqualTo("/test/flat"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody(helloWorld)));
        String response = getHTML("http://localhost:" + RandomPortInitailizer.wireMockPort + "/test/flat");
        Assertions.assertEquals(helloWorld, response);

    }

    public static String getHTML(String urlToRead) throws Exception
    {
        URL url = new URL(urlToRead);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null)
        {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        return content.toString();
    }

    @Test
    public void testUtilsTest() throws Exception
    {
        String csv = TestUtils.getTestResourceAsString("test/test.csv");
        Assertions.assertEquals("bob,denver", csv);
    }
}
