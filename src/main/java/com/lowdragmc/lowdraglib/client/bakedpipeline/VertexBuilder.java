package com.lowdragmc.lowdraglib.client.bakedpipeline;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VertexBuilder implements IVertexConsumer {
    public final VertexFormat vertexFormat;
    public TextureAtlasSprite sprite;
    public int quadTint = -1;
    public Direction quadOrientation;
    public boolean applyDiffuseLighting;
    public final ListMultimap<VertexFormatElement, float[]> data;

    public VertexBuilder(VertexFormat vertexFormat, TextureAtlasSprite sprite){
        this.vertexFormat = vertexFormat;
        this.sprite = sprite;
        this.data = MultimapBuilder.hashKeys(vertexFormat.getElements().size()).arrayListValues().build();
    }

    @Override
    public void put(int element, @Nullable float... data) {
        if (data == null) return;
        float[] copy = new float[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        VertexFormatElement ele = vertexFormat.getElements().get(element);
        this.data.put(ele, copy);
    }

    @Override
    @Nonnull
    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }

    @Override
    public void setQuadTint(int tint) {
        quadTint = tint;
    }

    @Override
    public void setQuadOrientation(@Nonnull Direction orientation) {
        this.quadOrientation = orientation;
    }

    @Override
    public void setApplyDiffuseLighting(boolean diffuse) {
        this.applyDiffuseLighting = diffuse;
    }

    @Override
    public void setTexture(@Nullable TextureAtlasSprite texture) {
        this.sprite = texture;
    }
}
