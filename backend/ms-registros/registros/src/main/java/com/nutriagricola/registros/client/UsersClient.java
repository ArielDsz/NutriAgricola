package com.nutriagricola.registros.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "users-service",
        url = "${services.users.url:http://localhost:8081}",
        path = "/api/v1/users")
public interface UsersClient {
    @GetMapping("/{id}")
    UserClientResponse findById(@PathVariable("id") String id);
}
