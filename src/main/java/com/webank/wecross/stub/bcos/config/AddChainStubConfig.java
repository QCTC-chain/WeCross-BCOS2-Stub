package com.webank.wecross.stub.bcos.config;

import java.util.ArrayList;
import java.util.List;

public class AddChainStubConfig {
    public static class Chain {
        private int chainId;
        private int groupId;

        public void setChainId(int chainId) {
            this.chainId = chainId;
        }

        public int getChainId() {
            return this.chainId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        public int getGroupId() {
            return this.groupId;
        }
    }

    public static class ChannelService {
        private String caCert = "";
        private String sslCert = "";
        private String sslKey = "";
        private boolean gmConnectEnable = false;
        private String gmCaCert = "";
        private String gmSslCert = "";
        private String gmSslKey = "";
        private String gmEnSslCert = "";
        private String gmEnSslKey = "";
        List<String> connectionsStr = new ArrayList<>();

        public void setCaCert(String caCert) {
            this.caCert = caCert;
        }

        public String getCaCert() {
            return this.caCert;
        }

        public void setSslCert(String sslCert) {
            this.sslCert = sslCert;
        }

        public String getSslCert() {
            return this.sslCert;
        }

        public String getSslKey() {
            return sslKey;
        }

        public void setSslKey(String sslKey) {
            this.sslKey = sslKey;
        }

        public boolean isGmConnectEnable() {
            return gmConnectEnable;
        }

        public void setGmConnectEnable(boolean gmConnectEnable) {
            this.gmConnectEnable = gmConnectEnable;
        }

        public String getGmCaCert() {
            return gmCaCert;
        }

        public void setGmCaCert(String gmCaCert) {
            this.gmCaCert = gmCaCert;
        }

        public String getGmSslCert() {
            return gmSslCert;
        }

        public void setGmSslCert(String gmSslCert) {
            this.gmSslCert = gmSslCert;
        }

        public String getGmSslKey() {
            return gmSslKey;
        }

        public void setGmSslKey(String gmSslKey) {
            this.gmSslKey = gmSslKey;
        }

        public String getGmEnSslCert() {
            return gmEnSslCert;
        }

        public void setGmEnSslCert(String gmEnSslCert) {
            this.gmEnSslCert = gmEnSslCert;
        }

        public String getGmEnSslKey() {
            return gmEnSslKey;
        }

        public void setGmEnSslKey(String gmEnSslKey) {
            this.gmEnSslKey = gmEnSslKey;
        }

        public List<String> getConnectionsStr() {
            return connectionsStr;
        }

        public void setConnectionsStr(List<String> connectionsStr) {
            this.connectionsStr = connectionsStr;
        }
    }

    private Chain chain;
    private ChannelService channelService;

    public void setChain(Chain chain) {
        this.chain = chain;
    }

    public Chain getChain() {
        return this.chain;
    }

    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    public ChannelService getChannelService() {
        return this.channelService;
    }
}
