package com.lowdragmc.lowdraglib.gui.widget;


import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SwitchWidget extends Widget {

    protected IGuiTexture baseTexture;
    protected IGuiTexture pressedTexture;
    protected IGuiTexture hoverTexture;
    protected boolean isPressed;
    protected BiConsumer<ClickData, Boolean> onPressCallback;
    protected Supplier<Boolean> supplier;

    public SwitchWidget(int xPosition, int yPosition, int width, int height, BiConsumer<ClickData, Boolean> onPressed) {
        super(xPosition, yPosition, width, height);
        this.onPressCallback = onPressed;
    }

    public void setOnPressCallback(BiConsumer<ClickData, Boolean> onPressCallback) {
        this.onPressCallback = onPressCallback;
    }

    public SwitchWidget setTexture(IGuiTexture baseTexture, IGuiTexture pressedTexture) {
        setBaseTexture(baseTexture);
        setPressedTexture(pressedTexture);
        return this;
    }

    public SwitchWidget setBaseTexture(IGuiTexture... baseTexture) {
        this.baseTexture = new GuiTextureGroup(baseTexture);
        return this;
    }

    public SwitchWidget setPressedTexture(IGuiTexture... pressedTexture) {
        this.pressedTexture = new GuiTextureGroup(pressedTexture);
        return this;
    }

    public SwitchWidget setHoverTexture(IGuiTexture... hoverTexture) {
        this.hoverTexture = new GuiTextureGroup(hoverTexture);
        return this;
    }

    public SwitchWidget setHoverBorderTexture(int border, int color) {
        this.hoverTexture = new ColorBorderTexture(border, color);
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        if (baseTexture != null) {
            baseTexture.updateTick();
        }
        if (pressedTexture != null) {
            pressedTexture.updateTick();
        }
        if (hoverTexture != null) {
            hoverTexture.updateTick();

        }
        if (isClientSideWidget && supplier != null) {
            setPressed(supplier.get());
        }
    }

    @Override
    public void writeInitialData(PacketBuffer buffer) {
        buffer.writeBoolean(isPressed);
    }

    @Override
    public void readInitialData(PacketBuffer buffer) {
        isPressed = buffer.readBoolean();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!isClientSideWidget && supplier != null) {
            setPressed(supplier.get());
        }
    }

    public boolean isPressed() {
        return isPressed;
    }

    public SwitchWidget setPressed(boolean isPressed) {
        if (this.isPressed == isPressed) return this;
        this.isPressed = isPressed;
        if (gui == null) return this;
        if (isRemote()) {
            writeClientAction(2, buffer -> buffer.writeBoolean(isPressed));
        } else {
            writeUpdateInfo(2, buffer -> buffer.writeBoolean(isPressed));
        }
        return this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        Size size = getSize();
        if (baseTexture != null && !isPressed) {
            baseTexture.draw(matrixStack, mouseX, mouseY, position.x, position.y, size.width, size.height);

        } else if (pressedTexture != null && isPressed) {
            pressedTexture.draw(matrixStack, mouseX, mouseY, position.x, position.y, size.width, size.height);

        }
        if (isMouseOverElement(mouseX, mouseY) && hoverTexture != null) {
            hoverTexture.draw(matrixStack, mouseX, mouseY, position.x, position.y, size.width, size.height);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData();
            isPressed = !isPressed;
            writeClientAction(1, buffer -> {
                clickData.writeToBuf(buffer);
                buffer.writeBoolean(isPressed);
            });
            if (onPressCallback != null) {
                onPressCallback.accept(clickData, isPressed);
            }
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            if (onPressCallback != null) {
                onPressCallback.accept(ClickData.readFromBuf(buffer), isPressed = buffer.readBoolean());
            }
        } else if (id == 2) {
            isPressed = buffer.readBoolean();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        if (id == 2) {
            isPressed= buffer.readBoolean();
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    public SwitchWidget setSupplier(Supplier<Boolean> supplier) {
        this.supplier = supplier;
        return this;
    }
}
