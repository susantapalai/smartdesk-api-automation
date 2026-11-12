
package com.smartdesk.tests;

import com.sun.net.httpserver.HttpServer;
import io.restassured.RestAssured;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class EmployeeApiTest {

    private HttpServer server;

    @BeforeClass
    public void startServer() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress("localhost", 0), 0);

        server.createContext("/employees", exchange -> {

            String requestBody = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8);

            boolean valid = requestBody.contains("\"name\":\"Rahul\"")
                    && requestBody.contains("\"job\":\"QA Engineer\"");

            int status = valid ? 201 : 400;

            String response = valid
                    ? "{\"name\":\"Rahul\",\"job\":\"QA Engineer\"}"
                    : "{\"error\":\"Invalid employee data\"}";

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json");

            exchange.sendResponseHeaders(status, bytes.length);

            try (var output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        });

        server.start();

        RestAssured.baseURI =
                "http://localhost:" + server.getAddress().getPort();
    }

    @Test
    public void createEmployeeSuccessfully() {

        String requestBody =
                "{\"name\":\"Rahul\",\"job\":\"QA Engineer\"}";

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/employees")
                .then()
                .statusCode(201)
                .body("name", equalTo("Rahul"))
                .body("job", equalTo("QA Engineer"));
    }

    @Test
    public void rejectInvalidEmployee() {

        String requestBody =
                "{\"name\":\"\",\"job\":\"\"}";

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/employees")
                .then()
                .statusCode(400)
                .body("error", equalTo("Invalid employee data"));
    }

    @AfterClass
    public void stopServer() {
        server.stop(0);
    }
}
