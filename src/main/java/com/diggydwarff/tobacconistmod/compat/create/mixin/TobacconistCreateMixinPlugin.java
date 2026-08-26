package com.diggydwarff.tobacconistmod.compat.create.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/** Keeps every Create-targeted mixin completely dormant when Create is not installed. */
public final class TobacconistCreateMixinPlugin implements IMixinConfigPlugin {
    private boolean createLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        createLoaded = TobacconistCreateMixinPlugin.class.getClassLoader()
                .getResource("com/simibubi/create/Create.class") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return createLoaded;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
