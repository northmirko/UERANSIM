package com.runsim.backend.nas.impl.messages;

import com.runsim.backend.nas.core.IMessageBuilder;
import com.runsim.backend.nas.core.messages.PlainSmMessage;
import com.runsim.backend.nas.impl.enums.EMessageType;
import com.runsim.backend.nas.impl.ies.*;

public class PduSessionModificationRequest extends PlainSmMessage {
    public IE5gSmCapability smCapability;
    public IE5gSmCause smCause;
    public IEMaximumNumberOfSupportedPacketFilters maximumNumberOfSupportedPacketFilters;
    public IEAlwaysOnPduSessionRequested alwaysOnPduSessionRequested;
    public IEIntegrityProtectionMaximumDataRate integrityProtectionMaximumDataRate;
    public IEQoSRules requestedQosRules;
    public IEQoSFlowDescriptions requestedQosFlowDescriptions;
    public IEMappedEpsBearerContexts mappedEpsBearerContexts;
    public IEExtendedProtocolConfigurationOptions extendedProtocolConfigurationOptions;

    public PduSessionModificationRequest() {
        super(EMessageType.PDU_SESSION_MODIFICATION_REQUEST);
    }

    @Override
    public void build(IMessageBuilder builder) {
        super.build(builder);

        builder.optionalIE(0x28, "smCapability");
        builder.optionalIE(0x59, "smCause");
        builder.optionalIE(0x55, "maximumNumberOfSupportedPacketFilters");
        builder.optionalIE1(0xB, "alwaysOnPduSessionRequested");
        builder.optionalIE(0x13, "integrityProtectionMaximumDataRate");
        builder.optionalIE(0x7A, "requestedQosRules");
        builder.optionalIE(0x79, "requestedQosFlowDescriptions");
        builder.optionalIE(0x7F, "mappedEpsBearerContexts");
        builder.optionalIE(0x7B, "extendedProtocolConfigurationOptions");
    }
}
