package pw.eit.junit5.extension;

import org.apache.activemq.junit.EmbeddedActiveMQBroker;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EmbeddedActiveMQBroker extends org.apache.activemq.junit.EmbeddedActiveMQBroker implements BeforeAllCallback, AfterAllCallback
{
    @Override
    public void afterAll(ExtensionContext context) throws Exception
    {
        super.after();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception
    {
        try
        {
            super.before();
        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
    }
}
