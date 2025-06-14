package ca.uwaterloo.crysp.privacyguard.Application.Network.Protocol.IP;

import ca.uwaterloo.crysp.privacyguard.Application.Network.Protocol.TCP.TCPDatagram;
import ca.uwaterloo.crysp.privacyguard.Application.Network.Protocol.UDP.UDPDatagram;
import ca.uwaterloo.crysp.privacyguard.Utilities.ByteOperations;
import ca.uwaterloo.crysp.privacyguard.Application.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by frank on 2014-03-26.
 */

public class IPDatagram {
    public final static String TAG = "IPDatagram";
    public static final int TCP = 6, UDP = 17;
    IPHeader header;
    IPPayLoad data;

    public IPDatagram(IPHeader header, IPPayLoad data) {
        this.header = header;
        this.data = data;
        int totalLength = header.headerLength() + data.length();
        if (this.header.length() != totalLength) {
            this.header.setLength(totalLength);
            this.header.setCheckSum(new byte[]{0, 0});
            byte[] toComputeCheckSum = this.header.toByteArray();
            this.header.setCheckSum(ByteOperations.computeCheckSum(toComputeCheckSum));
        }
    }

    public static IPDatagram create(ByteBuffer packet) {
        IPHeader header = IPHeader.create(packet.array());
        IPPayLoad payLoad;
        if (header.protocol() == TCP) {
            payLoad = TCPDatagram.create(packet.array(), header.headerLength(), packet.limit(), header.getDstAddress());
        } else if (header.protocol() == UDP) {
            payLoad = UDPDatagram.create(Arrays.copyOfRange(packet.array(), header.headerLength(), packet.limit()));
        } else return null;
        return new IPDatagram(header, payLoad);
    }

    public IPHeader header() {
        return header;
    }

    public IPPayLoad payLoad() {
        return data;
    }

    public byte[] toByteArray() {
        return ByteOperations.concatenate(header.toByteArray(), data.toByteArray());
    }

    public void debugInfo() {
        Logger.d(TAG, "SrcAddr=" + header.getSrcAddress() + " DstAddr=" + header.getDstAddress());
    }

    public String headerToString()
    {
        String sb = "SrcAddr=" + header.getSrcAddress() +
                " DstAddr=" +
                header.getDstAddress() +
                " ";
        //if (payLoad() instanceof TCPDatagram) {
        //    sb.append(((TCPDatagram)payLoad()).debugString());
        //}
        //if (payLoad() instanceof UDPDatagram) {
        //    sb.append(((UDPDatagram)payLoad()).debugString());
        //}
        return sb;
    }
}