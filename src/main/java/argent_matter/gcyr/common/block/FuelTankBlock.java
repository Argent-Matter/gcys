package argent_matter.gcyr.common.block;

import argent_matter.gcyr.api.block.IFuelTankProperties;
import argent_matter.gcyr.api.block.IRocketPart;
import lombok.Getter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.fluids.FluidType;

public class FuelTankBlock extends RotatedPillarBlock implements IRocketPart {

    @Getter
    private final IFuelTankProperties tankProperties;

    public FuelTankBlock(Properties properties, IFuelTankProperties tankProperties) {
        super(properties);
        this.tankProperties = tankProperties;
    }

    @Override
    public int getTier() {
        return this.tankProperties.getTier();
    }

    public enum FuelTankProperties implements IFuelTankProperties {
        BASIC("basic", 1, 5 * FluidType.BUCKET_VOLUME),
        ADVANCED("advanced", 2, 7 * FluidType.BUCKET_VOLUME),
        ELITE("elite", 3, 12 * FluidType.BUCKET_VOLUME),
        ;

        @Getter
        private final String serializedName;
        @Getter
        private final int tier;
        @Getter
        private final int fuelStorage;

        FuelTankProperties(String serializedName, int tier, int fuelStorage) {
            this.tier = tier;
            this.fuelStorage = fuelStorage;
            this.serializedName = serializedName;
        }
    }
}
