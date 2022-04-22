package com.lowdragmc.lowdraglib.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * BlockInfo represents immutable information for com.lowdragmc.lowdraglib.test.block in world
 * This includes com.lowdragmc.lowdraglib.test.block state and tile entity, and needed for complete representation
 * of some complex blocks like machines, when rendering or manipulating them without world instance
 */
public class BlockInfo {
    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);

    private final BlockState blockState;
    private final TileEntity tileEntity;
    private final ItemStack itemStack;

    public BlockInfo(Block block) {
        this(block.defaultBlockState());
    }

    public BlockInfo(BlockState blockState) {
        this(blockState, null);
    }

    public BlockInfo(BlockState blockState, TileEntity tileEntity) {
        this(blockState, tileEntity, null);
    }

    public BlockInfo(BlockState blockState, TileEntity tileEntity, ItemStack itemStack) {
        this.blockState = blockState;
        this.tileEntity = tileEntity;
        this.itemStack = itemStack;
    }

    public static BlockInfo fromBlockState(BlockState state) {
        try {
            if (state.getBlock().hasTileEntity(state)) {
                TileEntity tileEntity = state.getBlock().createTileEntity(state, new TrackedDummyWorld());
                if (tileEntity != null) {
                    return new BlockInfo(state, tileEntity);
                }
            }
        } catch (Exception ignored){ }
        return new BlockInfo(state);
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public ItemStack getItemStackForm() {
        return itemStack == null ? new ItemStack(blockState.getBlock()) : itemStack;
    }

    public void apply(World world, BlockPos pos) {
        world.setBlockAndUpdate(pos, blockState);
        if (tileEntity != null) {
            world.setBlockEntity(pos, tileEntity);
        }
    }
}
