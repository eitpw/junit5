package pw.eit.junit5;

import pw.eit.junit5.util.ActiveMQUtils;
import pw.eit.junit5.util.KafkaUtils;
import pw.eit.junit5.util.MongoUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class TestConfig
{
    @Value("${com.gci.junit5.kafka.topic}")
    private String topic;

    @Value("${com.gci.junit5.mongo.database}")
    private String mongoDatabase;

    @Value("${com.gci.junit5.activemq.queuename}")
    private String activMQQueueName;

    @Bean
    public KafkaUtils getKafkaUtils()
    {
        return new KafkaUtils(topic);
    }

    @Bean
    public MongoUtils getMongoUtils()
    {
        return new MongoUtils(mongoDatabase);
    }

    @Bean
    public ActiveMQUtils getActiveMQUtils()
    {
        return new ActiveMQUtils(activMQQueueName);
    }
}
