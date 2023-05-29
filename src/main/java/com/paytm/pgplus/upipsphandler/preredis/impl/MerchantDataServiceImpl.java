package com.paytm.pgplus.upipsphandler.preredis.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.pgplus.cache.enums.MappingServiceResultCode;
import com.paytm.pgplus.cache.enums.MerchantInfoRequest;
import com.paytm.pgplus.cache.model.*;
import com.paytm.pgplus.common.enums.ContractStatus;
import com.paytm.pgplus.facade.boss.models.response.ContractDetailsQueryResponseBody;
import com.paytm.pgplus.facade.boss.models.response.ContractDetailsResponse;
import com.paytm.pgplus.mapping.service.common.model.PG2MerchantData;
import com.paytm.pgplus.mapping.service.common.model.PG2MerchantExtendedInfoResponse;
import com.paytm.pgplus.mapping.service.common.model.PG2MerchantIdentityInfo;
import com.paytm.pgplus.mapping.service.common.model.PG2MerchantInfo;
import com.paytm.pgplus.mappingserviceclient.application.MappingServiceClient;
import com.paytm.pgplus.mappingserviceclient.enums.MappingServiceUrl;
import com.paytm.pgplus.mappingserviceclient.enums.MerchantUserRequestType;
import com.paytm.pgplus.mappingserviceclient.exception.MappingServiceClientException;
import com.paytm.pgplus.mappingserviceclient.service.IMerchantDataService;
import com.paytm.pgplus.mappingserviceclient.util.MappingClientUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("merchantDataServiceImpl")
public class MerchantDataServiceImpl implements IMerchantDataService {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.paytm.pgplus.mappingserviceclient.service.impl.MerchantDataServiceImpl.class);

    private static MappingServiceClient mappingServiceClient = new MappingServiceClient();

    private MappingClientUtil mappingClientUtil = new MappingClientUtil();
    private static final ObjectMapper mapper = new ObjectMapper();

    public MerchantDataServiceImpl() {
    }

    public MerchantPreferenceInfoResponse getMerchantPreference(String merchantId) throws MappingServiceClientException {
        StringBuilder sb = new StringBuilder();
        sb.append("M_PREFERENCE_").append(merchantId);
        return (MerchantPreferenceInfoResponse)this.mappingClientUtil.getData("read.from.redis.slaves", sb.toString(), merchantId, MappingServiceUrl.FETCH_PREFERENCE_DATA, MerchantPreferenceInfoResponse.class);
    }

    public MerchantInfo getMerchantMappingData(String merchantId) throws MappingServiceClientException {
        String key = "CACHE_KEY_FOR_ALIPAY_AND_PAYTM_ID_" + merchantId;
        LOGGER.info("MERCHANT_V1-{} and cacheKey is {}", merchantId, key);
        return (MerchantInfo)this.mappingClientUtil.getData("read.from.redis.slaves", key, merchantId, MappingServiceUrl.MERCHANT_MAPPING, MerchantInfo.class);
    }

    public MerchantInfo getMerchantMappingData(String merchantId, String identifier) throws MappingServiceClientException {
        String key = "CACHE_KEY_FOR_ALIPAY_AND_PAYTM_ID_" + merchantId;
        String mappingId = merchantId + "?identifier=" + identifier;
        return (MerchantInfo)this.mappingClientUtil.getData("read.from.redis.slaves", key, mappingId, MappingServiceUrl.MERCHANT_MAPPING, MerchantInfo.class);
    }

    public MerchantInfoResponse getMerchantInfoList(MerchantInfoRequest request) throws MappingServiceClientException {
        MerchantInfoResponse response = (MerchantInfoResponse)this.mappingServiceClient.getDataPostRequst(request, MappingServiceUrl.MERCHANT_INFO_LIST, MerchantInfoResponse.class);
        return response;
    }

    public MerchantIdentityInfo getMerchantIdentityMappingData(String merchantId) throws MappingServiceClientException {
        String key = MerchantData.generatePaytmorAlipayCachekey(merchantId);
        MerchantIdentityInfo merchantInfo = (MerchantIdentityInfo)this.mappingServiceClient.getData(key, merchantId, MappingServiceUrl.MERCHANT_IDENTITY_MAPPING, MerchantIdentityInfo.class);
        return merchantInfo;
    }

    public MerchantOfferDetails getMerchantOfferDetails(String merchantId, String channel, String website) throws MappingServiceClientException {
        MerchantOfferDetails merchantOfferDetails = new MerchantOfferDetails(merchantId, channel, website);
        String cacheKey = merchantOfferDetails.generateCacheKey();
        String mappingId = merchantId + "/" + channel + "/" + website;
        if (merchantId != null && channel != null && website != null) {
            merchantOfferDetails = (MerchantOfferDetails)this.mappingServiceClient.getData(cacheKey, mappingId, MappingServiceUrl.MERCHANT_OFFER_DETAILS, MerchantOfferDetails.class);
            return merchantOfferDetails;
        } else {
            return null;
        }
    }

    public MerchantUrlInfo getMerchantUrlInfo(String merchantId, String urlTypeId, String website) throws MappingServiceClientException {
        StringBuilder rediskey = new StringBuilder();
        rediskey.append("M_URL_INFO_").append(merchantId).append(urlTypeId).append(website);
        StringBuilder mappingId = new StringBuilder();
        mappingId.append(merchantId).append("/").append(urlTypeId).append("/").append(website);
        return (MerchantUrlInfo)this.mappingClientUtil.getData("read.from.redis.slaves", rediskey.toString(), mappingId.toString(), MappingServiceUrl.MERCHANT_URL_INFO, MerchantUrlInfo.class);
    }

    public MerchantExtendedInfoResponse getMerchantExtendedDataV2(String merchantId) throws MappingServiceClientException {
        MerchantExtendedInfoResponse merchantExtendedInfoResponse = this.getMerchantExtendedData(merchantId);
        if (null != merchantExtendedInfoResponse.getExtendedInfo() && null == merchantExtendedInfoResponse.getResultInfo()) {
            merchantExtendedInfoResponse.setResultInfo(new MappingServiceResultInfo(MappingServiceResultCode.SUCCESS));
        }

        return merchantExtendedInfoResponse;
    }

    public MerchantExtendedInfoResponse getMerchantExtendedData(String merchantId) throws MappingServiceClientException {
        String key = "MERCHANT_EXTENDED" + "_" + merchantId;
        return (MerchantExtendedInfoResponse)this.mappingClientUtil.getData("read.from.redis.slaves", key, merchantId, MappingServiceUrl.FETCH_MERCHANT_DATA, MerchantExtendedInfoResponse.class);
    }

    public MerchantMappingInfo getMerchantMappingInfo(String merchantGuid) throws MappingServiceClientException {
        Map<String, String> urlModifier = new HashMap();
        urlModifier.put("$MERCHANT_GUID", merchantGuid);
        return (MerchantMappingInfo)this.mappingServiceClient.getDataViaHttp(urlModifier, MappingServiceUrl.GET_MERCHANT_MAPPING_INFO, MerchantMappingInfo.class);
    }

    public MBIdConfiguration getMBID(String merchantId, Long bankCodeId, Long paymentId, Long authModeId) throws MappingServiceClientException {
        MBIdConfiguration mbidConfiguration = new MBIdConfiguration(merchantId, bankCodeId, paymentId, authModeId);
        return (MBIdConfiguration)this.mappingServiceClient.getData(mbidConfiguration.generateCacheKey(), mbidConfiguration.generateQueryParamString(), MappingServiceUrl.GET_MBID_INFO, MBIdConfiguration.class);
    }

    public MerchantProfile getMerchantProfileInfo(String merchantId) throws MappingServiceClientException {
        Map<String, String> urlModifier = new HashMap();
        urlModifier.put("$MERCHANT_PAYTM_ID", merchantId);
        String redisKey = "MERCHANT_INFO" + "_" + "paytm" + "_" + merchantId;
        return (MerchantProfile)this.mappingServiceClient.getData(redisKey, urlModifier, MappingServiceUrl.MERCHANT_PROFILE, MerchantProfile.class);
    }

    public MerchantExtendedInfoResponse getMerchantExtendedDataFromClientIdV2(String merchantId, String clientId) throws MappingServiceClientException {
        MerchantExtendedInfoResponse merchantExtendedInfoResponse = this.getMerchantExtendedDataFromClientId(merchantId, clientId);
        if (null != merchantExtendedInfoResponse.getExtendedInfo() && null == merchantExtendedInfoResponse.getResultInfo()) {
            merchantExtendedInfoResponse.setResultInfo(new MappingServiceResultInfo(MappingServiceResultCode.SUCCESS));
        }

        return merchantExtendedInfoResponse;
    }

    public MerchantExtendedInfoResponse getMerchantExtendedDataFromClientId(String merchantId, String clientId) throws MappingServiceClientException {
        String key = "MERCHANT_EXTENDED" + "_" + merchantId + "_" + clientId;
        String merchantIdDetail = merchantId + "/" + clientId;
        return (MerchantExtendedInfoResponse)this.mappingServiceClient.getData(key, merchantIdDetail, MappingServiceUrl.FETCH_MERCHANT_DATA, MerchantExtendedInfoResponse.class);
    }

    public MerchantUrlInfo getMerchantUrlInfoV2(String merchantId, String urlTypeId, String website) throws MappingServiceClientException {
        StringBuilder rediskey = new StringBuilder();
        rediskey.append("M_URL_INFO_V2").append(merchantId).append(urlTypeId).append(website);
        StringBuilder mappingId = new StringBuilder();
        mappingId.append(merchantId).append("/").append(urlTypeId).append("/").append(website);
        return (MerchantUrlInfo)this.mappingClientUtil.getData("read.from.redis.slaves", rediskey.toString(), mappingId.toString(), MappingServiceUrl.MERCHANT_URL_INFO_V2, MerchantUrlInfo.class);
    }

    public MerchantApiUrlInfoResponse getMerchantApiUrlInfo(String midType, String mid) throws MappingServiceClientException {
        String cacheKey = MerchantApiUrlInfoResponse.generateCacheKey(midType, mid);
        StringBuilder params = new StringBuilder();
        params.append(midType).append("/").append(mid);
        return (MerchantApiUrlInfoResponse)this.mappingClientUtil.getData("read.from.redis.slaves", cacheKey, params.toString(), MappingServiceUrl.MERCHANT_API_URL_INFO, MerchantApiUrlInfoResponse.class);
    }

    public MerchantExtendedInfoResponse getUserMerchantInfoResponse(String userId) throws MappingServiceClientException {
        StringBuilder cacheKey = (new StringBuilder("USER_MERCHANT_ADDTIONAL_INFO")).append("_").append(userId);
        UserMerchantInfo userInfo = (UserMerchantInfo)this.mappingServiceClient.getData(cacheKey.toString(), userId, MappingServiceUrl.FETCH_USER_MID, UserMerchantInfo.class);
        String mid = null;
        if (userInfo != null && userInfo.getMid() != null) {
            mid = userInfo.getMid();
        }

        if (mid == null) {
            return null;
        } else {
            String key = "MERCHANT_EXTENDED" + "_" + mid;
            return (MerchantExtendedInfoResponse)this.mappingServiceClient.getData(key, userId, MappingServiceUrl.FETCH_USER_MERCHANT_EXTENDED_INFO, MerchantExtendedInfoResponse.class);
        }
    }

    public MerchantIdentityInfo getMerchantIdentityMappingData(String merchantId, String idType) throws MappingServiceClientException {
        String key = MerchantData.generatePaytmorAlipayCachekey(merchantId);
        String url = merchantId + "/" + idType;
        MerchantIdentityInfo merchantInfo = (MerchantIdentityInfo)this.mappingServiceClient.getData(key, url, MappingServiceUrl.MERCHANT_IDENTITY_MAPPING, MerchantIdentityInfo.class);
        return merchantInfo;
    }

    public MerchantIdentityInfo getMerchantIdentityMappingData(String merchantId, String idType, String identifier) throws MappingServiceClientException {
        String key = MerchantData.generatePaytmorAlipayCachekey(merchantId);
        String url = merchantId + "/" + idType;
        url = url + "?identifier=" + identifier;
        MerchantIdentityInfo merchantInfo = (MerchantIdentityInfo)this.mappingServiceClient.getData(key, url, MappingServiceUrl.MERCHANT_IDENTITY_MAPPING, MerchantIdentityInfo.class);
        return merchantInfo;
    }

    public PerfernceInfo getMerchantPreferenceInfoExt(String merchantId, String prefType) throws MappingServiceClientException {
        StringBuilder urlBuilder = new StringBuilder();
        String cacheKey = "";
        if (!StringUtils.isEmpty(merchantId)) {
            urlBuilder.append(merchantId);
            if (StringUtils.isNotBlank(prefType)) {
                urlBuilder.append("?prefType=" + prefType);
                cacheKey = PerfernceInfo.generateCacheKeyExt(merchantId) + "_" + prefType;
            } else {
                cacheKey = PerfernceInfo.generateCacheKeyExt(merchantId);
            }

            String mappingID = urlBuilder.toString();
            MappingServiceUrl mappingServiceUrlEnum = MappingServiceUrl.MERCHANT_PREFERENCE_EXT;
            RestResponse resp = null;

            try {
                PerfernceInfo response = (PerfernceInfo)this.mappingServiceClient.getDataViaCache(cacheKey, PerfernceInfo.class, mappingServiceUrlEnum.getCacheClass());
                if (response != null) {
                    return response;
                }
            } catch (Exception var9) {
                resp = (RestResponse)this.mappingServiceClient.getDataViaHttp(mappingID, mappingServiceUrlEnum, RestResponse.class);
            }

            resp = (RestResponse)this.mappingServiceClient.getDataViaHttp(mappingID, mappingServiceUrlEnum, RestResponse.class);
            if (resp != null) {
                return (PerfernceInfo)mapper.convertValue(resp.getResultResp(), PerfernceInfo.class);
            }
        }

        return null;
    }

    public ThematicPreferenceDetails getThematicPreferenceDetails(String merchantId, String prefType) throws MappingServiceClientException {
        StringBuilder urlBuilder = new StringBuilder();
        if (StringUtils.isNotEmpty(merchantId)) {
            urlBuilder.append(merchantId);
            String cacheKey;
            if (StringUtils.isNotEmpty(prefType)) {
                urlBuilder.append("?prefType=" + prefType);
                cacheKey = ThematicPreferenceDetails.generateCacheKeyForThematicPreferenceWithPrefType(merchantId, prefType);
            } else {
                cacheKey = ThematicPreferenceDetails.generateCacheKeyForThematicPreferenceWithPrefType(merchantId, "THEME_PREFERENCE_DETAILS");
            }

            String mappingID = urlBuilder.toString();
            return (ThematicPreferenceDetails)this.mappingServiceClient.getData(cacheKey, mappingID, MappingServiceUrl.GET_MERCHANT_THEMATIC_PREFRENCE_DETAILS, ThematicPreferenceDetails.class);
        } else {
            return null;
        }
    }

    @Override
    public PG2MerchantIdentityInfo getPG2MerchantIdentityInfo(String s, String s1) throws MappingServiceClientException {
        return null;
    }

    @Override
    public PG2MerchantIdentityInfo getPG2MerchantIdentityInfo(String s, String s1, String s2) throws MappingServiceClientException {
        return null;
    }

    @Override
    public PG2MerchantExtendedInfoResponse getPG2MerchantExtendedData(String s) throws MappingServiceClientException {
        return null;
    }

    @Override
    public PG2MerchantExtendedInfoResponse getPG2MerchantExtendedDataFromClientId(String s, String s1) throws MappingServiceClientException {
        return null;
    }

    @Override
    public PG2MerchantData getPG2PaytmMid(String s) throws MappingServiceClientException {
        return null;
    }

    @Override
    public PG2MerchantInfo getPg2MerchantMappingData(String s) throws MappingServiceClientException {
        return null;
    }

    @Override
    public PG2MerchantInfo getPg2MerchantMappingData(String s, String s1) throws MappingServiceClientException {
        return null;
    }

    public MerchantIdentityInfo getMerchantIdentityInfo(String type, String merchantId) throws MappingServiceClientException {
        try {
            String cacheKey = MerchantData.generatePaytmorAlipayCachekey(merchantId);
            MerchantIdentityInfo merchantIdentityInfo = (MerchantIdentityInfo)this.mappingServiceClient.getDataViaCache(cacheKey, MerchantIdentityInfo.class, MerchantIdentityInfo.class);
            return merchantIdentityInfo;
        } catch (Exception var5) {
            LOGGER.warn("Unable to get the Data via Redis cache", var5);
            Map<String, String> urlModifier = new HashMap();
            urlModifier.put("{type}", type);
            urlModifier.put("{merchantId}", merchantId);
            return (MerchantIdentityInfo)this.mappingServiceClient.getDataViaHttp(urlModifier, MappingServiceUrl.GET_MERCHANT_IDENTITY_INFO, MerchantIdentityInfo.class);
        }
    }

    public MerchantIdentityInfo getMerchantIdentityInfo(String type, String merchantId, String identifier) throws MappingServiceClientException {
        try {
            String cacheKey = MerchantData.generatePaytmorAlipayCachekey(merchantId);
            MerchantIdentityInfo merchantIdentityInfo = (MerchantIdentityInfo)this.mappingServiceClient.getDataViaCache(cacheKey, MerchantIdentityInfo.class, MerchantIdentityInfo.class);
            return merchantIdentityInfo;
        } catch (Exception var6) {
            LOGGER.warn("Unable to get the Data via Redis cache", var6);
            Map<String, String> urlModifier = new HashMap();
            urlModifier.put("{type}", type);
            urlModifier.put("{merchantId}", merchantId + "?identifier=" + identifier);
            return (MerchantIdentityInfo)this.mappingServiceClient.getDataViaHttp(urlModifier, MappingServiceUrl.GET_MERCHANT_IDENTITY_INFO, MerchantIdentityInfo.class);
        }
    }

    public ContractDetailsResponse getMerchantContractDetails(String mid) throws MappingServiceClientException {
        String cacheKey = ContractDetailsResponse.generateCacheKey(mid);
        Map<String, String> urlModifier = new HashMap();
        urlModifier.put("{mid}", mid);
        return (ContractDetailsResponse)this.mappingServiceClient.getData(cacheKey, urlModifier, MappingServiceUrl.GET_MERCHANT_CONTRACT_DETAILS, ContractDetailsResponse.class);
    }


    @Override
    public ContractDetailsQueryResponseBody getMerchantContractWithProductCode(String merchantId, ContractStatus contractStatus, String productCode) throws MappingServiceClientException {
        String cacheKey = "CONTRACT_DETAILS_" + merchantId + "_" + contractStatus + "_" + productCode;
        Map<String, String> urlModifier = new HashMap();
        urlModifier.put("{merchantId}", merchantId);
        urlModifier.put("{contractStatus}", contractStatus.getDescription());
        urlModifier.put("{productCode}", productCode);
        return (ContractDetailsQueryResponseBody)this.mappingServiceClient.getData(cacheKey, urlModifier, MappingServiceUrl.GET_MERCHANT_CONRACT_WITH_PRODUCT_CODE, ContractDetailsQueryResponseBody.class);
    }

    public MerchantData getPaytmMid(String alipayId) throws MappingServiceClientException {
        String cacheKey = MerchantData.getMerchantDataCacheKeyForAlipayOrPaytm(alipayId);
        return (MerchantData)this.mappingServiceClient.getData(cacheKey, alipayId, MappingServiceUrl.GET_PAYTM_MID, MerchantData.class);
    }

    private String generateMerchantDataCacheKey(String merchantId, String type) {
        String cacheKey = null;
        if (MerchantUserRequestType.ALIPAY.getValue().equalsIgnoreCase(type)) {
            cacheKey = MerchantData.generateAlipayCacheKey(merchantId);
        } else if (MerchantUserRequestType.PAYTM.getValue().equalsIgnoreCase(type)) {
            cacheKey = MerchantData.generatePaytmCacheKey(merchantId);
        }

        return cacheKey;
    }

    private String generateMerchantIdentityCacheKey(String merchantId, String type) {
        String cacheKey = null;
        if (MerchantUserRequestType.ALIPAY.getValue().equalsIgnoreCase(type)) {
            cacheKey = MerchantIdentityInfo.generatePGPlusCacheKey(merchantId);
        } else if (MerchantUserRequestType.PAYTM.getValue().equalsIgnoreCase(type)) {
            cacheKey = MerchantIdentityInfo.generatePaytmCacheKey(merchantId);
        } else if (MerchantUserRequestType.GUID.getValue().equalsIgnoreCase(type)) {
            cacheKey = MerchantIdentityInfo.generateGuidCacheKey(merchantId);
        } else if (MerchantUserRequestType.SSOID.getValue().equalsIgnoreCase(type)) {
            cacheKey = MerchantIdentityInfo.generateSsoIdCacheKey(merchantId);
        } else {
            cacheKey = MerchantIdentityInfo.generatePaytmCacheKey(merchantId);
        }

        return cacheKey;
    }
}

