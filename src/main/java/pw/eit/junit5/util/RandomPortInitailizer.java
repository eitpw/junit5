package pw.eit.junit5.util;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.util.SocketUtils;

public class RandomPortInitailizer
        implements ApplicationContextInitializer<ConfigurableApplicationContext>
{

    public static int mongoPort = SocketUtils.findAvailableTcpPort();
    public static int mysqlPort = SocketUtils.findAvailableTcpPort();
    public static int wireMockPort = SocketUtils.findAvailableTcpPort();

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext)
    {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                "com.gci.junit5.mongo.port=" + mongoPort, "com.gci.junit5.mysql.port=" + mysqlPort, "com.gci.junit5.wiremock.port=" + wireMockPort);
    }

}
