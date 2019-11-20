package com.runsim.backend.nas.impl.messages;

import com.runsim.backend.nas.core.IMessageBuilder;
import com.runsim.backend.nas.core.messages.PlainMmMessage;
import com.runsim.backend.nas.impl.ies.*;

public class ServiceRequest extends PlainMmMessage {
    public IENasKeySetIdentifier ngKSI;
    public IEServiceType serviceType;
    public IE5gsMobileIdentity tmsi;
    public IEUplinkDataStatus uplinkDataStatus;
    public IEPduSessionStatus pduSessionStatus;
    public IEAllowedPduSessionStatus allowedPduSessionStatus;
    public IENasMessageContainer nasMessageContainer;

    @Override
    public void build(IMessageBuilder builder) {
        super.build(builder);

        builder.mandatoryIE1("serviceType", "ngKSI");
        builder.mandatoryIE("tmsi");

        builder.optionalIE(0x40, "uplinkDataStatus");
        builder.optionalIE(0x50, "pduSessionStatus");
        builder.optionalIE(0x25, "allowedPduSessionStatus");
        builder.optionalIE(0x71, "nasMessageContainer");
    }
}