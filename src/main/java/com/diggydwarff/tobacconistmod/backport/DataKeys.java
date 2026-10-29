package com.diggydwarff.tobacconistmod.backport;

import java.util.function.Supplier;
import net.minecraftforge.eventbus.api.IEventBus;

/** Assigns stable namespaced NBT keys to common item/fluid fields. */
public final class DataKeys {
    private final String namespace;
    private DataKeys(String namespace) { this.namespace = namespace; }
    public static DataKeys create(String namespace) { return new DataKeys(namespace); }
    public <T> Supplier<DataComponentType<T>> register(String name, Supplier<DataComponentType<T>> factory) {
        DataComponentType<T> key = factory.get();
        key.name = namespace + ":" + name;
        return () -> key;
    }
    public void register(IEventBus unused) { /* Forge saves and syncs the item's normal NBT. */ }
}
