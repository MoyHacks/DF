package org.coppel.omnicanal.dto.partyidentity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Getter
@Setter
@DefaultCoder(AvroCoder.class)
public class TokenResponse implements Serializable {
    @JsonProperty("token_type")
    private String type;
    @JsonProperty("expires_in")
    private int expiresIn;
    @JsonProperty("ext_expires_in")
    private int extExpiresIn;
    @JsonProperty("access_token")
    private String accessT;


    public String toJson(){return new Gson().toJson(this);}

}
