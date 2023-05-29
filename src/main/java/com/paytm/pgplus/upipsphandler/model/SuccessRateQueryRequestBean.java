package com.paytm.pgplus.upipsphandler.model;

import java.io.Serializable;

public class SuccessRateQueryRequestBean implements Serializable {

    private static final long serialVersionUID = 3027687532299877614L;

    private String successRateType;

    public SuccessRateQueryRequestBean() {
        this.successRateType = "PAYMETHOD_ISSUINGBANK";
    }

    public String getSuccessRateType() {
        return successRateType;
    }

    public void setSuccessRateType(String successRateType) {
        this.successRateType = successRateType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SuccessRateQueryRequestBean [successRateType=");
        builder.append(successRateType);
        builder.append("]");
        return builder.toString();
    }

}

