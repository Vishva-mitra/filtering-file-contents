package org.example;
/**
 * Для написания кода использовалась Java 21 версии.
 * * При решении исходил из предположения, что в командную строку возможен ввод любой некорректной информации,
 * поэтому первым делом проводится проверка аргументов командной строки на допустимость и исключение дублей.
 * * Если для опции '-o' будут определены несколько допустимых путей, то использоваться будет последний из допустимых,
 * тот который был указан в командной строке позже.
 * * Если для опции '-p' будут определены несколько допустимых префиксов имен (допустимыми я определил префиксы которые
 * обязательно начинаются с латинской буквы и могут содержать только латинские буквы, цифры, нижнее подчеркивание и дефис)
 * то использоваться будет последний из допустимых, тот который был указан в командной строке позже.
 *  * В процессе фильтрации данных собирается статистика по каждому типу данных. Но если в командной строке не введено
 *  ни '-s', ни '-f', то статистика не отображается. Если же введены обе опции, то будет отбражаться полная статистика.
 */
import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            String defaultOutputStringTxt = "strings.txt";
            String defaultOutputIntTxt = "integers.txt";
            String defaultOutputFloatTxt = "floats.txt";

            // Все возможные одиночные опции
            HashSet<String> singleCommands = new HashSet<>();
            singleCommands.add("-a");
            singleCommands.add("-s");
            singleCommands.add("-f");

            // Хранение названий всех исходных txt файлов
            HashSet<String> inputFiles = new HashSet<>();

            // Хранение названия опций с аргументами для исполнения
            HashMap<String, String> commandsWithArguments = new HashMap<>();
            commandsWithArguments.put("-o", "");
            commandsWithArguments.put("-p", "");

            // Хранение одиночных опций, полученных из командной строки
            HashSet<String> singleCommandsForExecution = new HashSet<>();

            // Для работы с исходными txt файлами в которых есть данные
            ArrayList<ArrayList<String>> filesAndLines = new ArrayList<>();

            // Проверка аргументов командной строки и подготовка их для работы
            boolean isExistSourceFiles = checkingCommandLineArguments(args
                    , singleCommands
                    , inputFiles
                    , commandsWithArguments
                    , singleCommandsForExecution
                    , filesAndLines
                    , defaultOutputStringTxt
                    , defaultOutputIntTxt
                    , defaultOutputFloatTxt);

            if (isExistSourceFiles) {
                String directory = System.getProperty("user.dir");
                String fileSeparator = File.separator;
                new File(directory, commandsWithArguments.get("-o")).mkdirs();
                directory += fileSeparator + commandsWithArguments.get("-o");

                String outputStringTxt = directory + fileSeparator
                        + commandsWithArguments.get("-p") + defaultOutputStringTxt;
                String outputIntTxt = directory + fileSeparator
                        + commandsWithArguments.get("-p") + defaultOutputIntTxt;
                String outputFloatTxt = directory + fileSeparator
                        + commandsWithArguments.get("-p") + defaultOutputFloatTxt;
                String line;

                // Для сбора и расчетов статистики
                int countWhole = 0;
                int countDouble = 0;
                int countString = 0;
                int minLength = 0;
                int maxLength = 0;
                double minDouble = 0.0;
                double maxDouble = 0.0;
                double sumDouble = 0.0;
                double averageDouble;
                long minWhole = 0;
                long maxWhole = 0;
                long sumWhole = 0;
                double averageWhole;

                // Чтение и запись строк
                while (!filesAndLines.isEmpty()) {
                    line = getLineForWrite(filesAndLines.get(0));
                    if (line.matches("-?\\d+")) {
                        countWhole++;
                        if (countWhole == 1) {
                            minWhole = maxWhole = sumWhole = Long.parseLong(line);
                            File fileWhole = new File(outputIntTxt);
                            fileWhole.createNewFile();
                        } else {
                            minWhole = Math.min(minWhole, Long.parseLong(line));
                            maxWhole = Math.max(maxWhole, Long.parseLong(line));
                            sumWhole += Long.parseLong(line);
                        }
                        FileWriter writerInt;
                        if (singleCommandsForExecution.contains("-a")) {
                            writerInt = new FileWriter(outputIntTxt, true);
                        } else writerInt = new FileWriter(outputIntTxt);
                        writerInt.write(line);
                        writerInt.write("\n");
                        writerInt.close();
                    } else {
                        try {
                            Double.parseDouble(line);
                            countDouble++;
                            if (countDouble == 1) {
                                minDouble = maxDouble = sumDouble = Double.parseDouble(line);
                                File fileDouble = new File(outputFloatTxt);
                                fileDouble.createNewFile();
                            } else {
                                minDouble = Math.min(minDouble, Double.parseDouble(line));
                                maxDouble = Math.max(maxDouble, Double.parseDouble(line));
                                sumDouble += Double.parseDouble(line);
                            }
                            FileWriter writerFloat;
                            if (singleCommandsForExecution.contains("-a")) {
                                writerFloat = new FileWriter(outputFloatTxt, true);
                            } else writerFloat = new FileWriter(outputFloatTxt);
                            writerFloat.write(line);
                            writerFloat.write("\n");
                            writerFloat.close();
                        } catch (NumberFormatException e) {
                            countString++;
                            if (countString == 1) {
                                maxLength = minLength = line.length();
                                File fileString = new File(outputStringTxt);
                                fileString.createNewFile();
                            } else {
                                minLength = Math.min(minLength, line.length());
                                maxLength = Math.max(maxLength, line.length());
                            }
                            FileWriter writerString;
                            if (singleCommandsForExecution.contains("-a")) {
                                writerString = new FileWriter(outputStringTxt, true);
                            } else writerString = new FileWriter(outputStringTxt);
                            writerString.write(line);
                            writerString.write("\n");
                            writerString.close();
                        }
                    }
                    if (filesAndLines.get(0).get(1).equals(filesAndLines.get(0).get(2))) {
                        filesAndLines.remove(0);
                    } else Collections.rotate(filesAndLines, -1);
                }

                // Выбор статистики
                if (singleCommandsForExecution.contains("-f")) {
                    smallStat(countString, countWhole, countDouble);
                    if (countString > 0) {
                        System.out.println("Минимальная длина записанной строки: " + minLength);
                        System.out.println("Максимальная длина записанной строки: " + maxLength);
                    }
                    if (countWhole > 0) {
                        averageWhole = sumWhole / countWhole;
                        System.out.println("Минимальное целое число: " + minWhole);
                        System.out.println("Максимальное целое число: " + maxWhole);
                        System.out.println("Сумма целых чисел: " + sumWhole);
                        System.out.println("Среднее арифметическое целых чисел: " + averageWhole);
                    }
                    if (countDouble > 0) {
                        averageDouble = sumDouble / countDouble;
                        System.out.println("Минимальное вещественное число: " + minDouble);
                        System.out.println("Максимальное вещественное число: " + maxDouble);
                        System.out.println("Сумма вещественных чисел: " + sumDouble);
                        System.out.println("Среднее арифметическое вещественных чисел: " + averageDouble);
                    }
                } else if (singleCommandsForExecution.contains("-s")) {
                    smallStat(countString, countWhole, countDouble);
                }
            } else System.out.println("Отсутствуют исходные данные для работы.");
        } else System.out.println("Аргументы командной строки не переданы.");
    }

    public static boolean checkingCommandLineArguments(String[] args
            , HashSet<String> singleCommands
            , HashSet<String> inputFiles
            , HashMap<String, String> commandsWithArguments
            , HashSet<String> singleCommandsForExecution
            , ArrayList<ArrayList<String>> filesAndLines
            , String defaultOutputStringTxt
            , String defaultOutputIntTxt
            , String defaultOutputFloatTxt) throws IOException {

        final int MAX_FILE_NAME_LENGTH = 255;
        int maxPrefixLength = MAX_FILE_NAME_LENGTH
                - Math.max(defaultOutputStringTxt.length()
                , Math.max(defaultOutputIntTxt.length(), defaultOutputFloatTxt.length())
        );

        for (int i = 0; i < args.length; i++) {
            if (args[i].endsWith(".txt") && args[i].length() > 4) {
                File sourceFile = new File(args[i]);
                if (!sourceFile.exists()) System.out.println("Исходный файл '" + args[i] + "' не найден.");
                else if (!sourceFile.canRead())
                    System.out.println("Исходный файл '" + args[i] + "' недоступен для чтения.");
                else if (inputFiles.add(args[i])) {
                    long numberOfLines = getNumberOfLines(args[i]);
                    if (numberOfLines == 0) {
                        System.out.println("В файле '" + args[i] + "' нет данных.");
                    } else {
                        ArrayList<String> list = new ArrayList<>();
                        list.add(args[i]);
                        list.add(Long.toString(numberOfLines));
                        list.add("0");
                        filesAndLines.add(list);
                    }
                }
            } else if (singleCommands.contains(args[i])) singleCommandsForExecution.add(args[i]);
            else if (commandsWithArguments.containsKey(args[i])) continue;
            else {
                if (i > 0 && commandsWithArguments.containsKey(args[i - 1])) {
                    if ("-o".equals(args[i - 1])) {
                        try {
                            Files.isDirectory(Paths.get(args[i]));
                            commandsWithArguments.put(args[i - 1], args[i]);
                        } catch (InvalidPathException e) {
                            System.out.println("'" + args[i] + "' невозможно использовать для создания директории.");
                        }
                    } else if ("-p".equals(args[i - 1])) {
                        if (args[i].length() > maxPrefixLength) {
                            System.out.println("Длина префикса не должна превышать " + maxPrefixLength + " символа");
                        } else if (args[i].matches("^[a-zA-Z][a-zA-Z0-9_\\-]+")) {
                            commandsWithArguments.put(args[i - 1], args[i]);
                        } else
                            System.out.println("Префикс '" + args[i] + "' неприменим. " +
                                    "Префикс должен обязательно начинаться с латинской буквы " +
                                    "и может содержать только латинские буквы, цифры, нижнее подчеркивание и дефис");
                    }
                } else System.out.println("Аргумент командной строки '" + args[i] + "' не распознан.");
            }
        }
        return filesAndLines.size() > 0;
    }

    public static long getNumberOfLines(String fileName) throws IOException {
        long countLines = 0;
        try (var reader = new BufferedReader(new FileReader(fileName))) {
            while (reader.readLine() != null) {
                countLines++;
            }
            return countLines;
        }
    }

    public static String getLineForWrite(ArrayList<String> list) throws IOException {
        String fileName = list.get(0);
        long lineNumber = Long.parseLong(list.get(2)) + 1;
        String line = "";
        try (var reader = new BufferedReader(new FileReader(fileName))) {
            for (long i = 0; i < lineNumber; i++) {
                line = reader.readLine();
            }
        }
        list.set(2, Long.toString(lineNumber));
        return line;
    }


    public static void smallStat(int countString, int countWhole, int countDouble) {
        System.out.println("Количество записанных строк: " + countString);
        System.out.println("Количество записанных целых чисел: " + countWhole);
        System.out.println("Количество записанных вещественных чисел: " + countDouble);
    }
}