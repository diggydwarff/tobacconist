package com.diggydwarff.tobacconistmod.block.entity;

import com.diggydwarff.tobacconistmod.util.TobaccoCrateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Stores the exact nine tobacco stacks used to craft a placed crate. */
public class TobaccoCrateBlockEntity extends BlockEntity {
    private final List<ItemStack> contents = new ArrayList<>(TobaccoCrateHelper.CAPACITY);
    private boolean suppressDrops = false;

    public TobaccoCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOBACCO_CRATE.get(), pos, state);
    }

    public boolean hasContents() {
        return !contents.isEmpty();
    }

    public void loadFromCrateItem(ItemStack crateItem) {
        contents.clear();
        for (ItemStack stack : TobaccoCrateHelper.readContents(crateItem)) {
            contents.add(stack.copy());
        }
        setChanged();
    }

    public void suppressDrops() {
        suppressDrops = true;
        contents.clear();
        setChanged();
    }

    public boolean shouldSuppressDrops() {
        return suppressDrops;
    }

    public void dropContents(Level level, BlockPos pos) {
        if (suppressDrops) return;
        for (ItemStack stack : contents) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack.copy());
            }
        }
        contents.clear();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ItemStack stack : contents) {
            if (!stack.isEmpty()) {
                list.add(stack.save(new CompoundTag()));
            }
        }
        tag.put(TobaccoCrateHelper.TAG_CONTENTS, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        contents.clear();
        if (tag.contains(TobaccoCrateHelper.TAG_CONTENTS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TobaccoCrateHelper.TAG_CONTENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.of(list.getCompound(i));
                if (!stack.isEmpty()) contents.add(stack);
            }
        }
        suppressDrops = false;
    }
}
