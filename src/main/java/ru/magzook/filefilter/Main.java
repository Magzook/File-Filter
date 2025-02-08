package ru.magzook.filefilter;

import java.io.File;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        File outputDirectory = new File(System.getProperty("user.dir"));
        ArrayList<File> inputFiles = new ArrayList<>();
        boolean enableAppend = false, enableFullStats = false;
        String outputFilenamePrefix = "";
        char[] illegalChars = new char[]{'\\', '/', ':', '*', '?', '"', '<', '>', '|'};

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            try {
                switch (arg) {
                    case "-o":
                        outputDirectory = new File(args[++i]);
                        if (!outputDirectory.isDirectory()) {
                            System.err.println("(!) Не удалось найти папку для файлов с результатами по пути " + outputDirectory.getAbsolutePath());
                            System.err.println("(!) Файлы с результатами будут помещены в папку с приложением");
                            outputDirectory = new File(System.getProperty("user.dir"));
                        }
                        break;
                    case "-p":
                        outputFilenamePrefix = args[++i];
                        boolean badPrefix = false;
                        for (char illegalChar : illegalChars) {
                            if (outputFilenamePrefix.indexOf(illegalChar) != -1) {
                                outputFilenamePrefix = outputFilenamePrefix.replace("" + illegalChar, "#");
                                badPrefix = true;
                            }
                        }
                        if (badPrefix) {
                            System.err.println("В префиксе недопустимые символы были заменены на #");
                        }
                        break;
                    case "-a":
                        enableAppend = true;
                        break;
                    case "-s":
                        enableFullStats = false;
                        break;
                    case "-f":
                        enableFullStats = true;
                        break;
                    default:
                        File inputFile = new File(arg);
                        if (inputFile.isFile()) {
                            inputFiles.add(inputFile);
                        } else {
                            System.err.println("Не удалось найти файл c исходными данными " + inputFile.getAbsolutePath());
                        }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Опция " + args[args.length - 1] + " проигнорирована, т. к. после неё отсутствует значение");
            }
        }

        FileFilter fileFilter = new FileFilter(
                outputDirectory,
                outputFilenamePrefix,
                enableAppend,
                enableFullStats,
                inputFiles.toArray(new File[0]));
        fileFilter.work();
    }
}