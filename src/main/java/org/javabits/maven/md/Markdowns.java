package org.javabits.maven.md;

import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provide utility methods to extract Markdown info.
 *
 * @author Romain Gilles
 */
public final class Markdowns {

    static final String NO_TITLE = "No Title";
    private static final Pattern ATX_H1_PATTERN = Pattern.compile("^# ");

    private Markdowns() {
        throw new AssertionError("not for you!");
    }

    public static String getTitle(String markdownDocument) {
        for (String line : markdownDocument.split("\n")) {
            if (hasText(line)) {
                Matcher matcher = ATX_H1_PATTERN.matcher(line);
                if (matcher.find()) {
                    line = matcher.replaceFirst("");
                    if (hasText(line)) {
                        return line.trim();
                    }
                } else {
                    return line.trim();
                }
            }
        }
        return NO_TITLE;
    }

    private static boolean hasText(String line) {
        return !Strings.isNullOrEmpty(line) && line.trim().length() > 0;
    }
}
