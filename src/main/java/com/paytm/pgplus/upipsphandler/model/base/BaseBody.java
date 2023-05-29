package com.paytm.pgplus.upipsphandler.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.paytm.pgplus.theiacommon.utils.OfflinePaymentUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Map;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseBody implements Serializable {

    private static final long serialVersionUID = -325958117771934408L;
    private Map<String, Object> extendInfo;
    @ApiModelProperty(notes = "Currenlty its optional, as HMAC to be implemented")
    @JsonIgnore
    private String signature;

    public Map<String, Object> getExtendInfo() {
        return extendInfo;
    }

    public void setExtendInfo(Map<String, Object> extendInfo) {
        this.extendInfo = extendInfo;
    }

    public void setExtendInfoString(Map<String, String> extendInfo) {
        this.extendInfo = OfflinePaymentUtils.toStringObjectMap(extendInfo);
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseBody{");
        sb.append("extendInfo=").append(extendInfo);
        sb.append(", signature='").append(signature).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
