package com.diggydwarff.tobacconistmod.datagen.items.custom;

public class UpgradedHookahHoseItem extends HookahHoseItem {
    public UpgradedHookahHoseItem(Properties properties) {
        super(properties);
    }

    @Override
    protected String hoseTooltipKey() {
        return "tobacconistmod.tooltip.hose.upgraded";
    }
}
