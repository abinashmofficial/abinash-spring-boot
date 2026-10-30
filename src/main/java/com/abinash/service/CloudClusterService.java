package com.abinash.service;

import com.abinash.model.MicroserviceNode;
import com.abinash.model.ServerlessTask;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CloudClusterService {

    private final Map<String, MicroserviceNode> microservices = new ConcurrentHashMap<>();
    private final List<ServerlessTask> serverlessHistory = new CopyOnWriteArrayList<>();
    private final AtomicLong totalRequests = new AtomicLong(1042);
    private final long startTime = System.currentTimeMillis();

    @PostConstruct
    public void initCluster() {
        registerService(new MicroserviceNode("gw-01", "API-Gateway", "v2.4", "GATEWAY", "HEALTHY", 12, 14.5, "http://gateway.abinash.cloud:80"));
        registerService(new MicroserviceNode("auth-01", "Identity-Auth-Service", "v1.8", "AUTH", "HEALTHY", 18, 22.0, "http://auth.abinash.cloud:8081"));
        registerService(new MicroserviceNode("order-01", "Enterprise-Order-Core", "v3.1", "ENTERPRISE", "HEALTHY", 24, 31.2, "http://order.abinash.cloud:8082"));
        registerService(new MicroserviceNode("worker-01", "Serverless-Worker-Pool", "v1.0", "SERVERLESS", "HEALTHY", 8, 8.4, "http://lambda.abinash.cloud:9000"));
        registerService(new MicroserviceNode("telemetry-01", "Observability-Mesh", "v2.0", "TELEMETRY", "HEALTHY", 15, 12.1, "http://metrics.abinash.cloud:9090"));

        // Seed initial serverless tasks
        serverlessHistory.add(new ServerlessTask("image-optimizer", "jvm-native", "{\"size\":\"4.2MB\",\"format\":\"webp\"}", "{\"optimizedSize\":\"680KB\",\"ratio\":\"83%\"}", 34));
        serverlessHistory.add(new ServerlessTask("token-authorizer", "jvm-native", "{\"jwt\":\"ey...\",\"claims\":\"admin\"}", "{\"valid\":true,\"roles\":[\"ADMIN\",\"USER\"]}", 12));
    }

    public void registerService(MicroserviceNode node) {
        microservices.put(node.getId(), node);
    }

    public Collection<MicroserviceNode> getServices() {
        return microservices.values();
    }

    public Map<String, Object> getClusterOverview() {
        totalRequests.incrementAndGet();
        Map<String, Object> overview = new HashMap<>();
        overview.put("platform", "Abinash Cloud Platform");
        overview.put("architecture", "Cloud-Native Enterprise Microservices");
        overview.put("clusterStatus", "ONLINE_OPTIMAL");
        overview.put("activeNodes", microservices.size());
        overview.put("totalRequestsHandled", totalRequests.get());
        overview.put("uptimeSeconds", (System.currentTimeMillis() - startTime) / 1000);
        overview.put("timestamp", Instant.now().toString());
        return overview;
    }

    public Map<String, Object> getTelemetry() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax() / (1024 * 1024);

        Map<String, Object> telemetry = new HashMap<>();
        telemetry.put("usedHeapMb", usedHeap);
        telemetry.put("maxHeapMb", maxHeap);
        telemetry.put("threadCount", threadBean.getThreadCount());
        telemetry.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        telemetry.put("uptimeMs", System.currentTimeMillis() - startTime);
        telemetry.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        telemetry.put("totalRequests", totalRequests.get());
        return telemetry;
    }

    public ServerlessTask runServerlessFunction(String functionName, String payload) {
        totalRequests.incrementAndGet();
        long start = System.currentTimeMillis();
        
        // Process payload & simulate execution
        String result;
        if (payload == null || payload.trim().isEmpty()) {
            payload = "{\"event\":\"default-trigger\"}";
        }
        
        if ("order-validator".equalsIgnoreCase(functionName)) {
            result = "{\"status\":\"VALIDATED\",\"orderId\":\"ORD-" + System.currentTimeMillis() % 100000 + "\",\"verified\":true}";
        } else if ("auth-token".equalsIgnoreCase(functionName)) {
            result = "{\"token\":\"abi_" + UUID.randomUUID().toString().replace("-", "") + "\",\"expiresIn\":3600}";
        } else {
            result = "{\"status\":\"EXECUTED\",\"message\":\"Function [" + functionName + "] completed successfully\",\"hash\":\"" + UUID.randomUUID().toString().substring(0, 8) + "\"}";
        }

        long duration = System.currentTimeMillis() - start + (long)(Math.random() * 25 + 5);
        ServerlessTask task = new ServerlessTask(functionName, "Abinash-Serverless-Engine", payload, result, duration);
        serverlessHistory.add(0, task);
        if (serverlessHistory.size() > 20) {
            serverlessHistory.remove(serverlessHistory.size() - 1);
        }
        return task;
    }

    public List<ServerlessTask> getServerlessHistory() {
        return serverlessHistory;
    }
}
