package com.runsim.backend.nas.impl.ies;

import com.runsim.backend.nas.Decoder;
import com.runsim.backend.nas.impl.enums.*;
import com.runsim.backend.nas.impl.values.VHomeNetworkPki;
import com.runsim.backend.utils.OctetInputStream;

public class IEImsiMobileIdentity extends IESuciMobileIdentity {
    public EMobileCountryCode mobileCountryCode;
    public EMobileNetworkCode mobileNetworkCode;
    public String routingIndicator;
    public EProtectionSchemeIdentifier protectionSchemaId;
    public VHomeNetworkPki homeNetworkPublicKeyIdentifier;
    public String schemaOutput;

    public IEImsiMobileIdentity decodeIMSI(OctetInputStream stream, int length) {
        var result = new IEImsiMobileIdentity();

        /* Decode MCC */
        int octet1 = stream.readOctetI();
        int mcc1 = octet1 & 0b1111;
        int mcc2 = (octet1 >> 4) & 0b1111;
        int octet2 = stream.readOctetI();
        int mcc3 = octet2 & 0b1111;
        int mcc = 100 * mcc1 + 10 * mcc2 + mcc3;
        result.mobileCountryCode = EMobileCountryCode.fromValue(mcc);

        /* Decode MNC */
        int mnc3 = (octet2 >> 4) & 0b1111;
        int octet3 = stream.readOctetI();
        int mnc2 = octet3 & 0b1111;
        int mnc1 = (octet3 >> 4) & 0b1111;
        int mnc = 10 * mnc1 + mnc2;
        boolean longMnc = false;
        if ((mnc3 != 0xf) || (octet1 == 0xff && octet2 == 0xff && octet3 == 0xff)) {
            longMnc = true;
            mnc = 10 * mnc + mnc3;
        }
        if (longMnc) {
            result.mobileNetworkCode = EMobileNetworkCode3.fromValue(mcc * 1000 + mnc);
        } else {
            result.mobileNetworkCode = EMobileNetworkCode2.fromValue(mcc * 100 + mnc);
        }

        /* Decode routing indicator */
        int riLen = stream.peekOctetI(1) == 0xFF ? 1 : 2;
        result.routingIndicator = Decoder.bcdString(stream, riLen, false);
        if (riLen == 1) stream.readOctet();

        /* Decode protection schema id */
        result.protectionSchemaId = EProtectionSchemeIdentifier.fromValue(stream.readOctetI() & 0b1111);

        /* Decode home network public key identifier */
        result.homeNetworkPublicKeyIdentifier = Decoder.nasValue(stream, VHomeNetworkPki.class);

        /* Decode schema output */
        String schemaOutput;
        if (result.protectionSchemaId.equals(EProtectionSchemeIdentifier.NULL_SCHEMA)) {
            result.schemaOutput = Decoder.bcdString(stream, length - 7, false);
        } else {
            var range = stream.readOctetString(length - 7);
            result.schemaOutput = range.toHexString();
        }

        return result;
    }
}