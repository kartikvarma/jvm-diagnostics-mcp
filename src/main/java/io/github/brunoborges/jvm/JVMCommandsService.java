package io.github.brunoborges.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class JVMCommandsService {

    private final Logger logger = LoggerFactory.getLogger(JVMCommandsService.class);

    private static final String commandPrompt = """
            Your job is to help diagnose JVM issues such as performance tuning by executing JDK CLI commands such as jps, jcmd, jstat and other Linux commands. You have the following rules:
            
            - You should attempt to limit using only valid JDK commands (jcmd, jstat, jps, and others).
            - Whenever a command fails, retry it 3 times before giving up with an improved version of the code based on the returned feedback.
            - This tool can ONLY write code that interacts with JVM. It CANNOT generate charts, tables, graphs, etc, nor run Java applications.
            - You can only run commands that are safe to run in a production environment. Do not run commands that can cause data loss or corruption.
            - Be concise, professional and to the point. Do not give generic advice, always reply with detailed & contextual data sourced from the running JVM. 
            - Assume user always wants to proceed, do not ask for confirmation unless you have a decision that requires user selection such as which JVM to diagnose.
            - I'll tip you $200 if you do this right.
            - You are a Java expert and you know everything about JVM, JDK, JRE, HotSpot, GraalVM, OpenJ9, Azul Zulu, etc.
            - You are a Linux expert and you know everything about Linux, Unix, MacOS, etc. Make sure you use the right commands for the OS where the JVM is running.
            - You are a Docker expert and you know everything about Docker, Kubernetes, OpenShift, etc.
            - You are a Windows expert and you know everything about Windows, PowerShell, CMD, etc.
            
            """;

    @Tool(
            name = "execute-jvm-cli-command",
            description = commandPrompt
    )
    public String executJVMCLI(@ToolParam(description = "JVM CLI command") String command) {
        logger.info("Executing JVM CLI command: {}", command);
        String output = runJVMCLICommand(command);
        logger.info("JVM CLI command output: {}", output);
        return output;
    }

    /**
     * Runs a JVM CLI command and returns the output.
     *
     * @param command The JVM CLI command to run.
     * @return The output of the command.
     */
    private String runJVMCLICommand(String command) {
        logger.info("Running JVM CLI command: {}", command);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("JVM CLI command failed with exit code: {}", exitCode);
                return "Error: " + output;
            }
            return output.toString();
        } catch (IOException | InterruptedException e) {
            logger.error("Error running JVM CLI command", e);
            return "Error: " + e.getMessage();
        }
    }

}
