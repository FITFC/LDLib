package com.lowdragmc.lowdraglib.syncdata.blockentity;

import com.lowdragmc.lowdraglib.networking.LDLNetworking;
import com.lowdragmc.lowdraglib.networking.both.PacketRPCMethodPayload;
import com.lowdragmc.lowdraglib.syncdata.field.RPCMethodMeta;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public interface IRPCBlockEntity extends IManagedBlockEntity {

    /**
     * Get the RPC method
     */
    @Nullable
    default RPCMethodMeta getRPCMethod(String methodName) {
        return getFieldHolder().getRpcMethodMap().get(methodName);
    }

    default PacketRPCMethodPayload generateRpcPacket(String methodName, Object... args) {
        return PacketRPCMethodPayload.of(this, methodName,args);
    }

    @OnlyIn(Dist.CLIENT)
    default void rpcToServer(String methodName, Object... args) {
        var packet = generateRpcPacket(methodName, args);
        LDLNetworking.NETWORK.sendToServer(packet);
    }


    default void rpcToPlayer(ServerPlayer player, String methodName, Object... args) {
        var packet = generateRpcPacket(methodName, args);
        LDLNetworking.NETWORK.sendToPlayer(packet, player);
    }

    default void rpcToTracking(ServerPlayer player, String methodName, Object... args) {
        var packet = generateRpcPacket(methodName, args);
        LDLNetworking.NETWORK.sendToTrackingChunk(getSelf().getLevel().getChunkAt(getSelf().getBlockPos()), packet);
    }

}
