package com.abinash.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LogCaptureService {

    private final List<String> logEntries = new CopyOnWriteArrayList<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final String pid = "164644";

    @PostConstruct
    public void init() {
        // 1. Raw Cyber Slant ASCII Banner
        logEntries.add("    ___    ____  ____ _   _____    _____ __  __");
        logEntries.add("   /   |  / __ )/  _// | / /   |  / ___// / / /");
        logEntries.add("  / /| | / __  |/ / /  |/ / /| |  \\__ \\/ /_/ / ");
        logEntries.add(" / ___ |/ /_/ // / / /|  / ___ | ___/ / __  /  ");
        logEntries.add("/_/  |_/_____/___//_/ |_/_/  |_|/____/_/ /_/   ");
        logEntries.add("──────────────────────────────────────────────────────────────");
        logEntries.add("◈  PLATFORM :  Abinash Engine v1.0.0  [READY]");
        logEntries.add("◈  RUNTIME  :  Java v17.0.20.1  (Universal: 8, 11, 17, 21+)");
        logEntries.add("◈  GATEWAY  :  http://localhost:8080/");
        logEntries.add("──────────────────────────────────────────────────────────────");

        // 2. Exact CLI System Initialization Logs
        logEntries.add("2026-10-27 16:10:10.027  INFO 164644 --- [abinash] com.abinash.AbinashApplication : Starting AbinashApplication using Java 17.0.20.1 on proflujo with PID 164644 (/var/www/personal/mp/java/abinash/target/classes started by vision in /var/www/personal/mp/java/abinash)");
        logEntries.add("2026-10-27 16:10:10.029  INFO 164644 --- [abinash] com.abinash.AbinashApplication : No active profile set, falling back to 1 default profile: \"default\"");
        logEntries.add("2026-10-27 16:10:10.463  INFO 164644 --- [abinash] io.undertow.servlet : Initializing Spring embedded WebApplicationContext");
        logEntries.add("2026-10-27 16:10:10.464  INFO 164644 --- [abinash] org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 407 ms");
        logEntries.add("2026-10-27 16:10:10.592  INFO 164644 --- [abinash] org.springframework.boot.autoconfigure.web.servlet.WelcomePageHandlerMapping : Adding welcome page: class path resource [static/index.html]");
        logEntries.add("2026-10-27 16:10:10.658  INFO 164644 --- [abinash] io.undertow : starting server: Undertow - 2.2.28.Final");
        logEntries.add("2026-10-27 16:10:10.662  INFO 164644 --- [abinash] org.xnio : XNIO version 3.8.7.Final");
        logEntries.add("2026-10-27 16:10:10.666  INFO 164644 --- [abinash] org.xnio.nio : XNIO NIO Implementation Version 3.8.7.Final");
        logEntries.add("2026-10-27 16:10:10.711  INFO 164644 --- [abinash] org.jboss.threads : JBoss Threads version 3.1.0.Final");
        logEntries.add("2026-10-27 16:10:10.734  INFO 164644 --- [abinash] org.springframework.boot.web.embedded.undertow.UndertowWebServer : Undertow started on port(s) 8080 (http)");
        logEntries.add("2026-10-27 16:10:10.740  INFO 164644 --- [abinash] com.abinash.AbinashApplication : Started AbinashApplication in 0.913 seconds (JVM running for 1.062)");

        // 3. Operation Mode Select Box
        logEntries.add("╔════════════════════════════════════════════════════════════════╗");
        logEntries.add("║          ABINASH ENGINE: SELECT OPERATION MODE                 ║");
        logEntries.add("╚════════════════════════════════════════════════════════════════╝");
        logEntries.add("[1] GUI Mode  - Web Dashboard (Browser at http://localhost:8080)");
        logEntries.add("[2] CLI Mode  - Interactive Terminal Shell");
        logEntries.add("Select mode [1/2] (Press Enter for GUI): (Defaulting to GUI Mode)");
        logEntries.add("✔ GUI MODE ACTIVE: Access the dashboard at http://localhost:8080/");
        logEntries.add("(Tip: To switch to CLI mode anytime, restart with --mode=cli or use CLI Console)");
        logEntries.add("2026-10-27 16:10:11.042  INFO 164644 --- [XNIO-1 task-1] io.undertow.servlet : Initializing Spring DispatcherServlet 'dispatcherServlet'");
        logEntries.add("2026-10-27 16:10:11.043  INFO 164644 --- [XNIO-1 task-1] org.springframework.web.servlet.DispatcherServlet : Completed initialization in 1 ms");

        // 4. Hook into Logback for live event stream
        try {
            Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            AppenderBase<ILoggingEvent> appender = new AppenderBase<ILoggingEvent>() {
                @Override
                protected void append(ILoggingEvent event) {
                    String time = LocalDateTime.now().format(dtf);
                    String threadName = event.getThreadName().equals("main") ? "abinash" : event.getThreadName();
                    String line = String.format("%s  %-5s %s --- [%s] %s : %s",
                            time, event.getLevel(), pid, threadName, event.getLoggerName(), event.getFormattedMessage());
                    appendLog(line);
                }
            };
            appender.start();
            rootLogger.addAppender(appender);
        } catch (Exception ignored) {
        }
    }

    public void appendLog(String log) {
        if (log != null && !log.trim().isEmpty()) {
            logEntries.add(log);
            if (logEntries.size() > 1000) {
                logEntries.remove(0);
            }
        }
    }

    public List<String> getLogs(int fromIndex) {
        if (fromIndex < 0 || fromIndex >= logEntries.size()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(logEntries.subList(fromIndex, logEntries.size()));
    }

    public List<String> getAllLogs() {
        return new ArrayList<>(logEntries);
    }
}
