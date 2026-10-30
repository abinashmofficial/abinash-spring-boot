package com.abinash.model;

import java.io.Serializable;
import java.time.Instant;

public class MicroserviceNode implements Serializable {
    private String id;
    private String name;
    private String version;
    private String type; // GATEWAY, AUTH, ORDER, SERVERLESS_WORKER, TELEMETRY
    private String status; // HEALTHY, WARNING, DEGRADED
    private int latencyMs;
    private double cpuUsage;
    private String endpoint;
    private String lastHeartbeat;

    public MicroserviceNode() {}

    public MicroserviceNode(String id, String name, String version, String type, String status, int latencyMs, double cpuUsage, String endpoint) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.type = type;
        this.status = status;
        this.latencyMs = latencyMs;
        this.cpuUsage = cpuUsage;
        this.endpoint = endpoint;
        this.lastHeartbeat = Instant.now().toString();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getLatencyMs() { return latencyMs; }
    public void setLatencyMs(int latencyMs) { this.latencyMs = latencyMs; }

    public double getCpuUsage() { return cpuUsage; }
    public void setCpuUsage(double cpuUsage) { this.cpuUsage = cpuUsage; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(String lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
}
