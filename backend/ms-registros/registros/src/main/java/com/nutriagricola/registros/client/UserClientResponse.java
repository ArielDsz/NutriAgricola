package com.nutriagricola.registros.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserClientResponse(String id, String role, Boolean active,
                                 String firstName, String lastName) { }
