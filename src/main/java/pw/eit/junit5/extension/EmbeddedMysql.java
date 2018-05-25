package pw.eit.junit5.extension;

import pw.eit.junit5.util.RandomPortInitailizer;
import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.MysqldConfig;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.distribution.Version.v5_7_latest;

public class EmbeddedMysql implements BeforeAllCallback, AfterAllCallback
{
    private static com.wix.mysql.EmbeddedMysql mysqld;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception
    {
        MysqldConfig config = aMysqldConfig(v5_7_latest)
                .withPort(RandomPortInitailizer.mysqlPort)
                .build();
        mysqld = com.wix.mysql.EmbeddedMysql.anEmbeddedMysql(config)
                                            .addSchema("testschema")
                                            .start();
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception
    {
        mysqld.stop();
    }

    public static com.wix.mysql.EmbeddedMysql getMysqld()
    {
        return mysqld;
    }
}
