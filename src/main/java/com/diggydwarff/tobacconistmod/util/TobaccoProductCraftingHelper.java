package com.diggydwarff.tobacconistmod.util;

import com.diggydwarff.tobacconistmod.block.entity.TobaccoBarrelBlockEntity;
import com.diggydwarff.tobacconistmod.datagen.items.ModItems;
import com.diggydwarff.tobacconistmod.datagen.items.custom.TobaccoLeafItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Shared manual/Create product construction so automation produces identical metadata. */
public final class TobaccoProductCraftingHelper {
    private TobaccoProductCraftingHelper() {}

    public static ItemStack makeCigarette(ItemStack tobaccoStack) {
        if (tobaccoStack.isEmpty() || !TobaccoCuringHelper.isLooseTobacco(tobaccoStack)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(ModItems.CIGARETTE.get());

        // Apply the packed tobacco snapshot first, then reacquire the live custom-data tag.
        // LegacyItemTags uses copy-on-write LiveTag wrappers on 1.21; keeping an older LiveTag
        // across applyTobaccoMetadata() can overwrite PackedTobaccoData when it commits later.
        TobaccoDataHelper.applyTobaccoMetadata(result, tobaccoStack);
        CompoundTag tag = LegacyItemTags.getOrCreateTag(result);
        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));

        // PackedTobaccoData keeps the complete filler snapshot, but finished products also
        // need process state on their root tag because tooltip/smoking logic reads these
        // values directly from the finished cigarette.
        CompoundTag sourceTag = LegacyItemTags.getTag(tobaccoStack);
        if (sourceTag != null) {
            int agedDays = sourceTag.getInt("AgedDays");
            if (agedDays > 0) tag.putInt("AgedDays", agedDays);
            else tag.remove("AgedDays");

            if (TobaccoBarrelBlockEntity.isFermented(tobaccoStack)) tag.putBoolean("Fermented", true);
            else tag.remove("Fermented");

            if (TobaccoBarrelBlockEntity.isRuined(tobaccoStack)) tag.putBoolean("Ruined", true);
            else tag.remove("Ruined");
        }

        TobaccoProductQualityHelper.applyProductQualityToTag(
                tag,
                tobaccoStack,
                TobaccoProductQualityHelper.getCigaretteQuality(tobaccoStack)
        );
        return result;
    }

    public static ItemStack makeCigar(ItemStack tobaccoStack, ItemStack wrapperStack) {
        if (tobaccoStack.isEmpty()
                || wrapperStack.isEmpty()
                || !TobaccoCuringHelper.isLooseTobacco(tobaccoStack)
                || !(wrapperStack.getItem() instanceof TobaccoLeafItem)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(ModItems.CIGAR.get());

        // Same copy-on-write rule as cigarettes: preserve the complete filler snapshot before
        // adding cigar-specific fields to the result tag.
        TobaccoDataHelper.applyTobaccoMetadata(result, tobaccoStack);
        CompoundTag tag = LegacyItemTags.getOrCreateTag(result);

        CompoundTag wrapperData = LegacyItemTags.getTag(wrapperStack);
        if (wrapperData != null) {
            tag.put("WrapperLeafData", wrapperData.copy());
        }

        tag.putString("tobacco", TobaccoProductQualityHelper.getShortTobaccoLabel(tobaccoStack));
        tag.putString("wrapper", wrapperStack.getDisplayName().getString());

        CompoundTag fillerTag = LegacyItemTags.getTag(tobaccoStack);
        CompoundTag wrapperTag = LegacyItemTags.getTag(wrapperStack);

        int fillerAge = fillerTag != null ? fillerTag.getInt("AgedDays") : 0;
        int wrapperAge = wrapperTag != null ? wrapperTag.getInt("AgedDays") : 0;
        int finalAge = Math.max(fillerAge, wrapperAge);
        if (finalAge > 0) tag.putInt("AgedDays", finalAge);

        boolean fermented = TobaccoBarrelBlockEntity.isFermented(tobaccoStack)
                || TobaccoBarrelBlockEntity.isFermented(wrapperStack);
        if (fermented) tag.putBoolean("Fermented", true);

        boolean ruined = TobaccoBarrelBlockEntity.isRuined(tobaccoStack)
                || TobaccoBarrelBlockEntity.isRuined(wrapperStack);
        if (ruined) tag.putBoolean("Ruined", true);

        TobaccoProductQualityHelper.applyProductQualityToTag(
                tag,
                tobaccoStack,
                TobaccoProductQualityHelper.getCigarQuality(tobaccoStack)
        );
        return result;
    }
}
