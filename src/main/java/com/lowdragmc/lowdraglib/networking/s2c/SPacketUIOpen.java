package com.lowdragmc.lowdraglib.networking.s2c;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.networking.IPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

@NoArgsConstructor
public class SPacketUIOpen implements IPacket {
    private int uiFactoryId;
    private FriendlyByteBuf serializedHolder;
    private int windowId;

    public SPacketUIOpen(int uiFactoryId, FriendlyByteBuf serializedHolder, int windowId) {
        this.uiFactoryId = uiFactoryId;
        this.serializedHolder = serializedHolder;
        this.windowId = windowId;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(serializedHolder.readableBytes());
        buf.writeBytes(serializedHolder);

        buf.writeVarInt(uiFactoryId);
        buf.writeVarInt(windowId);
    }

    @Override
    public void decode(FriendlyByteBuf buf) {
        ByteBuf directSliceBuffer = buf.readBytes(buf.readVarInt());
        ByteBuf copiedDataBuffer = Unpooled.copiedBuffer(directSliceBuffer);
        directSliceBuffer.release();
        this.serializedHolder = new FriendlyByteBuf(copiedDataBuffer);

        this.uiFactoryId = buf.readVarInt();
        this.windowId = buf.readVarInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void execute(NetworkEvent.Context handler) {
        UIFactory<?> uiFactory = UIFactory.FACTORIES.get(uiFactoryId);
        if (uiFactory != null) {
            uiFactory.initClientUI(serializedHolder, windowId);
        }
    }
}
