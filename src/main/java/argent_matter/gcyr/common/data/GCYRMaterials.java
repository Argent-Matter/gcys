package argent_matter.gcyr.common.data;

import argent_matter.gcyr.GCYR;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.*;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_PLATE;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.SHINY;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

@SuppressWarnings("unused")
public class GCYRMaterials {

    public static void init() {

    }

    public static void modifyMaterials() {
        // Add flags to base GT materials
        IronMagnetic.addFlags(GENERATE_FOIL);
        TitaniumTungstenCarbide.addFlags(GENERATE_ROD);
        Titanium.setProperty(PropertyKey.ORE, new OreProperty());

        GTMaterials.Hydrogen.getProperty(PropertyKey.FLUID).enqueueRegistration(FluidStorageKeys.PLASMA, new FluidBuilder());
    }

    //region first degree mats
    public static final Material PotassiumChloride = new Material.Builder(GCYR.id("potassium_chloride"))
            .dust(1)
            .color(0xF6F1AE)
            .components(Potassium, 1, Chlorine, 1)
            .buildAndRegister();

    public static final Material Bisalloy400 = new Material.Builder(GCYR.id("bisalloy_400"))
            .ingot(5).fluid()
            .color(0x0ca819)
            .components(Carbon, 3, Manganese, 4, Silicon, 2, Chromium, 3, Molybdenum, 1, Iron, 11)
            .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_ROD, MaterialFlags.GENERATE_LONG_ROD, MaterialFlags.GENERATE_GEAR, MaterialFlags.GENERATE_FRAME)
            .toolStats(ToolProperty.Builder.of(70.0F, 2.25F, 1296, 5).build())
            .blastTemp(10800, BlastProperty.GasTier.HIGHER)
            .buildAndRegister();

    public static final Material ChromicAcid = new Material.Builder(GCYR.id("chromic_acid"))
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
            .color(0xE5D8F2)
            .components(Hydrogen, 2, Chromium, 1, Oxygen, 4)
            .buildAndRegister();

    public static Material Trinaquadalloy = new Material.Builder(GCYR.id("trinaquadalloy"))
            .ingot().fluid()
            .color(0x281832).iconSet(MaterialIconSet.BRIGHT)
            .flags(GENERATE_PLATE, GENERATE_DENSE)
            .components(Trinium, 6, Naquadah, 2, Carbon, 1)
            .blastTemp(8747, BlastProperty.GasTier.HIGHER, GTValues.VA[GTValues.ZPM], 1200)
            .buildAndRegister();

    public static Material Fluorite = new Material.Builder(GCYR.id("fluorite"))
            .gem().ore()
            .color(0x0c9949).iconSet(MaterialIconSet.DIAMOND)
            .components(Calcium, 1, Fluorine, 2)
            .buildAndRegister();

    public static Material CobaltBromide = new Material.Builder(GCYR.id("cobalt_bromide"))
            .fluid()
            .colorAverage()
            .flags(DECOMPOSITION_BY_ELECTROLYZING)
            .components(Cobalt, 1, Bromine, 1, AceticAcid, 1)
            .buildAndRegister();

    public static Material IronOxide = new Material.Builder(GCYR.id("iron_oxide"))
            .dust()
            .color(0xBD3514)
            .flags(DECOMPOSITION_BY_CENTRIFUGING)
            .components(Iron, 2, Oxygen, 3)
            .buildAndRegister();

    public static Material MartianRockDust = new Material.Builder(GCYR.id("martian_rock_dust"))
            .dust()
            .color(0xAB2C0E)
            .buildAndRegister();
    public static Material ManganeseBromide = new Material.Builder(GCYR.id("manganese_bromide"))
            .fluid()
            .colorAverage()
            .flags(DECOMPOSITION_BY_ELECTROLYZING)
            .components(Manganese, 1, Bromine, 1, AceticAcid, 1)
            .buildAndRegister();
    public static Material ManganeseAcetate = new Material.Builder(GCYR.id("manganese_acetate"))
            .fluid()
            .colorAverage()
            .flags(DECOMPOSITION_BY_ELECTROLYZING)
            .components(Manganese, 1, AceticAcid, 1)
            .buildAndRegister();
    public static Material HydrobromicAcid = new Material.Builder(GCYR.id("hydrobromic_acid"))
            .fluid()
            .colorAverage()
            .flags(DECOMPOSITION_BY_ELECTROLYZING)
            .components(Hydrogen, 1, Bromine, 1, Water, 1)
            .buildAndRegister();

    public static Material CoMnBrCatalyst = new Material.Builder(GCYR.id("co_mn_br_catalyst"))
            .fluid()
            .colorAverage()
            .components(CobaltBromide, 1, ManganeseBromide, 1, ManganeseAcetate, 1, HydrobromicAcid, 1)
            .buildAndRegister();

    //endregion

    //region second degree mats

    public static final Material FiberGlass = new Material.Builder(GCYR.id("fiberglass"))
            .polymer(1)
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(1531))
            .color(0xC7D9D1).iconSet(SHINY)
            .flags(GENERATE_FINE_WIRE, GENERATE_PLATE, GENERATE_FOIL, NO_SMASHING, NO_WORKING)
            .components(Epoxy, 2, SiliconDioxide, 7)
            .buildAndRegister();

    //endregion

    //region organic chemistry materials
    public static final Material PyromelliticDianhydride = new Material.Builder(GCYR.id("pyrometillic_dianhydride"))
            .dust()
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .color(0xB48C63)
            .components(Carbon, 10, Hydrogen, 2, Oxygen, 6)
            .buildAndRegister()
            .setFormula("C6H2(C2O3)2", true);

    public static final Material Durene = new Material.Builder(GCYR.id("durene"))
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .color(0xB48C63)
            .components(Carbon, 10, Hydrogen, 14)
            .buildAndRegister()
            .setFormula("C6H2(CH3)4", true);

    public static final Material Dimethylformamide = new Material.Builder(GCYR.id("dimethylformamide"))
            .fluid()
            .color(0x554469)
            .flags(DISABLE_DECOMPOSITION)
            .components(Carbon, 3, Hydrogen, 7, Nitrogen, 1, Oxygen, 1)
            .buildAndRegister()
            .setFormula("(CH3)2NC(O)H", true);

    public static final Material Oxydianiline = new Material.Builder(GCYR.id("oxydianiline"))
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .color(0xfAAEE0)
            .components(Carbon, 12, Hydrogen, 12, Nitrogen, 2, Oxygen, 1)
            .buildAndRegister();

    public static final Material OxydianilineSludge = new Material.Builder(GCYR.id("oxydianiline_sludge"))
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .color(0xD9CCBF)
            .components(Oxydianiline, 1, Dimethylformamide, 1)
            .buildAndRegister();

    public static final Material KaptonK = new Material.Builder(GCYR.id("kapton_k"))
            .polymer(1)
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .color(0x915A23)
            .appendFlags(STD_METAL, GENERATE_FOIL)
            .components(PyromelliticDianhydride, 1, Oxydianiline, 1)
            .buildAndRegister();


    // Kevlar
    public static final Material ParaXylene = new Material.Builder(GCYR.id("para_xylene"))
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .colorAverage()
            .components(Carbon, 8, Hydrogen, 10)
            .buildAndRegister();

    public static final Material BisTrichloromethylBenzene = new Material.Builder(GCYR.id("bis_trichloromethyl_benzene"))
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .color(0xB48C63)
            .components(Carbon, 8, Hydrogen, 4, Chlorine, 6)
            .buildAndRegister()
            .setFormula("C6H4(CCl3)2", true);

    public static final Material TerephthalicAcid = new Material.Builder(GCYR.id("therephthalic_acid"))
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().attribute(FluidAttributes.ACID))
            .color(0xDB9374)
            .components(Carbon, 6, Hydrogen, 4, CarbonDioxide, 2, Hydrogen, 2)
            .buildAndRegister()
            .setFormula("C6H4(CO2H)2", true);

    public static final Material TerephthaloylChloride = new Material.Builder(GCYR.id("terephthaloyl_chloride"))
            .fluid()
            .color(0xB883DE)
            .components(Carbon, 8, Hydrogen, 4, Chlorine, 2, Oxygen, 2)
            .buildAndRegister();

    public static final Material Nitroaniline = new Material.Builder(GCYR.id("nitroaniline"))
            .fluid()
            .colorAverage()
            .components(Carbon, 8, Hydrogen, 4, Chlorine, 2, Oxygen, 2)
            .buildAndRegister();

    public static final Material ParaPhenylenediamine = new Material.Builder(GCYR.id("para_phenylenediamine"))
            .fluid()
            .color(0xC3DE83)
            .components(Nitrochlorobenzene, 1, Ammonia, 1)
            .buildAndRegister();

    public static final Material ParaAramid = new Material.Builder(GCYR.id("para_aramid"))
            .polymer(2)
            .fluid(FluidStorageKeys.LIQUID, new FluidBuilder().temperature(493))
            .color(0xE6ED7B)
            .appendFlags(STD_METAL, GENERATE_FOIL)
            .components(ParaPhenylenediamine, 1, TerephthaloylChloride, 1)
            .fluidPipeProperties(1500, 450, true, true, true, false)
            .buildAndRegister();

    //endregion

}
