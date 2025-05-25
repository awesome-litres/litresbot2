package litresbot.utils;

import java.util.List;

public class StringUtils {
    public static String joinStrings(String delimiter, List<String> strings) {
        var result = new StringBuilder();
        boolean first = true;
        for (var i = 0; i < strings.size(); i++) {
            final var currentString = strings.get(i);
            if (currentString == null || currentString.isEmpty()) {
                continue;
            }
            if (!first) {
                result.append(delimiter);
            }
            result.append(strings.get(i));
            first = false;
        }
        return result.toString();
    }
}