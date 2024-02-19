package ru.job4j.finder;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Finder {

    private static int argsCounter = 4;
    private static List<String> firstCLIArgs = List.of("d", "n", "t", "o");
    public static List<String> searchValues = List.of("name", "mask", "regex");
    private static String out = "log.txt";

    private static void argsCountChecker(String[] args) {
        if (args.length != argsCounter) {
            throw new IllegalArgumentException(
                    String.format("Error: Have to pass exactly '%d' args!",
                            argsCounter));
        }
    }

    private static void checkFirstSplitArg(Map<String, String> preparedArgs) {
        for (String firstCLIArg : firstCLIArgs) {
            if (!preparedArgs.containsKey(firstCLIArg)) {
                throw new IllegalArgumentException(
                        String.format("Error: First argument should be exactly '%s'",
                                firstCLIArg));
            }
        }
    }

    private static void argumentsDetailedChecker(ArgsName preparedArgs) {
        checkFirstSplitArg(preparedArgs.getValues());

        int argCounter = -1;
        Path splitArg = Path.of(preparedArgs.get(firstCLIArgs.get(++argCounter)));
        if (!Files.exists(splitArg) && !Files.isDirectory(splitArg)) {
            throw new IllegalArgumentException(
                    String.format("Error: File '%s' doesn't exist or file isn't a directory",
                            splitArg.toAbsolutePath()));
        }

        ++argCounter;
        final String thirdParam = Path.of(preparedArgs.get(firstCLIArgs.get(++argCounter))).toString();
        if (searchValues.stream().noneMatch(elem -> elem.equals(thirdParam))) {
            throw new IllegalArgumentException(
                    String.format("Error: Third param should conform any of '%s' names ",
                            searchValues));
        }

        splitArg = Path.of(preparedArgs.get(firstCLIArgs.get(++argCounter)));
        if (!splitArg.toString().endsWith(out)) {
            throw new IllegalArgumentException(
                    String.format("Error: Need to provide output to '%s'",
                            out));
        }
    }

    private static String maskToRegexConverter(String arg) {
        return arg.replace("*", "\\S*")
                .replace("?", "\\S{1}")
                .replace(".", "\\.");
    }

    public static List<Path> searchFiles(ArgsName argsName) throws IOException {
        argumentsDetailedChecker(argsName);

        final String thirdParam = Path.of(argsName.get(firstCLIArgs.get(2))).toString();
        String secondParam = argsName.get(firstCLIArgs.get(1));
        specificStringChekcer(argsName, thirdParam, secondParam);

        Predicate<Path> predicate = null;
        if (searchValues.get(0).equals(thirdParam)) {
            predicate = path -> path.getFileName().toFile().toString().equals(secondParam);
        } else if (searchValues.get(1).equals(thirdParam)) {
            predicate = path -> path.getFileName().toFile().toString().matches(maskToRegexConverter(secondParam));
        } else if (searchValues.get(2).equals(thirdParam)) {
            try {
                Pattern pattern = Pattern.compile(secondParam);
                predicate = path -> pattern.matcher(path.getFileName().toString()).matches();
            } catch (PatternSyntaxException e) {
                System.out.println("Wrong pattern in params, proper use n=\"right pattern\" "
                        + System.lineSeparator()
                        + e);
            }
        }

        SearchFiles searcher = new SearchFiles(predicate);
        Files.walkFileTree(Path.of(argsName.get(firstCLIArgs.get(0))), searcher);
        return searcher.getPaths();
    }

    private static void specificStringChekcer(ArgsName argsName, String thirdParam, String secondParam) {
        if (searchValues.get(0).equals(thirdParam)
                && !argsName.get(firstCLIArgs.get(1)).matches("\\b\\S+\\.\\S+\\b")) {
            throw new IllegalArgumentException(
                    String.format("Error: File name+extension shouldn't be empty, while you provide: '%s' ",
                            secondParam));
        }
    }

    public static void writeFiles(List<Path> files) {
        try (BufferedWriter br = new BufferedWriter(new FileWriter(out))) {
            for (Path path : files) {
                br.write(path + System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        argsCountChecker(args);

        List<Path> list = searchFiles(ArgsName.of(args));

        writeFiles(list);
    }
}
