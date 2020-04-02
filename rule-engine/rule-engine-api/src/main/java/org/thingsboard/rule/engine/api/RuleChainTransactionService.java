package org.thingsboard.rule.engine.api;

import org.thingsboard.server.common.msg.TbMsg;
import org.thingsboard.server.common.msg.cluster.ServerAddress;

import java.util.function.Consumer;

public interface RuleChainTransactionService {

    void beginTransaction(TbMsg msg, Consumer<TbMsg> onStart, Consumer<TbMsg> onEnd, Consumer<Throwable> onFailure);

    void endTransaction(TbMsg msg, Consumer<TbMsg> onSuccess, Consumer<Throwable> onFailure);

    void onRemoteTransactionMsg(ServerAddress serverAddress, byte[] bytes);

}
