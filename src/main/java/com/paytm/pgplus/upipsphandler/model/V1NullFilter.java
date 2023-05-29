package com.paytm.pgplus.upipsphandler.model;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import java.util.Arrays;

public class V1NullFilter {

    public V1NullFilter() {
    }

    /*
     * Filter is used to stop serialization when "version" is v2 and value is
     * null
     */
    @Override
    public boolean equals(Object obj) {
        String version = MDC.get("version");
        String allowedVersions[] = com.paytm.pgplus.biz.utils.ConfigurationUtil.getTheiaProperty(
                com.paytm.pgplus.payloadvault.theia.constant.TheiaConstant.ExtraConstants.FPO_ALLOWED_VERSIONS).split(
                ",");
        boolean isVersionAllowedInFPO = Arrays.stream(allowedVersions).anyMatch(
                n -> org.apache.commons.lang3.StringUtils.equalsIgnoreCase(version, n));
        return (isVersionAllowedInFPO || StringUtils.equals("v4", version)) && (obj == null);
    }
}
