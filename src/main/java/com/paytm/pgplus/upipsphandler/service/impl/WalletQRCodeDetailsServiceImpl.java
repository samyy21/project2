package com.paytm.pgplus.upipsphandler.service.impl;

import com.paytm.pgplus.facade.enums.ExternalEntity;
import com.paytm.pgplus.facade.enums.Type;
import com.paytm.pgplus.facade.enums.WalletQRServiceUrl;
import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.utils.LogUtil;
import com.paytm.pgplus.facade.wallet.helper.WalletQRHelper;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseRequest;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;
import com.paytm.pgplus.httpclient.HttpRequestPayload;
import com.paytm.pgplus.httpclient.JerseyHttpClient;
import com.paytm.pgplus.requestidclient.utils.JsonMapper;
import com.paytm.pgplus.upipsphandler.service.IWalletQRCodeDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service("walletQRCodeDetailsServiceImpl")
public class WalletQRCodeDetailsServiceImpl implements IWalletQRCodeDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.paytm.pgplus.facade.wallet.services.Impl.WalletQRCodeDetailsServiceImpl.class);

    public WalletQRCodeDetailsServiceImpl() {
    }

    @Override
    public QRCodeInfoBaseResponse getQRCodeInfoByQrCodeId(QRCodeInfoBaseRequest request) throws FacadeUncheckedException, FacadeCheckedException {
        long startTime = System.currentTimeMillis();
        HttpRequestPayload<String> payload = WalletQRHelper.generatePayloadForWalletGetQRCodeInfoByQrCodeId(request);

        try {
            Response response = JerseyHttpClient.sendHttpPostRequest(payload);
            if (response.getStatus() != 200) {
                LogUtil.logResponse(response.getStatus(), response.getHeaders(), response.getEntity(), WalletQRServiceUrl.GET_QR_CODE_INFO_BY_QRCODE_ID.getUrl());
                return new QRCodeInfoBaseResponse("FAILURE");
            } else {
                String jsonResponse = (String)response.readEntity(String.class);
                LogUtil.logResponsePayload(ExternalEntity.WALLET, WalletQRServiceUrl.GET_QR_CODE_INFO_BY_QRCODE_ID, Type.RESPONSE, jsonResponse, startTime);
                QRCodeInfoBaseResponse qrCodeResponse = (QRCodeInfoBaseResponse) JsonMapper.mapJsonToObject(jsonResponse, QRCodeInfoBaseResponse.class);
                return qrCodeResponse;
            }
        } catch (Exception var8) {
            LOGGER.error("Exception Occurred : ", var8);
            throw new FacadeCheckedException(var8);
        }
    }

}