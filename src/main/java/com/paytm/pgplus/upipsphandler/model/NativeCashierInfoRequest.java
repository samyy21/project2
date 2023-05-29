package com.paytm.pgplus.upipsphandler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.ToStringBuilder;
import com.paytm.pgplus.request.TokenRequestHeader;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NativeCashierInfoRequest implements Serializable {

    @JsonProperty("head")
    private TokenRequestHeader head;

    @JsonProperty("body")
    private NativeCashierInfoRequestBody body;

    private final static long serialVersionUID = 3384550283911348477L;

    public TokenRequestHeader getHead() {
        return head;
    }

    public void setHead(TokenRequestHeader head) {
        this.head = head;
    }

    public NativeCashierInfoRequestBody getBody() {
        return body;
    }

    public void setBody(NativeCashierInfoRequestBody body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("head", head).append("body", body).toString();
    }

}

