package io.github.brunoborges.jvm;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JVMDiagnosticsMCPApp {

    public static void main(String[] args) {
        // say app is running
        System.out.println("JVMDiagnosticsMCPApp is running...");
        SpringApplication.run(JVMDiagnosticsMCPApp.class, args);
    }

    @Bean
    public ToolCallbackProvider mcpTool(JVMCommandsService azureCliService) {
        return MethodToolCallbackProvider.builder().toolObjects(azureCliService).build();
    }

}
