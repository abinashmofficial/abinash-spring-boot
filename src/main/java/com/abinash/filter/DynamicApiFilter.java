package com.abinash.filter;

import com.abinash.model.CustomApi;
import com.abinash.service.DynamicApiService;
import com.abinash.service.LogCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Order(1)
public class DynamicApiFilter extends OncePerRequestFilter {

    private final DynamicApiService dynamicApiService;
    private final LogCaptureService logCaptureService;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Autowired
    public DynamicApiFilter(DynamicApiService dynamicApiService, LogCaptureService logCaptureService) {
        this.dynamicApiService = dynamicApiService;
        this.logCaptureService = logCaptureService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Handle CORS pre-flight if needed
        if ("OPTIONS".equalsIgnoreCase(method)) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Match against dynamic custom APIs
        CustomApi api = dynamicApiService.matchApi(method, uri);
        if (api != null) {
            long start = System.currentTimeMillis();
            long latency = Math.max(1, System.currentTimeMillis() - start);
            api.recordInvocation(latency);

            response.setStatus(api.getStatusCode());
            response.setContentType(api.getContentType() + ";charset=UTF-8");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("X-Powered-By", "Abinash-Dynamic-Engine");
            response.setHeader("X-Custom-API-ID", api.getId());

            byte[] bytes = api.getResponseBody().getBytes(StandardCharsets.UTF_8);
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();

            String time = LocalDateTime.now().format(dtf);
            logCaptureService.appendLog(String.format("%s  INFO 105401 --- [XNIO-1 task-2] com.abinash.dispatch : %s %s -> %d (%dms)",
                    time, method, uri, api.getStatusCode(), latency));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
