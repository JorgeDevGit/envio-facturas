package com.miempresa.facturas;

import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class VerifactuClient {
    private final RestTemplate restTemplate;
    private final String urlService;
    private final String token;

    public VerifactuClient( final String urlService, final String token) {
        final SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout( 5_000 );  // 5 s para conectar
        rf.setReadTimeout( 30_000 );    // 30 s para leer la respuesta

        this.restTemplate = new RestTemplate( rf );
        this.urlService = urlService;
        this.token = token;
    }

    public String callRestService( final String json ) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.setBearerAuth( token) ;
        final HttpEntity< String > request = new HttpEntity<>( json, headers );

        final ResponseEntity< String > response = restTemplate.exchange(
                urlService,
                HttpMethod.POST,
                request,
                String.class );

        if ( response.getStatusCode() != HttpStatus.OK || response.getBody() == null ) {
            throw new RuntimeException( "HTTP " + response.getStatusCode().value() );
        }
        return response.getBody();
    }
}
