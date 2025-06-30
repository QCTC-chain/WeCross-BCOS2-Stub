package com.webank.wecross.stub.bcos.custom;

import com.webank.wecross.stub.*;
import com.webank.wecross.stub.bcos.AsyncCnsService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterExistingContractHandler implements CommandHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(RegisterExistingContractHandler.class);
    private AsyncCnsService asyncCnsService;

    public void setAsyncCnsService(AsyncCnsService asyncCnsService) {
        this.asyncCnsService = asyncCnsService;
    }

    @Override
    public void handle(
            Path path,
            Object[] args,
            Account account,
            BlockManager blockManager,
            Connection connection,
            Driver.CustomCommandCallback callback,
            CryptoSuite cryptoSuite) {
        String abi = (String) args[1];
        String address = (String) args[2];
        String version = (String) args[3];

        if (abi == null || abi.isEmpty()) {
            callback.onResponse(new Exception("请提供合约 abi"), null);
            return;
        }

        if (address == null || address.isEmpty()) {
            callback.onResponse(new Exception("请提供合约地址"), null);
            return;
        }

        if (version == null || version.isEmpty()) {
            version = "1.0.0";
        }

        String finalVersion = version;
        asyncCnsService.registerCNSByProxy(
                path,
                address,
                version,
                abi,
                account,
                blockManager,
                connection,
                e -> {
                    boolean isSuccess = false;
                    if (Objects.nonNull(e)) {
                        if (e.getMessage()
                                .contains("The contract name and version already exist")) {
                            isSuccess = true;
                        } else {
                            isSuccess = false;
                        }
                    }

                    if (!isSuccess) {
                        logger.warn("registering abi failed", e);
                        callback.onResponse(e, null);
                        return;
                    }

                    logger.info(
                            " register cns successfully, contractName: {}, version: {}, address: {}, abi: {}",
                            path.getResource(),
                            finalVersion,
                            address,
                            abi);

                    Map<String, Object> data = new HashMap<>();
                    data.put("CONTRACT_NAME", path.getResource());
                    data.put("CONTRACT_ADDRESS", address);
                    data.put("CONTRACT_VERSION", "v1.0.0");
                    data.put("CONTRACT_RUNTIME_TYPE", "EVM");
                    callback.onResponse(null, data);
                });
    }
}
