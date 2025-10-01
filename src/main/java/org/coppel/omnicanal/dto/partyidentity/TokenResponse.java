package org.coppel.omnicanal.dto.partyidentity;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String token_type;
    private int expires_in;
    private int ext_expires_in;
    private String access_token;


    public String toJson(){return new Gson().toJson(this);}

}
