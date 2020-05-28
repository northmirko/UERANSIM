package tr.havelsan.ueransim.api;

import threegpp.milenage.MilenageResult;
import threegpp.milenage.biginteger.BigIntegerBufferFactory;
import threegpp.milenage.cipher.Ciphers;
import tr.havelsan.ueransim.SendingMessage;
import tr.havelsan.ueransim.core.SimulationContext;
import tr.havelsan.ueransim.core.UeData;
import tr.havelsan.ueransim.enums.AutnValidationRes;
import tr.havelsan.ueransim.nas.core.messages.NasMessage;
import tr.havelsan.ueransim.nas.eap.*;
import tr.havelsan.ueransim.nas.impl.enums.EMmCause;
import tr.havelsan.ueransim.nas.impl.ies.IEAuthenticationResponseParameter;
import tr.havelsan.ueransim.nas.impl.ies.IEEapMessage;
import tr.havelsan.ueransim.nas.impl.messages.AuthenticationFailure;
import tr.havelsan.ueransim.nas.impl.messages.AuthenticationRequest;
import tr.havelsan.ueransim.nas.impl.messages.AuthenticationResponse;
import tr.havelsan.ueransim.nas.impl.messages.AuthenticationResult;
import tr.havelsan.ueransim.ngap2.NgapBuilder;
import tr.havelsan.ueransim.ngap2.NgapCriticality;
import tr.havelsan.ueransim.ngap2.NgapProcedure;
import tr.havelsan.ueransim.utils.Color;
import tr.havelsan.ueransim.utils.Console;
import tr.havelsan.ueransim.utils.bits.BitString;
import tr.havelsan.ueransim.utils.octets.Octet;
import tr.havelsan.ueransim.utils.octets.OctetString;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class UeAuthentication {

    public static void handleAuthenticationRequest(SimulationContext ctx, AuthenticationRequest message) {
        if (message.eapMessage != null) {
            handleEapAkaPrime(ctx, message);
        } else {
            handle5gAka(ctx, message);
        }
    }

    private static void handleEapAkaPrime(SimulationContext ctx, AuthenticationRequest message) {
        // TODO: EAP-AKA' is incomplete

        OctetString rand, res, mac;
        Octet id;

        // Read EAP-AKA' request
        {
            var akaPrimeRequest = (EapAkaPrime) message.eapMessage.eap;
            rand = akaPrimeRequest.attributes.getAttribute(EAttributeType.AT_RAND);
            mac = akaPrimeRequest.attributes.getAttribute(EAttributeType.AT_MAC);
            id = akaPrimeRequest.id;
        }

        // Derive keys
        {
            var snn = ctx.ueData.snn;
            var sqn = ctx.ueData.sqn;
            var supi = ctx.ueData.supi;

            var milenage = calculateMilenage(ctx.ueData, rand);
            res = milenage.get(MilenageResult.RES);
            var ck = milenage.get(MilenageResult.CK);
            var ik = milenage.get(MilenageResult.IK);
            var ak = milenage.get(MilenageResult.AK);

            var sqnXorAk = OctetString.xor(sqn, ak);
            var ckPrimeIkPrime = UeKeyManagement.calculateCkPrimeIkPrime(ck, ik, snn, sqnXorAk);
            var ckPrime = ckPrimeIkPrime[0];
            var ikPrime = ckPrimeIkPrime[1];

            var kAusf = UeKeyManagement.calculateKAusfForEapAkaPrime(ckPrime, ikPrime, supi);

            ctx.nasSecurityContext.keys.rand = rand;
            ctx.nasSecurityContext.keys.res = res;
            ctx.nasSecurityContext.keys.resStar = null;
            ctx.nasSecurityContext.keys.kAusf = kAusf;
            UeKeyManagement.deriveKeysSeafAmf(ctx);
        }

        // Send Response
        {
            var akaPrimeResponse = new EapAkaPrime(Eap.ECode.RESPONSE, id);
            akaPrimeResponse.subType = ESubType.AKA_CHALLENGE;
            akaPrimeResponse.attributes = new EapAttributes();
            akaPrimeResponse.attributes.putAttribute(EAttributeType.AT_RES, res);
            akaPrimeResponse.attributes.putAttribute(EAttributeType.AT_MAC, mac);
            akaPrimeResponse.attributes.putAttribute(EAttributeType.AT_KDF, new OctetString("0001"));

            var response = new AuthenticationResponse();
            response.eapMessage = new IEEapMessage(akaPrimeResponse);

            Messaging.send(ctx, new SendingMessage(new NgapBuilder(NgapProcedure.UplinkNASTransport, NgapCriticality.IGNORE), response));
        }
    }

    private static void handle5gAka(SimulationContext ctx, AuthenticationRequest request) {
        NasMessage response = null;

        var rand = request.authParamRAND.value;
        var milenage = calculateMilenage(ctx.ueData, rand);
        var res = milenage.get(MilenageResult.RES);
        var ck = milenage.get(MilenageResult.CK);
        var ik = milenage.get(MilenageResult.IK);
        var ckik = OctetString.concat(ck, ik);
        var ak = milenage.get(MilenageResult.AK);
        var mac = milenage.get(MilenageResult.MAC_A);

        var autnCheck = UeAuthentication.validateAutn(ak, mac, request.authParamAUTN.value);
        if (autnCheck == AutnValidationRes.OK) {

            // Derive keys
            var snn = ctx.ueData.snn;
            var sqnXorAk = OctetString.xor(ctx.ueData.sqn, ak);
            ctx.nasSecurityContext.keys.rand = rand;
            ctx.nasSecurityContext.keys.res = res;
            ctx.nasSecurityContext.keys.resStar = UeKeyManagement.calculateResStar(ckik, snn, rand, res);
            ctx.nasSecurityContext.keys.kAusf = UeKeyManagement.calculateKAusfFor5gAka(ck, ik, snn, sqnXorAk);
            UeKeyManagement.deriveKeysSeafAmf(ctx);

            // Prepare response
            response = new AuthenticationResponse(
                    new IEAuthenticationResponseParameter(ctx.nasSecurityContext.keys.resStar), null);

        } else if (autnCheck == AutnValidationRes.MAC_FAILURE) {
            // todo
            Console.println(Color.YELLOW, "MAC_FAILURE case not implemented yet in AUTN validation");
        } else if (autnCheck == AutnValidationRes.SYNCHRONISATION_FAILURE) {
            // todo
            Console.println(Color.YELLOW, "SYNCHRONISATION_FAILURE case not implemented yet in AUTN validation");
        } else {
            // Other errors
            response = new AuthenticationFailure(EMmCause.UNSPECIFIED_PROTOCOL_ERROR);
        }

        if (response != null) {
            Messaging.send(ctx, new SendingMessage(new NgapBuilder(NgapProcedure.UplinkNASTransport, NgapCriticality.IGNORE), response));
        }
    }

    private static AutnValidationRes validateAutn(OctetString ak, OctetString mac, OctetString autn) {
        // Decode AUTN
        var receivedSQNxorAK = autn.substring(0, 6);
        var receivedSQN = OctetString.xor(receivedSQNxorAK, ak);
        var receivedAMF = autn.substring(6, 2);
        var receivedMAC = autn.substring(8, 8);

        // Check MAC
        if (!receivedMAC.equals(mac)) {
            return AutnValidationRes.MAC_FAILURE;
        }

        // TS 33.501: An ME accessing 5G shall check during authentication that the "separation bit" in the AMF field
        // of AUTN is set to 1. The "separation bit" is bit 0 of the AMF field of AUTN.
        if (!BitString.from(receivedAMF).getB(0)) {
            return AutnValidationRes.AMF_SEPARATION_BIT_FAILURE;
        }

        // Verify that the received sequence number SQN is in the correct range
        if (!checkSqn(receivedSQN)) {
            return AutnValidationRes.SYNCHRONISATION_FAILURE;
        }

        return AutnValidationRes.OK;
    }

    private static Map<MilenageResult, OctetString> calculateMilenage(UeData ueData, OctetString rand) {
        var factory = BigIntegerBufferFactory.getInstance();
        var cipher = Ciphers.createRijndaelCipher(ueData.key.toByteArray());
        byte[] opc = threegpp.milenage.Milenage.calculateOPc(ueData.op.toByteArray(), cipher, factory);
        var milenage = new threegpp.milenage.Milenage<>(opc, cipher, factory);
        try {
            var calc = milenage.calculateAll(rand.toByteArray(), ueData.sqn.toByteArray(), ueData.amf.toByteArray(), Executors.newCachedThreadPool());
            var res = new HashMap<MilenageResult, OctetString>();
            for (var entry : calc.entrySet()) {
                res.put(entry.getKey(), new OctetString(entry.getValue()));
            }
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean checkSqn(OctetString sqn) {
        // TODO:
        //  Verify the freshness of sequence numbers to determine whether the specified sequence number is
        //  in the correct range and acceptable by the USIM. See 3GPP TS 33.102, Annex C.2.
        return true;
    }

    public static void handleAuthenticationResult(SimulationContext ctx, AuthenticationResult message) {
        Console.println(Color.BLUE, "Authentication result received");
    }
}
