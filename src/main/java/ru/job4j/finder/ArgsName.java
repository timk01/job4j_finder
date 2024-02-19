package ru.job4j.finder;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArgsName {

    private final Map<String, String> values = new LinkedHashMap<>();

    public String get(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException(
                    String.format("This key: '%s' is missing", key));
        }
        return values.get(key);
    }

    public Map<String, String> getValues() {
        return values;
    }

    private void preliminaryArgChecker(String arg) {
        if (!arg.contains("=")) {
            throw new IllegalArgumentException(
                    String.format("Error: This argument '%s' does not contain an equal sign", arg));
        }
        if (!arg.startsWith("-")) {
            throw new IllegalArgumentException(
                    String.format("Error: This argument '%s' does not start with a '-' character", arg));
        }
        if (arg.startsWith("-=")) {
            throw new IllegalArgumentException(
                    String.format("Error: This argument '%s' does not contain a key", arg));
        }
        int i = arg.indexOf("=");
        if (arg.substring(i + 1).isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Error: This argument '%s' does not contain a value",
                            arg));
        }
    }

    private void parse(String[] args) {
        for (String arg : args) {
            preliminaryArgChecker(arg);
            String[] splitted = arg.split("=", 2);
            values.put(splitted[0].substring(1), splitted[1]);
        }
    }

    public static ArgsName of(String[] args) {
        if (args.length == 0) {
            throw new IllegalArgumentException("Arguments not passed to program");
        }
        ArgsName names = new ArgsName();
        names.parse(args);
        return names;
    }

    public static void main(String[] args) {
        ArgsName threeParams = ArgsName.of(
                new String[]{args[0], args[1], args[2]}
        );
        System.out.println(threeParams.get("d"));
    }
}