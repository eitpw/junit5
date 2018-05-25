package pw.eit.junit5.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class TestUtils
{
    public static String getTestResourceAsString(String testResourceName) throws IOException
    {
        ClassLoader classLoader = TestUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(testResourceName)
                                        .getFile());
        String message = FileUtils.readFileToString(file);
        return message;
    }

}
