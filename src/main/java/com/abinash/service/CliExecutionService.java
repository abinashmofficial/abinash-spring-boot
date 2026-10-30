package com.abinash.service;

import com.abinash.model.CustomApi;
import com.abinash.model.MicroserviceNode;
import com.abinash.model.ServerlessTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class CliExecutionService {

    private final CloudClusterService clusterService;
    private final DynamicApiService dynamicApiService;

    @Autowired
    public CliExecutionService(CloudClusterService clusterService, DynamicApiService dynamicApiService) {
        this.clusterService = clusterService;
        this.dynamicApiService = dynamicApiService;
    }

    public String executeCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return "";
        }

        String trimmed = rawCommand.trim();
        String[] parts = trimmed.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help":
                return getHelpOutput();

            case "status":
                return getStatusOutput();

            case "nodes":
            case "services":
                return getServicesOutput();

            case "metrics":
            case "telemetry":
                return getTelemetryOutput();

            case "serverless":
            case "fn":
                String fnName = parts.length > 1 ? parts[1] : "cloud-worker";
                String payload = parts.length > 2 ? trimmed.substring(trimmed.indexOf(parts[2])) : "{\"cliTrigger\":true}";
                ServerlessTask task = clusterService.runServerlessFunction(fnName, payload);
                return String.format("[✓] Serverless Function Dispatched:\n    ID       : %s\n    Duration : %dms\n    Result   : %s",
                        task.getExecutionId(), task.getDurationMs(), task.getResult());

            case "apis":
                return listApisOutput();

            case "api":
                if (parts.length > 1) {
                    String sub = parts[1].toLowerCase();
                    if ("list".equals(sub)) {
                        return listApisOutput();
                    } else if ("create".equals(sub)) {
                        return handleCreateApi(parts, trimmed, 2);
                    } else if ("call".equals(sub) || "run".equals(sub)) {
                        return handleCallApi(parts, 2);
                    } else if ("delete".equals(sub)) {
                        return handleDeleteApi(parts, 2);
                    }
                }
                return "Usage: api [list | create <METHOD> <PATH> [JSON] | call <METHOD> <PATH> | delete <ID>]";

            case "create-api":
            case "api-create":
                return handleCreateApi(parts, trimmed, 1);

            case "call":
            case "call-api":
            case "api-call":
                return handleCallApi(parts, 1);

            case "delete-api":
            case "api-delete":
                return handleDeleteApi(parts, 1);

            case "test-api":
                if (parts.length > 1) {
                    Map<String, Object> res = dynamicApiService.testApi(parts[1]);
                    if (res.containsKey("error")) {
                        return "[!] Error: " + res.get("error");
                    }
                    return String.format("[✓] Invoked API [%s] %s in %sms (Status %s):\n%s",
                            res.get("method"), res.get("path"), res.get("durationMs"), res.get("statusCode"), res.get("responseBody"));
                }
                return "Usage: test-api <API_ID>";

            case "gui":
                return "[i] Web GUI Dashboard: http://localhost:8080/\n    Tip: Switch views or create APIs directly from the browser.";

            case "cli":
                return "[✓] CLI Engine Active.\n    Ready for commands: status, apis, create-api, call, serverless, metrics.";

            case "mode":
                if (parts.length > 1) {
                    String target = parts[1].toLowerCase();
                    if ("gui".equals(target)) {
                        return "[✓] GUI Mode is active at http://localhost:8080/";
                    } else if ("cli".equals(target)) {
                        return "[✓] CLI Mode is active in this terminal shell.";
                    }
                }
                return "[i] Supported Modes: 'mode gui' (Web Dashboard) | 'mode cli' (Interactive Terminal)";

            case "clear":
                return "\033[H\033[2J";

            default:
                return "[!] Unknown command: '" + cmd + "'. Type 'help' for the complete command list.";
        }
    }

    private String getHelpOutput() {
        return "=================== ABINASH COMMAND CONSOLE ===================\n" +
                "  API Management:\n" +
                "    apis                                 - List all active dynamic APIs\n" +
                "    create-api <METHOD> <PATH> [JSON]    - Create dynamic API (e.g. create-api GET /api/v1/ping {\"pong\":true})\n" +
                "    call <METHOD> <PATH>                 - Execute and test an API endpoint\n" +
                "    test-api <ID>                        - Test dynamic API by ID\n" +
                "    delete-api <ID>                      - Remove dynamic API by ID\n" +
                "\n" +
                "  Platform Operations:\n" +
                "    status                               - Cluster health and request metrics\n" +
                "    services / nodes                     - List all enterprise microservice nodes\n" +
                "    metrics / telemetry                  - JVM memory, CPU, and active threads\n" +
                "    serverless <name> [payload]          - Dispatch serverless cloud function\n" +
                "\n" +
                "  Mode Control:\n" +
                "    gui                                  - Display Web GUI Dashboard URL\n" +
                "    cli                                  - Confirm CLI terminal active state\n" +
                "    mode [gui|cli]                       - Switch operational mode\n" +
                "    clear                                - Clear screen output\n" +
                "    exit                                 - Shutdown application";
    }

    private String getStatusOutput() {
        Map<String, Object> o = clusterService.getClusterOverview();
        return String.format("[●] Platform   : %s\n    Architecture: %s\n    Cluster     : %s\n    Nodes Active: %s\n    Requests    : %s\n    Uptime      : %ss",
                o.get("platform"), o.get("architecture"), o.get("clusterStatus"), o.get("activeNodes"), o.get("totalRequestsHandled"), o.get("uptimeSeconds"));
    }

    private String getServicesOutput() {
        StringBuilder sb = new StringBuilder("[●] Registered Microservices:\n");
        for (MicroserviceNode node : clusterService.getServices()) {
            sb.append(String.format("    [%-9s] %-24s (v%-4s) Latency: %2dms | Endpoint: %s\n",
                    node.getStatus(), node.getName(), node.getVersion(), node.getLatencyMs(), node.getEndpoint()));
        }
        return sb.toString().trim();
    }

    private String getTelemetryOutput() {
        Map<String, Object> t = clusterService.getTelemetry();
        return String.format("[●] JVM Telemetry:\n    Heap Memory : %s MB used / %s MB max\n    Threads     : %s active (%s daemon)\n    Processors  : %s CPU cores",
                t.get("usedHeapMb"), t.get("maxHeapMb"), t.get("threadCount"), t.get("daemonThreadCount"), t.get("availableProcessors"));
    }

    private String listApisOutput() {
        Collection<CustomApi> list = dynamicApiService.listApis();
        if (list.isEmpty()) {
            return "[i] No dynamic APIs currently registered. Use 'create-api' or create one in the GUI.";
        }
        StringBuilder sb = new StringBuilder("[●] Active Dynamic APIs (" + list.size() + "):\n");
        for (CustomApi a : list) {
            sb.append(String.format("    [%-6s] %-26s Status: %-3d | Hits: %-3d | Source: %-6s | ID: %s\n",
                    a.getMethod(), a.getPath(), a.getStatusCode(), a.getCallCount(), a.getCreatedBy(), a.getId()));
        }
        return sb.toString().trim();
    }

    private String handleCreateApi(String[] parts, String raw, int offset) {
        if (parts.length < offset + 2) {
            return "Usage: create-api <METHOD> <PATH> [JSON_RESPONSE]\nExample: create-api GET /api/v1/hello {\"msg\":\"Hello World\"}";
        }
        String method = parts[offset].toUpperCase();
        String path = parts[offset + 1];
        String json = "{\"status\":\"ok\",\"source\":\"Abinash-CLI\"}";

        if (parts.length > offset + 2) {
            int idx = raw.indexOf(parts[offset + 2]);
            if (idx != -1) {
                json = raw.substring(idx).trim();
            }
        }

        CustomApi api = new CustomApi();
        api.setMethod(method);
        api.setPath(path);
        api.setName("CLI API: " + method + " " + path);
        api.setStatusCode("POST".equalsIgnoreCase(method) ? 201 : 200);
        api.setResponseBody(json);
        api.setCreatedBy("CLI");

        CustomApi created = dynamicApiService.createApi(api);
        return String.format("[✓] API Created Successfully!\n    ID       : %s\n    Endpoint : %s %s\n    Status   : %d\n    cURL     : curl -X %s http://localhost:8080%s",
                created.getId(), created.getMethod(), created.getPath(), created.getStatusCode(), created.getMethod(), created.getPath());
    }

    private String handleCallApi(String[] parts, int offset) {
        if (parts.length < offset + 2) {
            return "Usage: call <METHOD> <PATH>\nExample: call GET /api/v1/users";
        }
        String method = parts[offset].toUpperCase();
        String path = parts[offset + 1];

        CustomApi api = dynamicApiService.matchApi(method, path);
        if (api == null) {
            return "[!] No dynamic API matched " + method + " " + path + ". Use 'apis' to view registered routes.";
        }

        long start = System.currentTimeMillis();
        long latency = Math.max(1, System.currentTimeMillis() - start + 2);
        api.recordInvocation(latency);

        return String.format("[✓] 200 OK (%dms) -> [%s %s]\n%s",
                latency, api.getMethod(), api.getPath(), api.getResponseBody());
    }

    private String handleDeleteApi(String[] parts, int offset) {
        if (parts.length < offset + 1) {
            return "Usage: delete-api <API_ID>";
        }
        String id = parts[offset];
        boolean deleted = dynamicApiService.deleteApi(id);
        if (deleted) {
            return "[✓] Deleted API: " + id;
        } else {
            return "[!] API ID not found: " + id;
        }
    }
}
