package com.purohitcommunity.portal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.net.URI;
import java.util.Map;

/** Adapter contract for a trusted SMS gateway; never returns or logs the OTP. */
@Service
public class GatewayOtpSmsSender implements OtpSmsSender {
    private final String url;
    private final String token;
    private final RestClient client;

    public GatewayOtpSmsSender(@Value("${app.sms.gateway-url:}") String url,
                               @Value("${app.sms.gateway-token:}") String token){
        this.url=url;
        this.token=token;
        var factory=new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        client=RestClient.builder().requestFactory(factory).build();
    }

    @Override public void requireAvailable(){
        if(url.isBlank() || token.isBlank() || !"https".equalsIgnoreCase(URI.create(url).getScheme()))
            throw new IllegalStateException("SMS password recovery is not available yet. Please contact the administrator.");
    }

    @Override public void send(String registeredPhone,String otp){
        requireAvailable();
        try{
            client.post().uri(url).header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                .body(Map.of("phoneNumber",registeredPhone,"otp",otp,"purpose","PASSWORD_RESET","expiresInSeconds",300))
                .retrieve().toBodilessEntity();
        }catch(Exception ex){
            throw new IllegalStateException("OTP delivery failed. Please try again later.");
        }
    }
}
