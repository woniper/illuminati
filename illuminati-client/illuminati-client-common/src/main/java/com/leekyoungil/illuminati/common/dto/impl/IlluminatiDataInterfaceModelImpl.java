package com.leekyoungil.illuminati.common.dto.impl;

import com.leekyoungil.illuminati.common.dto.IlluminatiInterfaceModel;
import com.leekyoungil.illuminati.common.dto.RequestHeaderModel;
import com.leekyoungil.illuminati.common.dto.enums.IlluminatiInterfaceType;
import com.leekyoungil.illuminati.common.dto.enums.IlluminatiTransactionIdType;
import com.leekyoungil.illuminati.common.util.ConvertUtil;
import com.leekyoungil.illuminati.common.util.SystemUtil;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class IlluminatiDataInterfaceModelImpl implements IlluminatiInterfaceModel {

    private static final Logger ILLUMINATI_DATA_INTERFACE_MODEL_LOGGER = LoggerFactory.getLogger(IlluminatiDataInterfaceModelImpl.class);

    private final static String ILLUMINATI_UNIQUE_USER_ID_KEY_NAME = "illuminatiUniqueUserId";

    private final MethodSignature signature;
    private final Object[] args;
    private long elapsedTime = 0L;
    private final Map<String, Object> output;

    private String illuminatiUniqueUserId;
    private RequestHeaderModel requestHeaderModel;
    private Map<String, String> clientInfoMap;
    private Map<String, Object> staticInfo;
    private boolean isActiveChaosBomber;

    public IlluminatiDataInterfaceModelImpl(final HttpServletRequest request, final MethodSignature signature, final Object[] args, long elapsedTime, final Map<String, Object> resultMap) {
        this.signature = signature;
        this.args = args;
        this.elapsedTime = elapsedTime;
        this.output = resultMap;

        this.initDataFromHttpRequest(request);
    }

    private void initDataFromHttpRequest (final HttpServletRequest request) {
        this.requestHeaderModel = new RequestHeaderModel(request);
        this.requestHeaderModel.setSessionTransactionId(SystemUtil.generateTransactionIdByRequest(request, IlluminatiTransactionIdType.ILLUMINATI_S_PROC_ID));
        this.requestHeaderModel.setGlobalTransactionId(SystemUtil.generateTransactionIdByRequest(request, IlluminatiTransactionIdType.ILLUMINATI_G_PROC_ID));
        this.requestHeaderModel.setTransactionId(SystemUtil.generateTransactionIdByRequest(request, IlluminatiTransactionIdType.ILLUMINATI_PROC_ID));
        this.illuminatiUniqueUserId = SystemUtil.getValueFromHeaderByKey(request, ILLUMINATI_UNIQUE_USER_ID_KEY_NAME);
        this.clientInfoMap = ConvertUtil.getClientInfoFromHttpRequest(request);
        this.staticInfo = ConvertUtil.getStaticInfoFromHttpRequest(request);
        this.isActiveChaosBomber = ConvertUtil.getChaosBomberFromHttpRequest(request);
    }

    public boolean isValid () {
        if (this.requestHeaderModel == null) {
            ILLUMINATI_DATA_INTERFACE_MODEL_LOGGER.warn("request is must not null");
            return  false;
        }
        if (signature == null) {
            ILLUMINATI_DATA_INTERFACE_MODEL_LOGGER.warn("signature is must not null");
            return  false;
        }

        return true;
    }

    public long getElapsedTime () {
        return this.elapsedTime;
    }
    public Object getOutput () {
        return this.output;
    }
    public Object[] getParamValues () {
        return this.args;
    }
    public MethodSignature getSignature () {
        return this.signature;
    }
    public RequestHeaderModel getRequestHeaderModel () {
        return this.requestHeaderModel;
    }
    public String getIlluminatiUniqueUserId() {
        return this.illuminatiUniqueUserId;
    }
    public Map<String, String> getClientInfoMap() {
        return this.clientInfoMap;
    }
    public Map<String, Object> getStaticInfo() {
        return this.staticInfo;
    }
    public boolean isActiveChaosBomber() {
        return this.isActiveChaosBomber;
    }

    @Override
    public IlluminatiInterfaceType getInterfaceType() {
        return null;
    }

    @Override
    public void setIlluminatiInterfaceType(IlluminatiInterfaceType illuminatiInterfaceType) {

    }
}
