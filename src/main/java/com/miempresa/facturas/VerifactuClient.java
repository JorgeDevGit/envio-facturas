package com.miempresa.facturas;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

public class VerifactuClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String token;

    public VerifactuClient( final String baseUrl, final String token) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.token = token;
    }

    public String sendInvoice( final String json ) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.setBearerAuth( token) ;
        final HttpEntity< String > request = new HttpEntity<>( json, headers );
        final ResponseEntity< String > response = restTemplate.exchange(
            baseUrl + "/verifactu/create",
            HttpMethod.POST,
            request,
                String.class
        );
        if ( response.getStatusCode() != HttpStatus.OK || response.getBody() == null ) {
            throw new RuntimeException( "HTTP " + response.getStatusCode().value() );
        }
        return response.getBody();
    }
}
