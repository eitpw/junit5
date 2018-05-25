package pw.eit.junit5.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

import javax.jms.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ActiveMQUtils implements ExceptionListener
{
    private ActiveMQConnectionFactory connectionFactory;
    private String queueName;
    private long attemptDuration;
    private long consumerTimeout;

    public ActiveMQUtils(String queueName)
    {
        this(10000, 100, "vm://embedded-broker?create=false", null, null, queueName);
    }

    public ActiveMQUtils(long attemptDuration, long consumerTimeout, String brokerUrl, String username, String password, String queueName)
    {
        this.attemptDuration = attemptDuration;
        this.consumerTimeout = consumerTimeout;
        // Create a ConnectionFactory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        if (username != null && password != null)
        {
            connectionFactory.setUserName(username);
            connectionFactory.setPassword(password);
        }
        this.connectionFactory = connectionFactory;
        this.queueName = queueName;
    }

    public void send(String message) throws JMSException
    {
        send(message, null);
    }

    public void send(String message, Consumer<Message> actOnMessage) throws JMSException
    {
        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue(queueName);

        // Create a MessageProducer from the Session to the Topic or Queue
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // Create a messages
        TextMessage textMessage = session.createTextMessage(message);
        if (actOnMessage != null)
        {
            actOnMessage.accept(textMessage);
        }

        // Tell the producer to send the message
        producer.send(textMessage);

        // Clean up
        producer.close();
        session.close();
        connection.close();
    }

    private String receive() throws JMSException
    {
        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();

        connection.setExceptionListener(this);

        // Create a Session
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create the destination (Topic or Queue)
        Destination destination = session.createQueue(queueName);

        // Create a MessageConsumer from the Session to the Topic or Queue
        MessageConsumer consumer = session.createConsumer(destination);

        // Wait for a message
        Message message = consumer.receive(consumerTimeout);

        if (message == null)
        {
            return null;
        }

        String text = null;

        if (message instanceof TextMessage)
        {
            TextMessage textMessage = (TextMessage) message;
            text = textMessage.getText();
        } else
        {
            throw new RuntimeException("Currently ActiveMQUtils only works with messages of type TextMessage.");
        }

        consumer.close();
        session.close();
        connection.close();
        return text;
    }

    private Boolean verifyMessage(JsonMessageVerifier messageVerifier) throws JMSException
    {
        String message = receive();
        if (message == null)
        {
            return false;
        }
        Boolean isVerified = messageVerifier.verify(message);
        return isVerified;
    }

    public Boolean wasMessageReceivedByActiveMQ(JsonMessageVerifier messageVerifier)
    {
        Callable<Boolean> wereMessagesSentToKafka = () -> verifyMessage(messageVerifier);
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

    public synchronized void onException(JMSException ex)
    {
        throw new RuntimeException("A JMSException was thrown.", ex);
    }
}
