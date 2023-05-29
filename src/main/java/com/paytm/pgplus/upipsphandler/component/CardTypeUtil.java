package com.paytm.pgplus.upipsphandler.component;

import com.paytm.pgplus.cache.model.BinDetail;
import com.paytm.pgplus.payloadvault.theia.request.PaymentRequestBean;
import com.paytm.pgplus.pgproxycommon.enums.ResponseConstants;
import com.paytm.pgplus.theiacommon.supergw.models.bin.BinData;
import com.paytm.pgplus.theiacommon.supergw.response.bin.BinDetailResponse;
import com.paytm.pgplus.upipsphandler.enums.CardValidationCardType;
import com.paytm.pgplus.upipsphandler.exception.NativeFlowException;
import com.paytm.pgplus.upipsphandler.exception.TheiaDataMappingException;
import com.paytm.pgplus.upipsphandler.service.impl.ProcessTransactionUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.paytm.pgplus.upipsphandler.constants.TheiaConstant.ResponseConstants.CARD_BLOCKED_MESSAGE;

@Component
public class CardTypeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CardTypeUtil.class);

    @Autowired
    @Qualifier("processTransactionUtil")
    private ProcessTransactionUtil processTransactionUtil;

    public boolean checkForCardType(BinDetailResponse binDetailResponse, CardValidationCardType cardType) {
        if (binDetailResponse != null && binDetailResponse.getBody() != null) {
            BinData binDetail = binDetailResponse.getBody().getBinDetail();
            if (binDetail != null && StringUtils.isNotBlank(binDetail.getChannelName())
                    && binDetail.getChannelName().equalsIgnoreCase(cardType.getCardType())) {
                return true;
            }
        }
        return false;
    }

    public void validateBlockedBinPayment(BinDetail binDetails, PaymentRequestBean requestData)
            throws TheiaDataMappingException {
        if (!binDetails.isActive()) {
            LOGGER.error("Bin used for payment is blocked - " + binDetails.getBin());
            if (processTransactionUtil.isNativeEnhancedRequest(requestData)) {
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENTMODE)
                        .isHTMLResponse(false).isRedirectEnhanceFlow(false).isRetryAllowed(true)
                        .setRetryMsg(CARD_BLOCKED_MESSAGE).setMsg(CARD_BLOCKED_MESSAGE).build();
            }
            if (processTransactionUtil.isNativeJsonRequest(requestData)) {
                throw new NativeFlowException.ExceptionBuilder(ResponseConstants.INVALID_PAYMENTMODE)
                        .isHTMLResponse(false).isRetryAllowed(true).setMsg(CARD_BLOCKED_MESSAGE)
                        .setRetryMsg(CARD_BLOCKED_MESSAGE).build();
            }
            throw new TheiaDataMappingException(CARD_BLOCKED_MESSAGE, ResponseConstants.INVALID_PAYMENTMODE);
        }
    }
}

