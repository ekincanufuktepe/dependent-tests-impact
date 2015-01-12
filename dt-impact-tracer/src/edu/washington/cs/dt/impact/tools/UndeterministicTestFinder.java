package edu.washington.cs.dt.impact.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class UndeterministicTestFinder {

    public static void main(String[] args) {
        List<String> argsList = new ArrayList<String>(Arrays.asList(args));

        File undeterministicTestFile = null;
        int undeterministicFile = argsList.indexOf("-undeterministicTestFile");
        if (undeterministicFile != -1) {
            int undeterministicFileNameIndex = undeterministicFile + 1;
            if (undeterministicFileNameIndex >= argsList.size()) {
                System.err
                .println("Undeterministic file argument is specified but a file name is not."
                        + " Please use the format: -undeterministicTestFile aFileName");
                System.exit(0);
            }
            undeterministicTestFile = new File(argsList.get(undeterministicFileNameIndex));
        } else {
            System.err
            .println("No undeterministic file specified."
                    + " Please use the format: -undeterministicTestFile aFileName");
            System.exit(0);
        }

        File deterministicTestFile = null;
        int deterministicFile = argsList.indexOf("-deterministicTestFile");
        if (deterministicFile != -1) {
            int deterministicFileNameIndex = deterministicFile + 1;
            if (deterministicFileNameIndex >= argsList.size()) {
                System.err
                .println("Deterministic file argument is specified but a file name is not."
                        + " Please use the format: -deterministicTestFile aFileName");
                System.exit(0);
            }
            deterministicTestFile = new File(argsList.get(deterministicFileNameIndex));
        } else {
            System.err
            .println("No deterministic file specified."
                    + " Please use the format: -deterministicTestFile aFileName");
            System.exit(0);
        }

        File crossReferenceResultFile = null;
        int crossReferenceFile = argsList.indexOf("-crossReferenceFile");
        if (crossReferenceFile != -1) {
            int crossReferenceFileNameIndex = crossReferenceFile + 1;
            if (crossReferenceFileNameIndex >= argsList.size()) {
                System.err
                .println("Cross reference file argument is specified but a file name is not."
                        + " Please use the format: -crossReferenceFile aFileName");
                System.exit(0);
            }
            crossReferenceResultFile = new File(argsList.get(crossReferenceFileNameIndex));
        } else {
            System.err
            .println("No cross reference file specified."
                    + " Please use the format: -crossReferenceFile aFileName");
            System.exit(0);
        }

        boolean shouldRandomizeDeterministicTests = argsList.indexOf("-randomizeDeterministicTests") != -1;

        // parse the dependentTestFile and retrieve a dependent test to solve
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(crossReferenceResultFile));
            String line = br.readLine();
            while (line != null && !line.trim().matches(
                    "Original order result:        Test order result:")) {
                line = br.readLine();
            }

            if (line == null) {
                br.close();
                throw new RuntimeException("Incorrect format for dependent test file.");
            }

            String nextLine = br.readLine();
            if (nextLine == null || nextLine.equals("")) {
                System.out.println("There are no tests missing or undeterministic tests this round!");
                if (shouldRandomizeDeterministicTests) {
                    List<String> currentTests = parseFileToList(deterministicTestFile);
                    Collections.shuffle(currentTests);
                    printListToFile(currentTests, deterministicTestFile);
                }
            } else {
                int i = 0;
                List<String> currentTests = parseFileToList(deterministicTestFile);
                while(nextLine != null && !nextLine.equals("")) {
                    Scanner s = new Scanner(nextLine);

                    String testName = null;
                    String testResult = null;
                    if (s.hasNext()) {
                        testName = s.next();
                        testName = testName.substring(0, testName.length() - 1);
                    } else {
                        s.close();
                        System.out.println("Incorrect format for dependent test file.");
                        break;
                    }
                    if (s.hasNext()) {
                        testResult = s.next();
                    } else {
                        s.close();
                        System.out.println("Incorrect format for dependent test file.");
                        break;
                    }

                    if (!s.hasNext()) {
                        s.close();
                        System.out.println("The remaining tests are not dependent tests!");
                        break;
                    } else {
                        if (!currentTests.contains(testName)) {
                            s.close();
                            System.out.println("The deterministic tests file does not contain the undeterministic test!");
                            break;
                        }

                        i+= 1;
                        currentTests.remove(testName);

                        // Add the undeterministic test to a file for book-keeping
                        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(undeterministicTestFile, true)));
                        out.println(testName);
                        out.close();
                    }
                    s.close();

                    nextLine = br.readLine();
                }

                printListToFile(currentTests, deterministicTestFile);
                System.out.println("Total undeterministic tests found for this round: " + i);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // returns a list of Strings where each String is a line of the file
    private static List<String> parseFileToList(File orderFile) {
        List<String> tests = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(orderFile));
            String line = br.readLine();

            while (line != null) {
                if (!line.equals("")) {
                    tests.add(line);
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tests;
    }

    private static void printListToFile(List<String> listToPrint, File outputFile) {
        FileWriter output = null;
        BufferedWriter writer = null;
        try {
            output = new FileWriter(outputFile, false);
            writer = new BufferedWriter(output);

            for (int j = 0; j < listToPrint.size(); j++) {
                writer.write(listToPrint.get(j) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
            }
        }
    }

}
