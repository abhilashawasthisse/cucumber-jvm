package io.cucumber.core.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

/**
 * Identifies scenarios and examples (pickles) in a feature.
 * <p>
 * The structure {@code URI[:LINE]*} is a URI as defined by
 * {@link FeatureIdentifier} followed by a sequence of line
 * numbers each preceded by a colon.
 *
 * @see FeatureIdentifier
 */
public class FeatureWithLines implements Serializable {
    private static final long serialVersionUID = 20190126L;
    private static final Pattern FEATURE_COLON_LINE_PATTERN = Pattern.compile("^(.*?):([\\d:]+)$");
    private static final String INVALID_PATH_MESSAGE = " is not valid. Try URI[:LINE]*";

    private final URI uri;
    private final SortedSet<Integer> lines;

    private FeatureWithLines(URI uri, Collection<Integer> lines) {
        this.uri = uri;
        this.lines = Collections.unmodifiableSortedSet(new TreeSet<>(lines));
    }

    public static FeatureWithLines create(URI uri, Collection<Integer> lines) {
        return new FeatureWithLines(uri, lines);
    }

    public static FeatureWithLines parse(String uri, Collection<Integer> lines) {
        return new FeatureWithLines(FeatureIdentifier.parse(uri), lines);
    }

    public static FeatureWithLines parse(String uri, Integer... lines) {
        return new FeatureWithLines(FeatureIdentifier.parse(uri), asList(lines));
    }

    public static FeatureWithLines parse(String featurePath) {
        Matcher matcher = FEATURE_COLON_LINE_PATTERN.matcher(featurePath);

        try {
            if (!matcher.matches()) {
                return parseFeatureIdentifier(featurePath);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(featurePath + INVALID_PATH_MESSAGE, e);
        }

        String uriGroup = matcher.group(1);
        if (uriGroup.isEmpty()) {
            throw new IllegalArgumentException(featurePath + INVALID_PATH_MESSAGE);
        }

        try {
            return parseFeatureIdentifierAndLines(uriGroup, matcher.group(2));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(featurePath + INVALID_PATH_MESSAGE, e);
        }
    }

    private static FeatureWithLines parseFeatureIdentifierAndLines(String uriGroup, String linesGroup) {
        URI uri = FeatureIdentifier.parse(uriGroup);
        List<Integer> lines = toInts(linesGroup.split(":"));
        return new FeatureWithLines(uri, lines);
    }

    private static FeatureWithLines parseFeatureIdentifier(String pathName) {
        URI uri = FeatureIdentifier.parse(pathName);
        return new FeatureWithLines(uri, Collections.<Integer>emptyList());
    }

    private static List<Integer> toInts(String[] strings) {
        List<Integer> result = new ArrayList<>();
        for (String string : strings) {
            result.add(Integer.parseInt(string));
        }
        return result;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(uri.toString());
        for (Integer line : lines) {
            builder.append(':');
            builder.append(line);
        }
        return builder.toString();
    }

    public SortedSet<Integer> lines() {
        return lines;
    }

    public URI uri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeatureWithLines that = (FeatureWithLines) o;
        return uri.equals(that.uri) && lines.equals(that.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, lines);
    }
}