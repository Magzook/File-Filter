package ru.magzook.filefilter;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;

public class FileFilter {

    private BigInteger minInteger, maxInteger, sumInteger = new BigInteger("0");
    private BigDecimal minDecimal, maxDecimal, sumDecimal = new BigDecimal("0");
    private int minStringLength, maxStringLength;
    private long countInteger, countDecimal, countString;

    private final File[] inputFiles, outputFiles;
    private final BufferedWriter[] bufferedWriters = new BufferedWriter[3];
    private final boolean[] hideErrorMessage_cannotCreateFile, hideErrorMessage_cannotWriteToFile;
    private final boolean append, fullStats;

    public FileFilter(File outputDirectory,
                      String outputFilenamePrefix,
                      boolean append,
                      boolean fullStats,
                      File... inputFiles) {
        this.inputFiles = inputFiles;
        outputFiles = new File[]{
                new File(outputDirectory, outputFilenamePrefix + "integers.txt"),
                new File(outputDirectory, outputFilenamePrefix + "floats.txt"),
                new File(outputDirectory, outputFilenamePrefix + "strings.txt")
        };
        hideErrorMessage_cannotCreateFile = new boolean[3];
        hideErrorMessage_cannotWriteToFile = new boolean[3];
        this.append = append;
        this.fullStats = fullStats;
    }

    public void work() {
        if (!append) {
            clearOutputFiles();
        }

        ArrayList<BufferedReader> bufferedReaders = new ArrayList<>();
        for (File inputFile : inputFiles) {
            try {
                bufferedReaders.add(new BufferedReader(new FileReader(inputFile)));
            } catch (FileNotFoundException e) {
                System.err.println("Не удалось найти файл " + inputFile.getAbsolutePath());
            }
        }

        int i = 0;
        try {
            while(!bufferedReaders.isEmpty()) {
                if (i == bufferedReaders.size()) {
                    i = 0;
                }
                String line;
                if ((line = bufferedReaders.get(i).readLine()) != null) {
                    parseAndProcessValue(line);
                    i++;
                } else {
                    bufferedReaders.get(i).close();
                    bufferedReaders.remove(i);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла");
            e.printStackTrace();
        }

        closeWriters();
        printStats();
    }

    private void clearOutputFiles() {
        for (File outputFile : outputFiles) {
            if (outputFile.isFile()) {
                try {
                    new PrintWriter(outputFile).close();
                } catch (FileNotFoundException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void parseAndProcessValue(String strValue) {
        try {
            BigInteger integerValue = new BigInteger(strValue);
            writeToOutputFile(strValue, 0);
            updateStats(integerValue);
            return;
        } catch (NumberFormatException ignored) {}

        try {
            BigDecimal decimalValue = new BigDecimal(strValue);
            writeToOutputFile(strValue, 1);
            updateStats(decimalValue);
            return;
        } catch (NumberFormatException ignored) {}

        writeToOutputFile(strValue, 2);
        updateStats(strValue);
    }

    private void writeToOutputFile(String value, int datatypeId) {
        if (bufferedWriters[datatypeId] == null) {
            try {
                bufferedWriters[datatypeId] = new BufferedWriter(new FileWriter(outputFiles[datatypeId], true));
            } catch (IOException e) {
                if (!hideErrorMessage_cannotCreateFile[datatypeId]) {
                    System.err.println("Не удалось создать файл " + outputFiles[datatypeId].getAbsolutePath());
                    hideErrorMessage_cannotCreateFile[datatypeId] = true;
                }
                return;
            }
        }

        try {
            bufferedWriters[datatypeId].write(value);
            bufferedWriters[datatypeId].append('\n');
        } catch (IOException e) {
            if (!hideErrorMessage_cannotWriteToFile[datatypeId]) {
                System.err.println("Ошибка вывода в файл " + outputFiles[datatypeId].getAbsolutePath());
                hideErrorMessage_cannotWriteToFile[datatypeId] = true;
            }
        }
    }

    private void updateStats(BigInteger integer) {
        countInteger++;
        if (fullStats) {
            if (minInteger == null) {
                minInteger = integer;
                maxInteger = integer;
            } else {
                minInteger = minInteger.min(integer);
                maxInteger = maxInteger.max(integer);
            }
            sumInteger = sumInteger.add(integer);
        }
    }

    private void updateStats(BigDecimal decimal) {
        countDecimal++;
        if (fullStats) {
            if (minDecimal == null) {
                minDecimal = decimal;
                maxDecimal = decimal;
            } else {
                minDecimal = minDecimal.min(decimal);
                maxDecimal = maxDecimal.max(decimal);
            }
            sumDecimal = sumDecimal.add(decimal);
        }
    }

    private void updateStats(String strValue) {
        countString++;
        if (fullStats) {
            minStringLength = Integer.min(strValue.length(), minStringLength);
            maxStringLength = Integer.max(strValue.length(), maxStringLength);
        }
    }

    private void closeWriters() {
        for (var bufferedWriter : bufferedWriters) {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                System.err.println("Ошибка закрытия ресурса");
                e.printStackTrace();
            }
        }
    }

    private void printStats() {
        if (countInteger > 0) {
            System.out.println("=== Целые числа ===");
            System.out.println("Всего:\t" + countInteger);
            if (fullStats) {
                System.out.println("Минимум:\t" + minInteger);
                System.out.println("Максимум:\t" + maxInteger);
                System.out.println("Сумма:\t\t" + sumInteger);
                System.out.println("Среднее:\t" + new BigDecimal(sumInteger).divide(BigDecimal.valueOf(countInteger), 10, RoundingMode.DOWN).stripTrailingZeros());
            }
        }
        if (countDecimal > 0) {
            System.out.println("=== Вещественные числа ===");
            System.out.println("Всего:\t" + countDecimal);
            if (fullStats) {
                System.out.println("Минимум:\t" + minDecimal);
                System.out.println("Максимум:\t" + maxDecimal);
                System.out.println("Сумма:\t\t" + sumDecimal);
                System.out.println("Среднее:\t" + sumDecimal.divide(BigDecimal.valueOf(countDecimal), 10, RoundingMode.DOWN).stripTrailingZeros());
            }
        }
        if (countString > 0) {
            System.out.println("=== Строки ===");
            System.out.println("Всего:\t" + countString);
            if (fullStats) {
                System.out.println("Наименьшая длина:\t" + minStringLength);
                System.out.println("Наибольшая длина:\t" + maxStringLength);
            }
        }
    }
}
