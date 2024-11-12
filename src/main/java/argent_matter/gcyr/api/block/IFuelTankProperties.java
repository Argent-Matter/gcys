package argent_matter.gcyr.api.block;

import net.minecraft.util.StringRepresentable;

import javax.annotation.Nonnull;

public interface IFuelTankProperties extends StringRepresentable {

    /**
     * This is used for what galaxies you can travel to with this fuel tank.
     *
     * @return the tier of the fuel tank
     */
    int getTier();

    /**
     * @return how much fuel this tank can store at maximum.
     */
    int getFuelStorage();
}
