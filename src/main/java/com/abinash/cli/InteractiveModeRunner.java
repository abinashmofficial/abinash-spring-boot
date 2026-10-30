package com.abinash.cli;

import com.abinash.service.CliExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class InteractiveModeRunner implements ApplicationRunner {

    private final CliExecutionService cliExecutionService;
    private volatile String currentMode = "gui";

    @Autowired
    public InteractiveModeRunner(CliExecutionService cliExecutionService) {
        this.cliExecutionService = cliExecutionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (args.containsOption("mode")) {
            currentMode = args.getOptionValues("mode").get(0).toLowerCase();
        } else if (args.getNonOptionArgs().contains("cli") || (args.getSourceArgs() != null && String.join(" ", args.getSourceArgs()).contains("--cli"))) {
            currentMode = "cli";
        }

        // Always run the interactive stdin session in a dedicated thread
        Thread cliThread = new Thread(this::runTerminalSession, "abinash-cli");
        cliThread.setDaemon(true);
        cliThread.start();
    }

    private void runTerminalSession() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("\n\033[1;33m╔════════════════════════════════════════════════════════════════╗\033[0m");
            System.out.println("\033[1;33m║          ABINASH ENGINE: SELECT OPERATION MODE                 ║\033[0m");
            System.out.println("\033[1;33m╚════════════════════════════════════════════════════════════════╝\033[0m");
            System.out.println("  \033[1;36m[1] GUI Studio\033[0m   - Web Dashboard (Browser at http://localhost:8080)");
            System.out.println("  \033[1;35m[2] CLI Console\033[0m  - Interactive Terminal Shell");
            System.out.println("\n\033[1;32m  ✔ GUI MODE ACTIVE\033[0m   : Access the dashboard at \033[1;32mhttp://localhost:8080/\033[0m");
            System.out.println("\033[1;36m  ✔ TERMINAL CLI READY\033[0m : Type commands below anytime (e.g. 'help', 'apis', 'cli', 'exit')\n");

            while (true) {
                System.out.print("\033[1;36mabinash>\033[0m ");
                System.out.flush();

                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if ("exit".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                    System.out.println("\033[1;33mShutting down Abinash Platform...\033[0m");
                    System.exit(0);
                    return;
                }

                if ("cli".equalsIgnoreCase(line) || "2".equals(line)) {
                    currentMode = "cli";
                    System.out.println("\033[1;32m[✓] Dedicated CLI Terminal Mode Active.\033[0m Type 'help' for commands.");
                    continue;
                }

                if ("gui".equalsIgnoreCase(line) || "1".equals(line)) {
                    currentMode = "gui";
                    System.out.println("\033[1;36m[i] GUI Studio Active:\033[0m http://localhost:8080/");
                    System.out.println("    Open in your browser to create APIs, monitor microservices, and inspect telemetry.");
                    continue;
                }

                String output = cliExecutionService.executeCommand(line);
                System.out.println(output);
                System.out.println();
            }
        } catch (Exception e) {
            // Non-interactive or daemon mode
        }
    }
}
