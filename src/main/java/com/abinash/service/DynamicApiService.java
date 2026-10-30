package com.abinash.service;

import com.abinash.model.CustomApi;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DynamicApiService {

    private final Map<String, CustomApi> apiStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Seed default enterprise APIs created in Abinash Studio
        createApi(new CustomApi(
                "api-users-v1",
                "User Directory Service",
                "GET",
                "/api/v1/users",
                200,
                "application/json",
                "{\"users\":[{\"id\":101,\"name\":\"Abinash\",\"role\":\"Enterprise Architect\",\"status\":\"ACTIVE\"},{\"id\":102,\"name\":\"Elena Rostova\",\"role\":\"Mesh Lead\",\"status\":\"ACTIVE\"}],\"total\":2}",
                "STUDIO"
        ));

        createApi(new CustomApi(
                "api-orders-v1",
                "Enterprise Order Ingestion",
                "POST",
                "/api/v1/orders",
                201,
                "application/json",
                "{\"status\":\"ORDER_ACCEPTED\",\"orderId\":\"ORD-882194\",\"currency\":\"USD\",\"total\":429.99,\"gateway\":\"Abinash-Pay\"}",
                "STUDIO"
        ));

        createApi(new CustomApi(
                "api-inventory-v1",
                "Realtime Inventory Status",
                "GET",
                "/api/v1/inventory",
                200,
                "application/json",
                "{\"warehouse\":\"DC-North-1\",\"skuCount\":1420,\"healthy\":true}",
                "STUDIO"
        ));
    }

    public CustomApi createApi(CustomApi api) {
        if (api.getId() == null || api.getId().trim().isEmpty()) {
            api.setId("api-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (api.getName() == null || api.getName().trim().isEmpty()) {
            api.setName("API " + api.getMethod() + " " + api.getPath());
        }
        if (api.getCreatedBy() == null || api.getCreatedBy().trim().isEmpty()) {
            api.setCreatedBy("GUI");
        }
        if (!api.getPath().startsWith("/")) {
            api.setPath("/" + api.getPath());
        }
        api.setMethod(api.getMethod().toUpperCase());
        apiStore.put(api.getId(), api);
        return api;
    }

    public Collection<CustomApi> listApis() {
        return apiStore.values();
    }

    public CustomApi getApi(String id) {
        return apiStore.get(id);
    }

    public boolean deleteApi(String id) {
        return apiStore.remove(id) != null;
    }

    public CustomApi matchApi(String method, String requestUri) {
        if (requestUri == null || method == null) return null;
        String normUri = requestUri.endsWith("/") && requestUri.length() > 1
                ? requestUri.substring(0, requestUri.length() - 1)
                : requestUri;

        for (CustomApi api : apiStore.values()) {
            String apiPath = api.getPath().endsWith("/") && api.getPath().length() > 1
                    ? api.getPath().substring(0, api.getPath().length() - 1)
                    : api.getPath();
            if (api.getMethod().equalsIgnoreCase(method) && apiPath.equalsIgnoreCase(normUri)) {
                return api;
            }
        }
        return null;
    }

    public Map<String, Object> testApi(String id) {
        CustomApi api = apiStore.get(id);
        Map<String, Object> result = new HashMap<>();
        if (api == null) {
            result.put("error", "API not found: " + id);
            result.put("status", 404);
            return result;
        }

        long latency = (long) (Math.random() * 8 + 3);
        api.recordInvocation(latency);

        result.put("id", api.getId());
        result.put("name", api.getName());
        result.put("method", api.getMethod());
        result.put("path", api.getPath());
        result.put("statusCode", api.getStatusCode());
        result.put("contentType", api.getContentType());
        result.put("responseBody", api.getResponseBody());
        result.put("durationMs", latency);
        result.put("timestamp", Instant.now().toString());
        result.put("totalCalls", api.getCallCount());
        result.put("lastCalledAt", api.getLastCalledAt());
        return result;
    }
}
