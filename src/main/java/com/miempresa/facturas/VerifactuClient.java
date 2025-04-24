package com.miempresa.facturas;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class VerifactuClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public VerifactuClient(String baseUrl, String token) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public ResponseDto sendInvoice(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        ResponseEntity<ResponseDto> response = restTemplate.exchange(
            baseUrl + "/verifactu/create",
            HttpMethod.POST,
            request,
            ResponseDto.class
        );
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("HTTP " + response.getStatusCodeValue());
        }
        return response.getBody();
    }
}
