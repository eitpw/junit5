package pw.eit.junit5.extension;

import pw.eit.junit5.util.RandomPortInitailizer;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

public class WireMock implements BeforeAllCallback, AfterAllCallback
{
    private static WireMockServer wireMockServer;

    @Override
    public void afterAll(ExtensionContext context) throws Exception
    {
        wireMockServer.stop();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception
    {
        wireMockServer = new WireMockServer(RandomPortInitailizer.wireMockPort);
        wireMockServer.start();
        configureFor("localhost", RandomPortInitailizer.wireMockPort);
    }
}
