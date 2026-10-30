package com.abinash.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class ServerlessTask implements Serializable {
    private String executionId;
    private String functionName;
    private String runtime;
    private String status;
    private long durationMs;
    private String payload;
    private String result;
    private String timestamp;

    public ServerlessTask() {
        this.executionId = "fn-" + UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = Instant.now().toString();
    }

    public ServerlessTask(String functionName, String runtime, String payload, String result, long durationMs) {
        this();
        this.functionName = functionName;
        this.runtime = runtime;
        this.payload = payload;
        this.result = result;
        this.durationMs = durationMs;
        this.status = "SUCCESS";
    }

    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getRuntime() { return runtime; }
    public void setRuntime(String runtime) { this.runtime = runtime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
