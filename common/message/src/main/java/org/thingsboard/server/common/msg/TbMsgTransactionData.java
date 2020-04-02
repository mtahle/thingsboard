package org.thingsboard.server.common.msg;

import lombok.Data;
import org.thingsboard.server.common.data.id.EntityId;

import java.io.Serializable;
import java.util.UUID;

@Data
public final class TbMsgTransactionData implements Serializable {

    private final UUID transactionId;
    private final EntityId originatorId;

}
