package com.webank.wecross.stub.bcos;

import com.webank.wecross.stub.bcos.client.AbstractClientWrapper;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.fisco.bcos.sdk.abi.ABICodec;
import org.fisco.bcos.sdk.abi.ABICodecException;
import org.fisco.bcos.sdk.abi.EventEncoder;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.eventsub.EventLogParams;
import org.fisco.bcos.sdk.eventsub.EventSubscribe;
import org.fisco.bcos.sdk.eventsub.filter.EventSubNodeRespStatus;
import org.fisco.bcos.sdk.model.EventLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubscribeEventManager {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeEventManager.class);

    public interface SubscribeEventCallback {
        void onReceive(BigInteger blockNumber, String txId, String address, List<Object> data);
    }

    private class EventSubscribeWrapper {
        private String registerId;
        private EventSubscribe eventSubscribe;
        private SubscribeCallback callback;

        public EventSubscribeWrapper(
                String registerId, EventSubscribe eventSubscribe, SubscribeCallback callback) {
            this.registerId = registerId;
            this.eventSubscribe = eventSubscribe;
            this.callback = callback;
        }

        public String getRegisterId() {
            return this.registerId;
        }

        public EventSubscribe getEventSubscribe() {
            return this.eventSubscribe;
        }

        public SubscribeCallback getCallback() {
            return this.callback;
        }
    }

    private class SubscribeCallback implements EventCallback {

        private CryptoSuite cryptoSuite;
        private String eventName;
        private String abi;
        private SubscribeEventCallback callback;

        public SubscribeCallback(
                String eventName,
                String abi,
                CryptoSuite cryptoSuite,
                SubscribeEventCallback callback) {
            this.abi = abi;
            this.eventName = eventName;
            this.cryptoSuite = cryptoSuite;
            this.callback = callback;
        }

        @Override
        public void onReceiveLog(int status, List<EventLog> logs) {
            if (status == EventSubNodeRespStatus.PUSH_COMPLETED.getStatus()) {
                logger.info("event push completed.");
            }
            if (logs != null) {
                for (EventLog log : logs) {
                    ABICodec abiCodec = new ABICodec(this.cryptoSuite);
                    try {
                        List<Object> list = abiCodec.decodeEvent(this.abi, this.eventName, log);
                        logger.info(
                                "status in onReceiveLog : "
                                        + status
                                        + ",blockNumber:"
                                        + log.getBlockNumber()
                                        + ",txIndex:"
                                        + log.getTransactionIndex()
                                        + ",data:"
                                        + log.getData()
                                        + ",decode event log content, "
                                        + list);
                        callback.onReceive(
                                log.getBlockNumber(),
                                log.getTransactionHash(),
                                log.getAddress(),
                                list);
                    } catch (ABICodecException e) {
                        logger.error("decode event log error, " + e.getMessage());
                    }
                }
            }
        }
    }

    private Map<String, EventSubscribeWrapper> eventSubscribeInventory = new HashMap<>();

    private AbstractClientWrapper clientWrapper;

    public SubscribeEventManager(AbstractClientWrapper clientWrapper) {
        this.clientWrapper = clientWrapper;
    }

    public String addSubscribeEvent(
            String eventName,
            String abi,
            EventLogParams eventLogParams,
            SubscribeEventCallback finalCallback) {
        EventEncoder encoder = new EventEncoder(this.clientWrapper.getCryptoSuite());
        String eventSubscribeId = encoder.buildEventSignature(eventLogParams.toString());
        EventSubscribeWrapper wrapper = eventSubscribeInventory.get(eventSubscribeId);
        if (wrapper != null) {
            return wrapper.registerId;
        }
        EventSubscribe eventSubscribe =
                clientWrapper
                        .getBcosSDK()
                        .getEventSubscribe(clientWrapper.getClient().getGroupId());
        eventSubscribe.start();

        SubscribeCallback callback =
                new SubscribeCallback(
                        eventName, abi, clientWrapper.getCryptoSuite(), finalCallback);
        String registerId = eventSubscribe.subscribeEvent(eventLogParams, callback);
        eventSubscribeInventory.put(
                eventSubscribeId, new EventSubscribeWrapper(registerId, eventSubscribe, callback));
        return registerId;
    }

    private EventSubscribeWrapper getEventSubscribeWrapper(String registerId) {
        EventSubscribeWrapper wrapper = null;
        for (Map.Entry<String, EventSubscribeWrapper> entry : eventSubscribeInventory.entrySet()) {
            wrapper = entry.getValue();
            if (registerId.equals(wrapper.getRegisterId())) {
                break;
            }
        }
        return wrapper;
    }

    private void removeEventSubscribeWrapper(String registerId) {
        String eventSubscribeId = null;
        for (Map.Entry<String, EventSubscribeWrapper> entry : eventSubscribeInventory.entrySet()) {
            EventSubscribeWrapper wrapper = entry.getValue();
            if (registerId.equals(wrapper.getRegisterId())) {
                eventSubscribeId = entry.getKey();
                break;
            }
        }
        if (eventSubscribeId != null) {
            eventSubscribeInventory.remove(eventSubscribeId);
        }
    }

    public boolean removeSubscribeEvent(String registerId) {
        EventSubscribeWrapper wrapper = getEventSubscribeWrapper(registerId);
        if (wrapper == null) {
            return false;
        }
        EventSubscribe eventSubscribe = wrapper.getEventSubscribe();
        eventSubscribe.unsubscribeEvent(registerId, wrapper.getCallback());
        eventSubscribe.stop();
        removeEventSubscribeWrapper(registerId);
        return true;
    }
}
