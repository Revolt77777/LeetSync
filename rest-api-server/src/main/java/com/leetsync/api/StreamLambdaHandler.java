package com.leetsync.api;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {

    private static final SpringBootLambdaContainerHandler<?, ?> handler;

    static {
        try {
            // Use getHttpApiV2ProxyHandler for HTTP API v2 payload format
            handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(RestApiServerApplication.class);

            // Performance optimizations
            handler.stripBasePath("/");

            // Optional: Enable for debugging only
            // handler.enableLogging(true);

            // Optional: Set custom timeout for initialization
            // handler.setInitializationTimeout(30000); // 30 seconds

        } catch (ContainerInitializationException e) {
            System.err.println("Failed to initialize Spring Boot handler: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Spring Boot", e);
        }
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        // Add request logging only if needed (impacts performance)
        if (context != null) {
            System.out.println("Processing request: " + context.getAwsRequestId());
        }

        handler.proxyStream(input, output, context);
    }
}