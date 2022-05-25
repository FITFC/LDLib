package com.lowdragmc.lowdraglib.utils;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;

public final class ItemStackKey {

    private final ItemStack itemStack;
    private final int maxStackSize;
    private int hashCode = 0;

    public ItemStackKey(ItemStack itemStack) {
        this.itemStack = itemStack.copy();
        this.itemStack.setCount(1);
        this.hashCode = makeHashCode();
        this.maxStackSize = itemStack.getMaxStackSize();
    }

    public ItemStackKey(ItemStack itemStack, boolean doCopy) {
        this.itemStack = itemStack;
        this.maxStackSize = itemStack.getMaxStackSize();
        this.hashCode = makeHashCode();
    }

    public boolean isItemStackEqual(ItemStack itemStack) {
        return ItemStack.matches(this.itemStack, itemStack);
    }

    public ItemStack getItemStack() {
        return itemStack.copy();
    }

    public ItemStack getItemStackRaw() {
        return itemStack;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStackKey)) return false;
        ItemStackKey that = (ItemStackKey) o;
        return ItemStack.matches(itemStack, that.itemStack);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private int makeHashCode() {
        return Objects.hash(itemStack.getItem(), Items.FEATHER.getDamage(itemStack), itemStack.getTag());
    }

    @Override
    public String toString() {
        return itemStack.toString();
    }
}
