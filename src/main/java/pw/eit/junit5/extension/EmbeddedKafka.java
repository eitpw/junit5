package pw.eit.junit5.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.kafka.test.rule.KafkaEmbedded;

public class EmbeddedKafka extends KafkaEmbedded implements BeforeAllCallback, AfterAllCallback
{
    public EmbeddedKafka()
    {
        super(1, false, 1);
    }

    public EmbeddedKafka(int count)
    {
        super(count);
    }

    public EmbeddedKafka(int count, boolean controlledShutdown, String... topics)
    {
        super(count, controlledShutdown, topics);
    }

    public EmbeddedKafka(int count, boolean controlledShutdown, int partitions, String... topics)
    {
        super(count, controlledShutdown, partitions, topics);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception
    {
        super.after();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception
    {
        super.before();
    }
}
