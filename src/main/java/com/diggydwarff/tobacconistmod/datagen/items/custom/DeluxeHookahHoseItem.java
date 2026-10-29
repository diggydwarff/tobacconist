package com.diggydwarff.tobacconistmod.datagen.items.custom;

public class DeluxeHookahHoseItem extends HookahHoseItem {
    public DeluxeHookahHoseItem(Properties properties) {
        super(properties);
    }

    @Override
    protected String hoseTooltipKey() {
        return "tobacconistmod.tooltip.hose.deluxe";
    }
}
