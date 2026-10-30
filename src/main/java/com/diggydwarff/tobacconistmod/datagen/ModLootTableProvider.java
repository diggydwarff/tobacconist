package com.diggydwarff.tobacconistmod.datagen;

import com.diggydwarff.tobacconistmod.datagen.loot.ModBlockLootTables;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModLootTableProvider extends LootTableProvider {
    private ModLootTableProvider(DataGenerator generator) { super(generator); }
    public static LootTableProvider create(DataGenerator output) {
        return new ModLootTableProvider(output);
    }
    @Override protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return List.of(Pair.of(ModBlockLootTables::new, LootContextParamSets.BLOCK));
    }
    @Override protected void validate(Map<ResourceLocation, LootTable> tables, ValidationContext context) {
        tables.forEach((id, table) -> LootTables.validate(context, id, table));
    }
}
