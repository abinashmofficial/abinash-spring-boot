package com.abinash.model;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

public class CustomApi {

    private String id;
    private String name;
    private String method; // GET, POST, PUT, DELETE, PATCH
    private String path;   // e.g. /api/v1/users
    private int statusCode; // e.g. 200, 201
    private String contentType; // application/json, text/plain
    private String responseBody;
    private String createdBy; // GUI or CLI
    private String createdAt;
    private AtomicLong callCount;
    private long lastDurationMs;
    private String lastCalledAt;

    public CustomApi() {
        this.callCount = new AtomicLong(0);
        this.createdAt = Instant.now().toString();
        this.lastCalledAt = "-";
        this.statusCode = 200;
        this.contentType = "application/json";
        this.method = "GET";
        this.path = "/api/custom";
        this.responseBody = "{\"status\":\"ok\"}";
        this.createdBy = "GUI";
    }

    public CustomApi(String id, String name, String method, String path, int statusCode,
                     String contentType, String responseBody, String createdBy) {
        this.id = id;
        this.name = name;
        this.method = method != null ? method.toUpperCase() : "GET";
        this.path = (path != null && path.startsWith("/")) ? path : "/" + (path == null ? "" : path);
        this.statusCode = statusCode > 0 ? statusCode : 200;
        this.contentType = contentType != null ? contentType : "application/json";
        this.responseBody = responseBody != null ? responseBody : "{\"status\":\"ok\"}";
        this.createdBy = createdBy != null ? createdBy : "GUI";
        this.createdAt = Instant.now().toString();
        this.callCount = new AtomicLong(0);
        this.lastDurationMs = 0;
        this.lastCalledAt = "-";
    }

    public void recordInvocation(long durationMs) {
        this.callCount.incrementAndGet();
        this.lastDurationMs = durationMs;
        this.lastCalledAt = Instant.now().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method != null ? method.toUpperCase() : "GET";
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = (path != null && path.startsWith("/")) ? path : "/" + (path == null ? "" : path);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public long getCallCount() {
        return callCount != null ? callCount.get() : 0;
    }

    public void setCallCount(long count) {
        this.callCount = new AtomicLong(count);
    }

    public long getLastDurationMs() {
        return lastDurationMs;
    }

    public void setLastDurationMs(long lastDurationMs) {
        this.lastDurationMs = lastDurationMs;
    }

    public String getLastCalledAt() {
        return lastCalledAt;
    }

    public void setLastCalledAt(String lastCalledAt) {
        this.lastCalledAt = lastCalledAt;
    }
}
