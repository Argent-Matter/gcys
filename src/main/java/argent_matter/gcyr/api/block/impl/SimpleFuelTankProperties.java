package argent_matter.gcyr.api.block.impl;

import argent_matter.gcyr.api.block.IFuelTankProperties;
import lombok.Getter;

public class SimpleFuelTankProperties implements IFuelTankProperties {

    @Getter
    private final String serializedName;
    @Getter
    private final int tier;
    @Getter
    private final int fuelStorage;

    public SimpleFuelTankProperties(String name, int tier, int fuelStorage) {
        this.tier = tier;
        this.fuelStorage = fuelStorage;
        this.serializedName = name;
    }
}
