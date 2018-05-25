package pw.eit.junit5.util;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.json.JSONObject;
import org.springframework.kafka.test.rule.KafkaEmbedded;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KafkaUtils
{
    private HashMap<Status, Set<ConsumerRecord<String, String>>> pollResponses = new HashMap<>();
    private HashMap<JsonMessageVerifier, Set<ConsumerRecord<String, String>>> verifiedLastIteration;

    private Consumer<String, String> consumer;
    private Producer<String, String> producer;
    private String topic;

    private long consumerPollTimeout;
    private long attemptDuration;

    public KafkaUtils(String topic)
    {
        this(100, 10000, System.getProperty(KafkaEmbedded.SPRING_EMBEDDED_KAFKA_BROKERS), topic);
    }

    /**
     * @param consumerPollTimeout ms to leave connection open to Kafka when buffer is empty.
     * @param attemptDuration     ms to continue to attempt verifying that message is sent.
     * @param bootstrapServers
     * @param topic
     */
    public KafkaUtils(long consumerPollTimeout, long attemptDuration, String bootstrapServers, String topic)
    {
        this.topic = topic;
        this.consumerPollTimeout = consumerPollTimeout;
        this.attemptDuration = attemptDuration;
        this.consumer = getConsumer(bootstrapServers, topic, topic, topic);
        this.producer = getProducer(bootstrapServers);
        pollResponses.put(Status.VERIFIED, new HashSet<>());
        pollResponses.put(Status.UNVERIFIED, new HashSet<>());
    }

    private void pollKafka(Long pollTimeout)
    {
        ConsumerRecords<String, String> consumerRecords = consumer.poll(pollTimeout);
        for (ConsumerRecord<String, String> consumerRecord : consumerRecords)
        {
            pollResponses.get(Status.UNVERIFIED)
                         .add(consumerRecord);
        }
    }

    /**
     * @param messageVerifiers
     * @return returns true only if each message verfier has verified at least one unique message.  If attemptDuration
     * is reached prior to this, then false is returned.
     */
    public Boolean wereMessagesSentToKafka(JsonMessageVerifier... messageVerifiers)
    {
        Callable<Boolean> wereMessagesSentToKafka = () -> verifyMessages(messageVerifiers);
        try
        {
            Awaitility.await()
                      .atMost(attemptDuration, TimeUnit.MILLISECONDS)
                      .until(wereMessagesSentToKafka);
        } catch (ConditionTimeoutException e)
        {
            return false;
        }
        return true;
    }

    private Boolean verifyMessages(JsonMessageVerifier... messageVerifiers)
    {
        // Create a map to hold verified messages.
        HashMap<JsonMessageVerifier, Set<ConsumerRecord<String, String>>> verifierVerifiedMap = new HashMap<>();
        for (JsonMessageVerifier messageVerifier : messageVerifiers)
        {
            verifierVerifiedMap.put(messageVerifier, new HashSet());
        }

        // Get all messages.
        pollKafka(consumerPollTimeout);

        // Verify messages.
        for (ConsumerRecord<String, String> consumerRecord : pollResponses.get(Status.UNVERIFIED))
        {
            for (JsonMessageVerifier messageVerifier : messageVerifiers)
            {
                Boolean isVerified = messageVerifier.verify(consumerRecord.value());

                if (isVerified)
                {
                    verifierVerifiedMap.get(messageVerifier)
                                       .add(consumerRecord);

                }
            }
        }

        // Only move on if all expected messages were received.
        verifiedLastIteration = new HashMap<>();
        Boolean wereAllMessagesSentToKafka = verifierVerifiedMap.entrySet()
                                                                .stream()
                                                                .map(entry ->
                                                                {
                                                                    // This way a user can see what messages were verified per iteration (if they choose).
                                                                    HashSet<ConsumerRecord<String, String>> verifiedMessages = new HashSet<>(entry.getValue());
                                                                    verifiedLastIteration.put(entry.getKey(), verifiedMessages);
                                                                    return entry.getValue()
                                                                                .size() > 0;
                                                                })
                                                                .reduce(true, (last, next) -> last && next);

        // Mark as processed.
        if (wereAllMessagesSentToKafka)
        {
            for (Set<ConsumerRecord<String, String>> consumerRecords : verifierVerifiedMap.values())
            {
                pollResponses.get(Status.UNVERIFIED)
                             .removeAll(consumerRecords);
                pollResponses.get(Status.VERIFIED)
                             .addAll(consumerRecords);
            }
        }
        return wereAllMessagesSentToKafka;
    }

    public static String prettyPrint(ConsumerRecord<String, String> consumerRecord)
    {
        JSONObject jsonObject = new JSONObject(consumerRecord.value());
        String prettyJson = jsonObject.toString(2);
        return prettyJson;
    }

    public Set<ConsumerRecord<String, String>> getUnverifiedMessages()
    {
        pollKafka(consumerPollTimeout);
        Set<ConsumerRecord<String, String>> unverifiedMessages = pollResponses.get(Status.UNVERIFIED);
        return unverifiedMessages;
    }

    public Set<ConsumerRecord<String, String>> getAllVerifiedMessages()
    {
        Set<ConsumerRecord<String, String>> verifiedMessages = pollResponses.get(Status.VERIFIED);
        return verifiedMessages;
    }

    public Set<ConsumerRecord<String, String>> getMessagesVerifiedLastIteration()
    {
        return verifiedLastIteration.values()
                                    .stream()
                                    .flatMap(Set::stream)
                                    .collect(Collectors.toSet());
    }

    private Consumer<String, String> getConsumer(String bootstrapServers, String clientId, String groupId, String... topics)
    {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        // Create the consumer using props.
        final Consumer<String, String> consumer =
                new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Arrays.asList(topics));
        return consumer;
    }

    private Producer<String, String> getProducer(String bootstrapServers)
    {
        Properties props = new Properties();
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("bootstrap.servers", bootstrapServers);
        Producer<String, String> producer = new KafkaProducer<>(props);
        return producer;
    }

    public RecordMetadata sendMessage(String message) throws ExecutionException, InterruptedException
    {
        RecordMetadata metadata = producer.send(new ProducerRecord<String, String>(topic, null, message))
                                          .get();
        return metadata;
    }

    public RecordMetadata sendMessageFromTestResource(String messageResourceName) throws IOException, ExecutionException, InterruptedException
    {
        String message = TestUtils.getTestResourceAsString(messageResourceName);
        return sendMessage(message);
    }
}
