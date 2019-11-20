package com.runsim.backend.nas.impl.messages;

import com.runsim.backend.nas.core.IMessageBuilder;
import com.runsim.backend.nas.core.messages.PlainMmMessage;
import com.runsim.backend.nas.impl.ies.IE5gMmCause;
import com.runsim.backend.nas.impl.ies.IEEapMessage;
import com.runsim.backend.nas.impl.ies.IEGprsTimer2;
import com.runsim.backend.nas.impl.ies.IEPduSessionStatus;

public class ServiceReject extends PlainMmMessage {
    public IE5gMmCause mmCause;
    public IEPduSessionStatus pduSessionStatus;
    public IEGprsTimer2 t3346Value;
    public IEEapMessage eapMessage;

    @Override
    public void build(IMessageBuilder builder) {
        super.build(builder);

        builder.mandatoryIE("mmCause");
        builder.optionalIE(0x50, "pduSessionStatus");
        builder.optionalIE(0x5f, "t3346Value");
        builder.optionalIE(0x78, "eapMessage");
    }
}