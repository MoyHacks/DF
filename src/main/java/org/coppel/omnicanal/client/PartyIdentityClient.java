package org.coppel.omnicanal.client;

import org.coppel.omnicanal.dto.partyidentity.TokenResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class PartyIdentityClient {
    private final WebClient webClient;
    private String tokenApiUrl;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String scope;

    public PartyIdentityClient(WebClient webClient,String tokenApiUrl,String clientId,String clientSecret,String grantType,String scope){
        this.webClient = webClient;
        this.tokenApiUrl = tokenApiUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.scope = scope;
    }

    public TokenResponse getNewToken() {


        String requestBody = String.format("grant_type=%s&client_id=%s&client_secret=%s&scope=%s",
                this.grantType,this.clientId, this.clientSecret,this.scope);

        return this.webClient.post().uri(this.tokenApiUrl)
                .headers(httpHeaders -> {
                    httpHeaders.add("X-COPPEL-DATE-REQUEST", getTimeStampHeader());
                    httpHeaders.add("X-COPPEL-LATITUDE", "24.71093149082847");
                    httpHeaders.add("X-COPPEL-LONGITUDE", "-107.38788217024636");
                    httpHeaders.add("X-COPPEL-TRANSACTIONID", "ts-0002");
                })
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();
    }
    public static String getTimeStampHeader() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Asegúrate de que sea UTC
        return dateFormat.format(date);
    }
}
