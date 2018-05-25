package pw.eit.junit5.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode
public class JsonMessageVerifier
{
    private Map<String, String> expectedValueMap = new HashMap<>();

    public JsonMessageVerifier()
    {
    }

    public JsonMessageVerifier(String jsonPointer, String expectedValue)
    {
        expectedValueMap.put(jsonPointer, expectedValue);
    }

    public void addExpectedValue(String jsonPointer, String expectedValue)
    {
        expectedValueMap.put(jsonPointer, expectedValue);
    }

    public Boolean verify(String message)
    {
        JSONObject jsonObject = new JSONObject(message);
        Boolean isVerified = getExpectedValueMap()
                .entrySet()
                .stream()
                .map(entry -> ((String) jsonObject.query(entry.getKey())).equals(entry.getValue()))
                .reduce(true, (last, next) -> last && next);
        return isVerified;
    }
}
