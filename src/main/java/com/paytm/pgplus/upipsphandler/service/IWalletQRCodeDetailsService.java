package com.paytm.pgplus.upipsphandler.service;

import com.paytm.pgplus.facade.exception.FacadeCheckedException;
import com.paytm.pgplus.facade.exception.FacadeUncheckedException;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseRequest;
import com.paytm.pgplus.facade.wallet.models.QRCodeInfoBaseResponse;

public interface IWalletQRCodeDetailsService {
    QRCodeInfoBaseResponse getQRCodeInfoByQrCodeId(QRCodeInfoBaseRequest var1) throws FacadeUncheckedException, FacadeCheckedException;
}
