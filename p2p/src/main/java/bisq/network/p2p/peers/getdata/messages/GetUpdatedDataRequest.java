/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.network.p2p.peers.getdata.messages;

import bisq.network.p2p.NodeAddress;
import bisq.network.p2p.SendersNodeAddressMessage;

import bisq.common.app.Version;
import bisq.common.proto.ProtoUtil;

import protobuf.NetworkEnvelope;

import com.google.protobuf.ByteString;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Value
public final class GetUpdatedDataRequest extends GetDataRequest implements SendersNodeAddressMessage {
    private final NodeAddress senderNodeAddress;

    public GetUpdatedDataRequest(NodeAddress senderNodeAddress,
                                 int nonce,
                                 Set<byte[]> excludedKeys) {
        this(senderNodeAddress,
                nonce,
                excludedKeys,
                Version.getP2PMessageVersion());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    private GetUpdatedDataRequest(NodeAddress senderNodeAddress,
                                  int nonce,
                                  Set<byte[]> excludedKeys,
                                  int messageVersion) {
        super(messageVersion,
                nonce,
                excludedKeys);
        checkNotNull(senderNodeAddress, "senderNodeAddress must not be null at GetUpdatedDataRequest");
        this.senderNodeAddress = senderNodeAddress;
    }

    @Override
    public protobuf.NetworkEnvelope toProtoNetworkEnvelope() {
        final protobuf.GetUpdatedDataRequest.Builder builder = protobuf.GetUpdatedDataRequest.newBuilder()
                .setSenderNodeAddress(senderNodeAddress.toProtoMessage())
                .setNonce(nonce)
                .addAllExcludedKeys(excludedKeys.stream()
                        .map(ByteString::copyFrom)
                        .collect(Collectors.toList()));

        NetworkEnvelope proto = getNetworkEnvelopeBuilder()
                .setGetUpdatedDataRequest(builder)
                .build();
        log.info("Sending a GetUpdatedDataRequest with {} kB", proto.getSerializedSize() / 1000d);
        return proto;
    }

    public static GetUpdatedDataRequest fromProto(protobuf.GetUpdatedDataRequest proto, int messageVersion) {
        log.info("Received a GetUpdatedDataRequest with {} kB", proto.getSerializedSize() / 1000d);
        return new GetUpdatedDataRequest(NodeAddress.fromProto(proto.getSenderNodeAddress()),
                proto.getNonce(),
                ProtoUtil.byteSetFromProtoByteStringList(proto.getExcludedKeysList()),
                messageVersion);
    }
}
