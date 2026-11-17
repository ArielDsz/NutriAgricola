package com.nutriagricola.api_gateway;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingIntegrationTest {
    private static final DisposableServer BACKEND = HttpServer.create()
            .host("127.0.0.1")
            .port(0)
            .handle((request, response) -> response
                    .status(200)
                    .sendString(Mono.just(request.uri())))
            .bindNow();

    @LocalServerPort
    private int gatewayPort;

    @DynamicPropertySource
    static void backendAddresses(DynamicPropertyRegistry registry) {
        String backendUrl = "http://127.0.0.1:" + BACKEND.port();
        registry.add("USERS_SERVICE_URL", () -> backendUrl);
        registry.add("RECORDS_SERVICE_URL", () -> backendUrl);
    }

    @AfterAll
    static void stopBackend() {
        BACKEND.disposeNow();
    }

    @Test
    void forwardsUsersAndRecordsRequestsWithoutChangingTheirPaths() throws Exception {
        assertForwardedPath("/api/v1/users", "/api/v1/users");
        assertForwardedPath("/api/v1/records", "/api/v1/records");
    }

    private void assertForwardedPath(String gatewayPath, String expectedBackendPath) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:" + gatewayPort + gatewayPath))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(expectedBackendPath, response.body());
    }
}
