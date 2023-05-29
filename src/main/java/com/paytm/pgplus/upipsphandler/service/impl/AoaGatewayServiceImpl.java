package com.paytm.pgplus.upipsphandler.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.cache.model.AoaGatewayConfigInfoResponse;
import com.paytm.pgplus.cache.model.RestResponse;
import com.paytm.pgplus.mappingserviceclient.application.MappingServiceClient;
import com.paytm.pgplus.mappingserviceclient.enums.MappingServiceUrl;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IAoaGatewayService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("aoaGatewayServiceImpl")
public class AoaGatewayServiceImpl implements IAoaGatewayService {

    private MappingServiceClient mappingServiceClient = new MappingServiceClient();

    public AoaGatewayServiceImpl() {
    }

    public List<AoaGatewayConfigInfoResponse> getGatewayConfigViaAoaMid(String aoaMid) throws MappingServiceClientException {
        List<AoaGatewayConfigInfoResponse> aoaGatewayConfigInfoResponseList = null;
        String cacheKey = AoaGatewayConfigInfoResponse.generateCacheKeyViaAoaMid(aoaMid);
        aoaGatewayConfigInfoResponseList = (List)this.mappingServiceClient.getDataViaCache(cacheKey, List.class, List.class);
        if (CollectionUtils.isEmpty(aoaGatewayConfigInfoResponseList)) {
            Map<String, String> urlModifier = new HashMap();
            urlModifier.put("{merchantId}", aoaMid);
            RestResponse restResponse = (RestResponse)this.mappingServiceClient.getDataViaHttp(urlModifier, MappingServiceUrl.GET_MERCHANT_GATEWAY_CONFIG_VIA_AOA_MID, RestResponse.class);
            if (null != restResponse) {
                aoaGatewayConfigInfoResponseList = (List)convertObjectValue(restResponse.getResultResp(), List.class);
            }
        }

        return aoaGatewayConfigInfoResponseList;
    }

    public AoaGatewayConfigInfoResponse getGatewayConfigViaAoaMidGatewayName(String aoaMid, String gatewayName) throws MappingServiceClientException {
        AoaGatewayConfigInfoResponse aoaGatewayConfigInfoResponse = null;
        String cacheKey = AoaGatewayConfigInfoResponse.generateCacheKeyViaAoaMidGatewayName(aoaMid, gatewayName);
        aoaGatewayConfigInfoResponse = (AoaGatewayConfigInfoResponse)this.mappingServiceClient.getDataViaCache(cacheKey, AoaGatewayConfigInfoResponse.class, AoaGatewayConfigInfoResponse.class);
        if (null == aoaGatewayConfigInfoResponse) {
            Map<String, String> urlModifier = new HashMap();
            urlModifier.put("{merchantId}", aoaMid);
            urlModifier.put("{gatewayName}", gatewayName);
            RestResponse restResponse = (RestResponse)this.mappingServiceClient.getDataViaHttp(urlModifier, MappingServiceUrl.GET_MERCHANT_GATEWAY_CONFIG_VIA_AOA_MID_GATEWAY_NAME, RestResponse.class);
            if (null != restResponse) {
                aoaGatewayConfigInfoResponse = (AoaGatewayConfigInfoResponse)convertObjectValue(restResponse.getResultResp(), AoaGatewayConfigInfoResponse.class);
            }
        }

        return aoaGatewayConfigInfoResponse;
    }

    public AoaGatewayConfigInfoResponse getGatewayConfigViaGatewayMid(String gatewayMid) throws MappingServiceClientException {
        AoaGatewayConfigInfoResponse aoaGatewayConfigInfoResponse = null;
        String cacheKey = AoaGatewayConfigInfoResponse.generateCacheKeyViaGatewayMid(gatewayMid);
        aoaGatewayConfigInfoResponse = (AoaGatewayConfigInfoResponse)this.mappingServiceClient.getDataViaCache(cacheKey, AoaGatewayConfigInfoResponse.class, AoaGatewayConfigInfoResponse.class);
        if (null == aoaGatewayConfigInfoResponse) {
            Map<String, String> urlModifier = new HashMap();
            urlModifier.put("{merchantId}", gatewayMid);
            RestResponse restResponse = (RestResponse)this.mappingServiceClient.getDataViaHttp(urlModifier, MappingServiceUrl.GET_BY_GATEWAY_MID, RestResponse.class);
            if (null != restResponse) {
                aoaGatewayConfigInfoResponse = (AoaGatewayConfigInfoResponse)convertObjectValue(restResponse.getResultResp(), AoaGatewayConfigInfoResponse.class);
            }
        }

        return aoaGatewayConfigInfoResponse;
    }

    public AoaGatewayConfigInfoResponse getGatewayConfigViaMidGatewayName(String mid, String gatewayName) throws MappingServiceClientException {
        String cacheKey = AoaGatewayConfigInfoResponse.generateCacheKeyViaGatewayMidGatewayName(mid, gatewayName);
        Map<String, String> urlModifier = new HashMap();
        urlModifier.put("{merchantId}", mid);
        urlModifier.put("{gatewayName}", gatewayName);
        return (AoaGatewayConfigInfoResponse)this.mappingServiceClient.getRestResponseData(cacheKey, urlModifier, MappingServiceUrl.GET_MERCHANT_GATEWAY_CONFIG_VIA_MID_GATEWAY_NAME, AoaGatewayConfigInfoResponse.class);
    }

    public static <T> T convertObjectValue(Object fromObject, Class<T> toClass) {
        ObjectMapper MAPPER = new ObjectMapper();
        return MAPPER.convertValue(fromObject, toClass);
    }
}

