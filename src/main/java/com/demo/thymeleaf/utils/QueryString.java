package com.demo.thymeleaf.utils;

import org.thymeleaf.expression.Uris;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class QueryString {

    private String originalQueryString;
    private Uris uris;
    private Map<String, List<KeyValueIndex>> state;

    private QueryString(String originalQueryString, Uris uris) {
        this.originalQueryString = uris.unescapeQueryParam(originalQueryString);
        this.uris = uris;
        state = createState();
    }

    /**
     * @param queryString The query string with at least 1 key=value pair.
     * @param uris        Handles escaping/unescaping the string
     * @return A valid instance
     * @throws IllegalArgumentException If there is not at least 1 key=value pair.
     */
    public static QueryString of(String queryString, Uris uris) throws IllegalArgumentException {
        if (!hasAtLeastOneKeyValuePair(queryString)) {
            throw new IllegalArgumentException("Query string must have at least key=value pair");
        }
        // unescape every percent-encoded (%HH) sequences present in input to avoid re escaping.
        return new QueryString(queryString, uris);
    }

    /**
     * Validate initial query string prior to constructing instance.
     */
    private static boolean isValidPair(String pair) {
        String[] tokens = pair.split("=");
        // Need to handle edge case where if you split '=value', index 0 is an empty string.
        return tokens.length == 2 && !tokens[0].isEmpty();
    }

    /**
     * Validate initial query string prior to constructing instance.
     */
    private static boolean hasAtLeastOneKeyValuePair(String queryString) {
        if (queryString == null) {
            return false;
        }

        String[] multiplePairs = queryString.split("&");
        if (multiplePairs.length == 0) {
            return isValidPair(queryString);
        }
        return Arrays.stream(multiplePairs).allMatch(QueryString::isValidPair);
    }

    /**
     * @return The unescaped query string.
     */
    public String getOriginalQueryString() {
        return originalQueryString;
    }


    public String replaceFirst(String key, String value) {
        List<KeyValueIndex> indices = state.get(key);
        if (indices != null) {
            indices.set(0, indices.get(0).updateValue(value));
        }
        return reconstructQueryString();
    }

    public String reconstructQueryString() {
        return state.entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .sorted(Comparator.comparing(KeyValueIndex::getOverallIndex))
                .map(keyValueIndex -> keyValueIndex.keyValue.escape(uris::escapeQueryParam))
                .collect(Collectors.joining("&"));
    }


//    public String replaceFirst(String key, String value) {
//        Set<String> seenKeys = new HashSet<>();
//        List<KeyValue> replacements = new ArrayList<>();
//
//        for (KeyValue keyValue : getKeyValues()) {
//            if (!seenKeys.contains(keyValue.key) && keyValue.key.equals(key)) {
//                replacements.add(new KeyValue(key, value));
//            } else {
//                replacements.add(keyValue);
//            }
//            seenKeys.add(key);
//        }
//
//        return replacements.stream()
//                .map(kv -> kv.escape(uris::escapeQueryParam))
//                .collect(Collectors.joining("&"));
//    }
//
//
//    public String replaceAll(String key, List<String> values) {
//        // Handle case where there are more keys than replacement values to avoid array out of bounds.
//        int maxReplacements = values.size();
//        int replacedIndex = 0;
//
//        List<KeyValue> replacements = new ArrayList<>();
//        for (KeyValue keyValue : getKeyValues()) {
//            boolean isReplaceable = replacedIndex < maxReplacements && keyValue.key.equals(key);
//            if (isReplaceable) {
//                replacements.add(new KeyValue(key, values.get(replacedIndex)));
//                replacedIndex++;
//            } else {
//                replacements.add(keyValue);
//            }
//        }
//        return replacements.stream()
//                .map(kv -> kv.escape(uris::escapeQueryParam))
//                .collect(Collectors.joining("&"));
//    }

    private String replaceAt(Map map) {
        return "";
    }

    public String remove(String key) {
        return null;
    }

    public String add(String key, String value) {
        return null;
    }

    /**
     * The originalQueryString string is transformed into a state map for easier manipulation. The value is of type {@code KeyValueIndex}
     * which tracks a key=value pairs index position in the overall originalQueryString string. This ensures the reconstructed
     * originalQueryString string maintains its original ordering. Lastly, the value is a list which naturally tracks relative key
     * ordering. In the context of the example below, its possible to perform an update such as
     * {@code stateMap["sort"].relativeIndex(1).update("address")} which changes the second occurrence of sort to
     * have the value {@code address}.
     *
     * <p>This example originalQueryString string is transformed into the below map.
     * {@code suburb=Melbourne&postcode=3000&page=0&sort=stars,desc&country=AU&sort=name}
     *
     *
     * <pre>
     * country = [4 -> country=AU]
     * postcode = [1 -> postcode=3000]
     * suburb = [0 -> suburb=Melbourne]
     * sort = [3 -> sort=stars,desc, 5 -> sort=name]
     * page = [2 -> page=0]
     * </pre>
     *
     * @return The state map.
     */
    private Map<String, List<KeyValueIndex>> createState() {
        return Pattern.compile("&").splitAsStream(originalQueryString)
                .map(paramPairs -> paramPairs.split("="))
                .map(paramPair -> new KeyValue(paramPair[0], paramPair[1]))
                .collect(new QueryCollector());
    }

    public Map<String, List<KeyValueIndex>> getState() {
        return Collections.unmodifiableMap(state);
    }

    public static class KeyValue {
        private String key;
        private String value;

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String escape(Function<String, String> escapeMapper) {
            return escapeMapper.apply(key) + "=" + escapeMapper.apply(value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyValue keyValue = (KeyValue) o;
            return Objects.equals(key, keyValue.key) &&
                    Objects.equals(value, keyValue.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public String toString() {
            return key + "=" + value;
        }
    }

    public static class KeyValueIndex {
        private int overallIndex;
        private KeyValue keyValue;

        public KeyValueIndex(int overallIndex, KeyValue keyValue) {
            this.overallIndex = overallIndex;
            this.keyValue = keyValue;
        }

        public int getOverallIndex() {
            return overallIndex;
        }

        public KeyValue getKeyValue() {
            return keyValue;
        }

        public KeyValueIndex updateValue(String value) {
            return new KeyValueIndex(overallIndex, new KeyValue(keyValue.key, value));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            KeyValueIndex that = (KeyValueIndex) o;
            return overallIndex == that.overallIndex &&
                    Objects.equals(keyValue, that.keyValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(overallIndex, keyValue);
        }

        @Override
        public String toString() {
            return overallIndex + " -> " + keyValue;
        }
    }


    public static class QueryCollector implements
            Collector<KeyValue, Map<String, List<KeyValueIndex>>, Map<String, List<KeyValueIndex>>> {

        private int index = 0;

        private KeyValueIndex createKeyValueIndex(KeyValue kv) {
            KeyValueIndex keyValueIndex = new KeyValueIndex(index, kv);
            index++;
            return keyValueIndex;
        }

        @Override
        public Supplier<Map<String, List<KeyValueIndex>>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<Map<String, List<KeyValueIndex>>, KeyValue> accumulator() {
            return (stateMap, keyValue) ->
                    stateMap.merge(keyValue.key, new ArrayList<>(Arrays.asList(createKeyValueIndex(keyValue))),
                            (existingValues, newValues) -> {
                                existingValues.addAll(newValues);
                                return existingValues;
                            });
        }

        @Override
        public BinaryOperator<Map<String, List<KeyValueIndex>>> combiner() {
            /*
             * Unfortunately there is no mapWithIndex on streams, so to get around that a mutable index is kept
             * which needs to run sequentially to maintain the original key=value originalQueryString string ordering.
             */
            return (a, b) -> {
                throw new UnsupportedOperationException("Parallel streams not permitted");
            };
        }

        @Override
        public Function<Map<String, List<KeyValueIndex>>, Map<String, List<KeyValueIndex>>> finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.noneOf(Characteristics.class);
        }
    }

}
