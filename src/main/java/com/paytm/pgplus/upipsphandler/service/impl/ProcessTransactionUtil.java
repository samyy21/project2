package com.paytm.pgplus.upipsphandler.service.impl;

import com.paytm.pgplus.biz.utils.BizConstant;
import com.paytm.pgplus.biz.workflow.service.util.FailureLogUtil;
import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.common.enums.EPayMethod;
import com.paytm.pgplus.common.enums.ERequestType;
import com.paytm.pgplus.common.enums.EventNameEnum;
import com.paytm.pgplus.common.model.CardTokenInfo;
import com.paytm.pgplus.common.model.nativ.NativeConsultDetails;
import com.paytm.pgplus.biz.workflow.model.WorkFlowRequestBean;
import com.paytm.pgplus.enums.EChannelId;
import com.paytm.pgplus.facade.common.model.BankFormOptimizationParams;
import com.paytm.pgplus.facade.emisubvention.models.request.ValidateRequest;
import com.paytm.pgplus.facade.payment.models.FeeRateFactors;
import com.paytm.pgplus.logging.ExtendedLogger;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.payloadvault.theia.response.TransactionResponse;
import com.paytm.pgplus.pgproxycommon.utils.BinHelper;
import com.paytm.pgplus.pgproxycommon.utils.CardUtils;
import com.paytm.pgplus.pgproxycommon.utils.CoftUtils;
import com.paytm.pgplus.pgproxycommon.utils.EcomTokenUtils;
import com.paytm.pgplus.request.TokenRequestHeader;
import com.paytm.pgplus.savedcardclient.models.SavedCardResponse;
import com.paytm.pgplus.savedcardclient.models.request.SavedCardVO;
import com.paytm.pgplus.savedcardclient.service.ISavedCardService;
import com.paytm.pgplus.theia.nativ.utils.CardTransactionUtil;
import com.paytm.pgplus.upipsphandler.component.NativePaymentUtil;
import com.paytm.pgplus.upipsphandler.component.NativeSessionUtil;
import com.paytm.pgplus.upipsphandler.component.SubventionEmiServiceHelper;
import com.paytm.pgplus.upipsphandler.constants.TheiaConstant;
import com.paytm.pgplus.upipsphandler.enums.common.ResultCode;
import com.paytm.pgplus.upipsphandler.exception.NativeFlowException;
import com.paytm.pgplus.upipsphandler.exception.TheiaServiceException;
import com.paytm.pgplus.upipsphandler.model.*;
import com.paytm.pgplus.upipsphandler.utils.ConfigurationUtil;
import com.paytm.pgplus.upipsphandler.utils.EventUtils;
import com.paytm.pgplus.upipsphandler.utils.Ff4jUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.paytm.pgplus.common.enums.CardTypeEnum.DINERS;
import static com.paytm.pgplus.upipsphandler.constants.TheiaConstant.FF4J.THEIA_CHECK_PAYMENT_STATUS_IN_FORM_OPTIMIZED_FLOW;
import static com.paytm.pgplus.upipsphandler.constants.TheiaConstant.FF4J.THEIA_WAIT_TIME_FOR_BANK_FORM_OPTIMIZATION;
import static com.paytm.pgplus.upipsphandler.constants.TheiaConstant.MerchantPreference.PreferenceKeys.NATIVE_JSON_REQUEST;
import static com.paytm.pgplus.upipsphandler.constants.TheiaConstant.ResponseConstants.CARD_NOT_ALLOWD_ON_MERCHANT;

@Service("processTransactionUtil")
public class ProcessTransactionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTransactionUtil.class);
    private static final ExtendedLogger EXT_LOGGER = ExtendedLogger.create(ProcessTransactionUtil.class);

    private static final String MAESTRO_CVV = "|123";
    private static final String BAJAJFN_CVV = "|1111";
    private static final String MAESTRO_EXPIRY = "|122049";
    private static final String MAESTRO = "MAESTRO";
    private static final String VIEW_BASE = "/WEB-INF/views/jsp/";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private static final String ADDITIONAL_INFO_DELIMITER = "|";
    private static final int ETM_ENCRYPTION_CONSTANT = 7;

    private static final String ADDITIONAL_INFO_KEY_VAL_SEPARATOR = ":";

    private static final String GV_PRODUCT_CODE = ConfigurationUtil.getProperty(TheiaConstant.GV_PRODUCT_CODE);
    private static final String NCMC_PRODUCT_CODE = ConfigurationUtil.getProperty(TheiaConstant.NCMC_PRODUCT_CODE);

    @Autowired
    private Ff4jUtils ff4jUtils;
    @Autowired
    private EcomTokenUtils ecomTokenUtils;
    @Autowired
    private NativePaymentUtil nativePaymentUtil;
    @Autowired
    BinHelper binHelper;
    @Autowired
    CoftUtils coftUtils;
    @Autowired
    private NativeSessionUtil nativeSessionUtil;
    @Autowired
    @Qualifier("savedCardService")
    private ISavedCardService savedCardService;
    @Autowired
    @Qualifier("cardUtils")
    private CardUtils cardUtils;
    @Autowired
    private SubventionEmiServiceHelper subventionEmiServiceHelper;
    @Autowired
    @Qualifier("cardTransactionUtil")
    private CardTransactionUtil cardTransactionUtil;
    @Autowired
    @Qualifier("failureLogUtil")
    private FailureLogUtil failureLogUtil;


//    public void setBankFormOptimizationFlow(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
//        if ((requestData.isGenerateEsnRequest() || requestData.isOrderPSPRequest())
//                && ff4jUtils.isFeatureEnabledOnMidPayModeAndPercentage(TheiaConstant.FF4J.BANK_FORM_OPTIMIZED_FLOW,
//                requestData.getMid(), flowRequestBean.getPayMethod(), flowRequestBean.getPayOption(),
//                (flowRequestBean.getUserDetailsBiz() != null) ? flowRequestBean.getUserDetailsBiz().getUserId()
//                        : null, requestData.getOrderId())) {
//            flowRequestBean.setBankFormOptimizationParams(new BankFormOptimizationParams());
//            flowRequestBean.getBankFormOptimizationParams().setBankFormOptimizedFlow(true);
//            try {
//                flowRequestBean.getBankFormOptimizationParams().setWaitTimeForBankFormOptimizedFlow(
//                        Long.parseLong(ff4jUtils.getPropertyAsStringWithDefault(
//                                THEIA_WAIT_TIME_FOR_BANK_FORM_OPTIMIZATION, "10")));
//            } catch (Exception ex) {
//                LOGGER.error(
//                        "Exception occured while parsing THEIA_SLEEP_TIME_BANK_FORM_OPTIMIZATION property into long {}",
//                        ex);
//                flowRequestBean.getBankFormOptimizationParams().setWaitTimeForBankFormOptimizedFlow(10);
//            }
//            flowRequestBean.getBankFormOptimizationParams().setCheckPaymentStateInFormOptimizedFlow(
//                    ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(),
//                            THEIA_CHECK_PAYMENT_STATUS_IN_FORM_OPTIMIZED_FLOW, false));
//        }
//    }
    public boolean isNativeEnhancedRequest(PaymentRequestBean paymentRequestData) {

        if (paymentRequestData.getRequest() != null
                && paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW") != null
                && Boolean.TRUE.equals(paymentRequestData.getRequest().getAttribute("NATIVE_ENHANCED_FLOW"))) {
            return true;
        }

        return false;
    }
    public boolean isNativeJsonRequest(PaymentRequestBean paymentRequestBean) {
        return (paymentRequestBean != null) && (paymentRequestBean.getRequest() != null)
                && isNativeJsonRequest(paymentRequestBean.getRequest());
    }
    public boolean isNativeJsonRequest(HttpServletRequest request) {
        return (request != null && null != request.getAttribute(NATIVE_JSON_REQUEST))
                && Boolean.TRUE.equals(request.getAttribute(NATIVE_JSON_REQUEST));
    }

    public boolean isRequestOfType(String requestType) {
        if (((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI()
                .contains(requestType)) {
            return true;
        }
        return false;
    }

    public boolean isEnhancedNativeFlow(HttpServletRequest request) {
        return (null != request.getAttribute("NATIVE_ENHANCED_FLOW"))
                && Boolean.TRUE.equals(request.getAttribute("NATIVE_ENHANCED_FLOW"));
    }

    public BinDetail getBinDetail(final WorkFlowRequestBean flowRequestBean) {
        String cardNumber = flowRequestBean.getCardNo();
        if (StringUtils.isBlank(cardNumber) && flowRequestBean.getIsSavedCard()
                && StringUtils.isNotBlank(flowRequestBean.getSavedCardID())) {
            if (flowRequestBean.getSavedCardID().length() > 15 && !flowRequestBean.isCoftTokenTxn()) {
                cardNumber = nativeSessionUtil.getMaskCardNumberFromCIN(flowRequestBean.getSavedCardID());
            } else {
                SavedCardResponse<SavedCardVO> savedCardResponse = savedCardService.getSavedCardByCardId(Long
                        .parseLong(flowRequestBean.getSavedCardID()));
                if ((savedCardResponse != null) && savedCardResponse.getStatus()) {
                    cardNumber = savedCardResponse.getResponseData().getCardNumber();
                    flowRequestBean.setCardNo(cardNumber);
                }
            }
        }
        if (flowRequestBean.isTxnFromCardIndexNo()) {
            return flowRequestBean.getBinDetail();
        } else if (StringUtils.isNotBlank(cardNumber)) {
            if (flowRequestBean.isEcomTokenTxn()) {
                BinDetail tokenBinDetail = getBinDetailForEcomToken(cardNumber);
                validateTokenDetail(tokenBinDetail);
                return tokenBinDetail;
            } else if (flowRequestBean.isCoftTokenTxn()) {
                BinDetail tokenBinDetail = getBinDetailForCoft(cardNumber);
                validateTokenDetail(tokenBinDetail);
                return tokenBinDetail;
            } else {
                BinDetail binDetail = getBinDetailForCardBin(cardNumber);
                validateBinDetail(binDetail);
                return binDetail;
            }
        }
        return null;
    }
    public BinDetail getBinDetail(final com.paytm.pgplus.upipsphandler.model.WorkFlowRequestBean flowRequestBean) {
        String cardNumber = flowRequestBean.getCardNo();
        if (StringUtils.isBlank(cardNumber) && flowRequestBean.getIsSavedCard()
                && StringUtils.isNotBlank(flowRequestBean.getSavedCardID())) {
            if (flowRequestBean.getSavedCardID().length() > 15 && !flowRequestBean.isCoftTokenTxn()) {
                cardNumber = nativeSessionUtil.getMaskCardNumberFromCIN(flowRequestBean.getSavedCardID());
            } else {
                SavedCardResponse<SavedCardVO> savedCardResponse = savedCardService.getSavedCardByCardId(Long
                        .parseLong(flowRequestBean.getSavedCardID()));
                if ((savedCardResponse != null) && savedCardResponse.getStatus()) {
                    cardNumber = savedCardResponse.getResponseData().getCardNumber();
                    flowRequestBean.setCardNo(cardNumber);
                }
            }
        }
        if (flowRequestBean.isTxnFromCardIndexNo()) {
            return flowRequestBean.getBinDetail();
        } else if (StringUtils.isNotBlank(cardNumber)) {
            if (flowRequestBean.isEcomTokenTxn()) {
                BinDetail tokenBinDetail = getBinDetailForEcomToken(cardNumber);
                validateTokenDetail(tokenBinDetail);
                return tokenBinDetail;
            } else if (flowRequestBean.isCoftTokenTxn()) {
                BinDetail tokenBinDetail = getBinDetailForCoft(cardNumber);
                validateTokenDetail(tokenBinDetail);
                return tokenBinDetail;
            } else {
                BinDetail binDetail = getBinDetailForCardBin(cardNumber);
                validateBinDetail(binDetail);
                return binDetail;
            }
        }
        return null;
    }
    public BinDetail getBinDetailForEcomToken(final String requiredCardDetails) {

        final String binNumber = requiredCardDetails.substring(0, 9);
        BinDetail binDetails = null;

        try {
            binDetails = ecomTokenUtils.fetchTokenDetails(binNumber);
        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}", binNumber, e);
        }
        return binDetails;
    }
//
    public void validateTokenDetail(final BinDetail tokenDetails) {
        if ((tokenDetails == null) || StringUtils.isEmpty(tokenDetails.getCardType())
                || StringUtils.isEmpty(tokenDetails.getCardName())) {
            throw new TheiaServiceException("Mandatory data not available for the token ");
        }
    }
    public BinDetail getBinDetailForCoft(final String requiredCardDetails) {

        final String binNumber = requiredCardDetails.substring(0, 9);
        BinDetail binDetails = null;

        try {
            binDetails = coftUtils.fetchTokenDetails(binNumber);
        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}", binNumber, e);
        }
        return binDetails;
    }
    public BinDetail getBinDetailForCardBin(final String requiredCardDetails) {
        /*
         * update paymode as the bin cardtype
         */
        BinDetail binDetails = null;
        try {
            binDetails = cardUtils.fetchBinDetails(requiredCardDetails);

        } catch (Exception e) {
            LOGGER.error("failed fetching binDetails for bin {}, : {}",
                    binHelper.logMaskedBinnumber(requiredCardDetails), e);
        }
        return binDetails;
    }
    public void validateBinDetail(final BinDetail binDetails) {
        if ((binDetails == null) || StringUtils.isEmpty(binDetails.getBank())
                || StringUtils.isEmpty(binDetails.getCardType()) || StringUtils.isEmpty(binDetails.getCardName())) {
            throw new TheiaServiceException("Mandatory data not available for the bin number");
        }
    }

    public PCFFeeCharges getPCFFeecharges(String txnToken, EPayMethod payMethod, String instId,
                                          FeeRateFactors feeRateFactors) throws Exception {
        PCFFeeCharges pcfFeeCharges = new PCFFeeCharges();

        Map<EPayMethod, NativeConsultDetails> consultFeeResponse = nativePaymentUtil.consultFeeForGivenPaymode(
                txnToken, payMethod.getMethod(), instId, feeRateFactors, null);
        if (consultFeeResponse != null && consultFeeResponse.size() > 0) {
            NativeConsultDetails consultDetails = consultFeeResponse.get(payMethod);
            pcfFeeCharges.setFeeAmount(consultDetails.getFeeAmount());
            pcfFeeCharges.setTaxAmount(consultDetails.getTaxAmount());
            pcfFeeCharges.setTotalTransactionAmount(consultDetails.getTotalTransactionAmount());

        }

        return pcfFeeCharges;
    }
    public boolean isAddMoneyMerchant(String mid) {
        if (StringUtils.isNotBlank(mid)) {
            return StringUtils.equals(mid, com.paytm.pgplus.common.config.ConfigurationUtil.getProperty(BizConstant.MP_ADD_MONEY_MID));
        }
        return false;
    }
    public PayMethod getPayMethod(NativeCashierInfoResponse cashierInfoResponse, String ePayMethod) {
        if (null != cashierInfoResponse && null != cashierInfoResponse.getBody()
                && null != cashierInfoResponse.getBody().getMerchantPayOption()
                && null != cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods()) {
            List<PayMethod> payMethods = cashierInfoResponse.getBody().getMerchantPayOption().getPayMethods();
            for (PayMethod payMethod : payMethods) {
                if (ePayMethod.equals(payMethod.getPayMethod())) {
                    return payMethod;
                }
            }
        }
        return null;
    }
    public PayMethod getPayMethodForAddnPay(NativeCashierInfoResponse cashierInfoResponse, String ePayMethod) {
        if (null != cashierInfoResponse.getBody().getAddMoneyPayOption()
                && null != cashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods()) {
            List<PayMethod> payMethods = cashierInfoResponse.getBody().getAddMoneyPayOption().getPayMethods();
            for (PayMethod payMethod : payMethods) {
                if (ePayMethod.equals(payMethod.getPayMethod())) {
                    return payMethod;
                }
            }
        }
        return null;
    }

    public void setBankFormOptimizationFlow(WorkFlowRequestBean flowRequestBean, PaymentRequestBean requestData) {
        if ((requestData.isGenerateEsnRequest() || requestData.isOrderPSPRequest())
                && ff4jUtils.isFeatureEnabledOnMidPayModeAndPercentage(TheiaConstant.FF4J.BANK_FORM_OPTIMIZED_FLOW,
                requestData.getMid(), flowRequestBean.getPayMethod(), flowRequestBean.getPayOption(),
                (flowRequestBean.getUserDetailsBiz() != null) ? flowRequestBean.getUserDetailsBiz().getUserId()
                        : null, requestData.getOrderId())) {
            flowRequestBean.setBankFormOptimizationParams(new BankFormOptimizationParams());
            flowRequestBean.getBankFormOptimizationParams().setBankFormOptimizedFlow(true);
            try {
                flowRequestBean.getBankFormOptimizationParams().setWaitTimeForBankFormOptimizedFlow(
                        Long.parseLong(ff4jUtils.getPropertyAsStringWithDefault(
                                THEIA_WAIT_TIME_FOR_BANK_FORM_OPTIMIZATION, "10")));
            } catch (Exception ex) {
                LOGGER.error(
                        "Exception occured while parsing THEIA_SLEEP_TIME_BANK_FORM_OPTIMIZATION property into long {}",
                        ex);
                flowRequestBean.getBankFormOptimizationParams().setWaitTimeForBankFormOptimizedFlow(10);
            }
            flowRequestBean.getBankFormOptimizationParams().setCheckPaymentStateInFormOptimizedFlow(
                    ff4jUtils.isFeatureEnabledOnMid(requestData.getMid(),
                            THEIA_CHECK_PAYMENT_STATUS_IN_FORM_OPTIMIZED_FLOW, false));
        }
    }
    public void paymentOptionCardIndexNoPopulated(HttpServletRequest request, ValidateRequest subventionvalidateRequest) {
        if (subventionvalidateRequest.getPaymentDetails() != null
                && CollectionUtils.isNotEmpty(subventionvalidateRequest.getPaymentDetails().getPaymentOptions())
                && StringUtils.isBlank(subventionvalidateRequest.getPaymentDetails().getPaymentOptions().get(0)
                .getCardIndexNo()))
            subventionvalidateRequest.getPaymentDetails().setPaymentOptions(
                    Collections.singletonList(subventionEmiServiceHelper.updatePaymentOptionPopulateCardIndexNo(
                            request, subventionvalidateRequest.getPaymentDetails().getPaymentOptions().get(0))));
    }
    public TokenRequestHeader getTokenRequestHeader(String txnToken, String channel, String version) {
        TokenRequestHeader tokenRequestHeader = new TokenRequestHeader();
        tokenRequestHeader.setVersion(version);
        tokenRequestHeader.setRequestTimestamp(Long.toString(System.currentTimeMillis()));
        tokenRequestHeader.setTxnToken(txnToken);
        if (StringUtils.isNotBlank(channel)) {
            tokenRequestHeader.setChannelId(EChannelId.valueOf(channel.toUpperCase()));
        } else {
            tokenRequestHeader.setChannelId(EChannelId.WEB);
        }
        return tokenRequestHeader;

    }
    public Boolean isValidCardTokenInfo(CardTokenInfo cardTokenInfo, String cardScheme) {
        if (StringUtils.isBlank(cardTokenInfo.getCardToken()) || StringUtils.isBlank(cardTokenInfo.getCardSuffix())) {
            LOGGER.error("Invalid cardTokenInfo : {}", cardTokenInfo);
            return Boolean.FALSE;
        } else if (DINERS.getName().equalsIgnoreCase(cardScheme)) {
            if (StringUtils.isBlank(cardTokenInfo.getTokenUniqueReference())
                    || StringUtils.isBlank(cardTokenInfo.getMerchantTokenRequestorId())) {
                LOGGER.error("Invalid cardTokenInfo for Diners Scheme : {}", cardTokenInfo);
                return Boolean.FALSE;
            }
        } else if (StringUtils.isBlank(cardTokenInfo.getTAVV())
                || StringUtils.isBlank(cardTokenInfo.getPanUniqueReference())) {
            LOGGER.error("Invalid cardTokenInfo : {}", cardTokenInfo);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    public void checkIfIssuerAllowedOnMid(String mid, ERequestType requestType, String issuerConfig,
                                          boolean isAddNPayOrAddMoneyFlow, boolean sendHTML) {
        if (!cardTransactionUtil.isIssuerConfigAllowedOnMid(requestType, mid, issuerConfig, isAddNPayOrAddMoneyFlow)) {
            String customErrorMsg = cardTransactionUtil.getCustomErrMsgForIssuerNotAllowed(mid);
            customErrorMsg = StringUtils.isNotBlank(customErrorMsg) ? customErrorMsg : CARD_NOT_ALLOWD_ON_MERCHANT;
            failureLogUtil.setFailureMsgForDwhPush(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN.getResultCodeId(),
                    customErrorMsg, null, true);
            throw new NativeFlowException.ExceptionBuilder(ResultCode.PAYMENT_NOT_ALLOWED_FOR_BIN)
                    .isHTMLResponse(sendHTML).isRetryAllowed(true).setMsg(customErrorMsg).setRetryMsg(customErrorMsg)
                    .build();
        }
    }
    public void pushNativeJsonResponseEvent(TransactionResponse transactionResponse) {

        LinkedHashMap<String, String> metaData = new LinkedHashMap<>();

        if (transactionResponse != null) {

            metaData.put("mid", StringUtils.defaultString(transactionResponse.getMid()));
            metaData.put("orderId", StringUtils.defaultString(transactionResponse.getOrderId()));
            EPayMethod payMethod = EPayMethod.getPayMethodByOldName(transactionResponse.getPaymentMode());
            if (payMethod != null) {
                metaData.put("paymentMode", StringUtils.defaultString(payMethod.getMethod()));
            }
            metaData.put("bankName", StringUtils.defaultString(transactionResponse.getBankName()));
            metaData.put("gatewayName", StringUtils.defaultString(transactionResponse.getGateway()));
            metaData.put("responseCode", StringUtils.defaultString(transactionResponse.getResponseCode()));
            metaData.put("responseMsg", StringUtils.defaultString(transactionResponse.getResponseMsg()));
            metaData.put("txnStatus", StringUtils.defaultString(transactionResponse.getTransactionStatus()));
            metaData.put("txnAmount", StringUtils.defaultString(transactionResponse.getTxnAmount()));

            EventUtils.pushTheiaEvents(transactionResponse.getMid(), transactionResponse.getOrderId(),
                    EventNameEnum.NATIVE_JSON_RESPONSE, metaData);
        }
    }
}
