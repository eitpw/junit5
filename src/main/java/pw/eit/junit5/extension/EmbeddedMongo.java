package pw.eit.junit5.extension;

import pw.eit.junit5.util.RandomPortInitailizer;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EmbeddedMongo implements BeforeAllCallback, AfterAllCallback
{
    private static MongodExecutable mongodExe;
    private static MongodProcess mongodProcess;

    @Override
    public void afterAll(ExtensionContext context) throws Exception
    {
        mongodProcess.stop();
        mongodExe.stop();
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception
    {
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        String bindIp = "localhost";
        int port = RandomPortInitailizer.mongoPort;
        mongodExe = runtime.prepare(
                new MongodConfigBuilder().version(Version.V3_4_3)
                                         .net(new Net(bindIp, port, Network.localhostIsIPv6()))
                                         .build());
        mongodProcess = mongodExe.start();
        Awaitility.await()
                  .until(() -> mongodProcess != null);
    }

    public static MongodExecutable getMongodExe()
    {
        return mongodExe;
    }
}
