package com.abinash.controller;

import com.abinash.model.CustomApi;
import com.abinash.model.MicroserviceNode;
import com.abinash.model.ServerlessTask;
import com.abinash.service.CliExecutionService;
import com.abinash.service.CloudClusterService;
import com.abinash.service.DynamicApiService;
import com.abinash.service.LogCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final CloudClusterService clusterService;
    private final DynamicApiService dynamicApiService;
    private final CliExecutionService cliExecutionService;
    private final LogCaptureService logCaptureService;

    @Autowired
    public ApiController(CloudClusterService clusterService,
                         DynamicApiService dynamicApiService,
                         CliExecutionService cliExecutionService,
                         LogCaptureService logCaptureService) {
        this.clusterService = clusterService;
        this.dynamicApiService = dynamicApiService;
        this.cliExecutionService = cliExecutionService;
        this.logCaptureService = logCaptureService;
    }

    @GetMapping
    public Map<String, Object> rootApi() {
        return clusterService.getClusterOverview();
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return clusterService.getClusterOverview();
    }

    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("system", "Abinash Cloud Platform");
        health.put("clusterState", "HEALTHY");
        health.put("nodesOnline", clusterService.getServices().size());
        health.put("dynamicApisCount", dynamicApiService.listApis().size());
        return health;
    }

    @GetMapping("/services")
    public Collection<MicroserviceNode> getServices() {
        return clusterService.getServices();
    }

    @GetMapping("/telemetry")
    public Map<String, Object> getTelemetry() {
        return clusterService.getTelemetry();
    }

    @PostMapping("/serverless/run")
    public ServerlessTask runFunction(
            @RequestParam(defaultValue = "demo-function") String name,
            @RequestBody(required = false) String payload) {
        ServerlessTask task = clusterService.runServerlessFunction(name, payload);
        logCaptureService.appendLog(String.format("INFO --- [abinash] com.abinash.serverless : Dispatched function [%s] -> executionId=%s, duration=%dms",
                name, task.getExecutionId(), task.getDurationMs()));
        return task;
    }

    @GetMapping("/serverless/history")
    public List<ServerlessTask> getServerlessHistory() {
        return clusterService.getServerlessHistory();
    }

    @GetMapping("/greet")
    public Map<String, String> greet(@RequestParam(defaultValue = "Abinash") String name) {
        Map<String, String> response = new HashMap<>();
        response.put("greeting", "Hello, " + name + "!");
        response.put("poweredBy", "Abinash Engine");
        return response;
    }

    // Dynamic API Management
    @GetMapping("/dynamic/list")
    public Collection<CustomApi> listCustomApis() {
        return dynamicApiService.listApis();
    }

    @PostMapping("/dynamic/create")
    public CustomApi createCustomApi(@RequestBody CustomApi api) {
        CustomApi created = dynamicApiService.createApi(api);
        logCaptureService.appendLog(String.format("INFO --- [abinash] com.abinash.dynamic : Deployed dynamic route [%s %s] (ID: %s, Status: %d)",
                created.getMethod(), created.getPath(), created.getId(), created.getStatusCode()));
        return created;
    }

    @DeleteMapping("/dynamic/delete/{id}")
    public Map<String, Object> deleteCustomApi(@PathVariable String id) {
        boolean deleted = dynamicApiService.deleteApi(id);
        if (deleted) {
            logCaptureService.appendLog(String.format("INFO --- [abinash] com.abinash.dynamic : Removed dynamic route ID: %s", id));
        }
        Map<String, Object> res = new HashMap<>();
        res.put("success", deleted);
        res.put("id", id);
        return res;
    }

    @PostMapping("/dynamic/test/{id}")
    public Map<String, Object> testCustomApi(@PathVariable String id) {
        Map<String, Object> result = dynamicApiService.testApi(id);
        logCaptureService.appendLog(String.format("INFO --- [abinash] com.abinash.dynamic : Tested dynamic route [%s %s] -> %sms",
                result.get("method"), result.get("path"), result.get("durationMs")));
        return result;
    }

    // Interactive CLI Bridge for GUI Screen
    @PostMapping("/cli/execute")
    public Map<String, Object> executeCliCommand(@RequestBody Map<String, String> payload) {
        String cmd = payload != null ? payload.getOrDefault("command", "help") : "help";
        logCaptureService.appendLog("INFO --- [abinash] com.abinash.cli.session : abinash> " + cmd);
        String output = cliExecutionService.executeCommand(cmd);
        Map<String, Object> response = new HashMap<>();
        response.put("command", cmd);
        response.put("output", output);
        return response;
    }

    // Full System Log Stream Endpoint
    @GetMapping("/logs")
    public Map<String, Object> getSystemLogs(@RequestParam(defaultValue = "0") int from) {
        Map<String, Object> res = new HashMap<>();
        res.put("logs", logCaptureService.getLogs(from));
        res.put("total", logCaptureService.getAllLogs().size());
        return res;
    }
}
