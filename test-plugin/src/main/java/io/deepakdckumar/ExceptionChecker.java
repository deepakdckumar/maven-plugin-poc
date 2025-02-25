package io.deepakdckumar;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

@Mojo(name = "check", defaultPhase = LifecyclePhase.COMPILE)
public class ExceptionChecker extends AbstractMojo {
    private static List<String> validMessages = Arrays.asList(
            "Invalid Input",
            "File Not Found",
            "Null Pointer Exception",
            "Database Error"
    );

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("Inside the execute method of the ExceptionChecker plugin");
        Path projectDir = Paths.get("src/main/java");

        List<String> invalidMessages = new ArrayList<>();  // To collect invalid exception messages

        try {
            Files.walk(projectDir)
                    .filter(path -> path.toString().endsWith(".java")) // Only process Java files
                    .forEach(filePath -> checkForExceptionsInFile(filePath, invalidMessages));

            // If any invalid message found, fail the build
            if (!invalidMessages.isEmpty()) {
                String errorMessage = "Build failed due to invalid exception messages found:\n";
                errorMessage += String.join("\n", invalidMessages);
                throw new MojoFailureException(errorMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkForExceptionsInFile(Path filePath, List<String> invalidMessages) {
        System.out.println("Checking file: " + filePath);
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("throw new Exception")) {
                    System.out.println(line);
                    // Use a regular expression to extract the message inside the exception
                    Pattern pattern = Pattern.compile("throw new Exception\\(\"([^\"]+)\"\\);");
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        String exceptionMessage = matcher.group(1).trim();
                        System.out.println(exceptionMessage);
                        if (validMessages.contains(exceptionMessage)) {
                            System.out.println("Valid exception message found in file " + filePath);
                            System.out.println("Exception message: " + exceptionMessage);
                        } else {
                            // Collect invalid exception messages
                            System.out.println("Invalid exception message found in file " + filePath);
                            System.out.println("Exception message: " + exceptionMessage);
                            invalidMessages.add("File: " + filePath + " - Invalid message: " + exceptionMessage);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
