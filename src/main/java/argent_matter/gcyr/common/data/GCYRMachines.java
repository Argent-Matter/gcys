package argent_matter.gcyr.common.data;

import argent_matter.gcyr.GCYR;
import argent_matter.gcyr.common.machine.electric.OxygenSpreaderMachine;
import argent_matter.gcyr.common.machine.multiblock.RocketScannerMachine;
import argent_matter.gcyr.common.machine.multiblock.SpaceStationPackagerMachine;
import argent_matter.gcyr.common.machine.multiblock.electric.DroneHangarMachine;
import argent_matter.gcyr.common.machine.multiblock.electric.DysonSystemControllerMachine;
import argent_matter.gcyr.data.recipe.GCYRTags;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.TieredHullMachineRenderer;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static argent_matter.gcyr.api.registries.GCYRRegistries.REGISTRATE;
import static argent_matter.gcyr.common.data.GCYRBlocks.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_ATOMIC;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.POWER_TRANSFORMER;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

@SuppressWarnings({"Convert2MethodRef", "FunctionalExpressionCanBeFolded", "unused", "DataFlowIssue"})
public class GCYRMachines {
    public final static int[] ELECTRIC_TIERS = GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? OpV : UV);
    public final static int[] LOW_TIERS = GTValues.tiersBetween(LV, EV);
    public final static int[] HIGH_TIERS = GTValues.tiersBetween(IV, GTCEuAPI.isHighTier() ? OpV : UHV);
    public static final Int2IntFunction defaultTankSizeFunction = tier -> (tier <= GTValues.LV ? 8 : tier == GTValues.MV ? 12 : tier == GTValues.HV ? 16 : tier == GTValues.EV ? 32 : 64) * FluidType.BUCKET_VOLUME;


    public final static MachineDefinition[] OXYGEN_SPREADER = registerTieredMachines("oxygen_spreader", OxygenSpreaderMachine::new,
            (tier, builder) -> builder
                    .langValue("%s Oxygen Spreader".formatted(VNF[tier]))
                    .rotationState(RotationState.NON_Y_AXIS)
                    .renderer(() -> new TieredHullMachineRenderer(tier, GCYR.id("block/machine/oxygen_spreader")))
                    .recipeType(GCYRRecipeTypes.OXYGEN_SPREADER_RECIPES)
                    .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, GCYRRecipeTypes.OXYGEN_SPREADER_RECIPES, OxygenSpreaderMachine.tankScalingFunction(tier), true))
                    .blockBuilder(block -> block.tag(GCYRTags.PASSES_FLOOD_FILL))
                    .register(),
            HIGH_TIERS);

    public static final MachineDefinition ROCKET_SCANNER = REGISTRATE.multiblock("rocket_scanner", RocketScannerMachine::new)
            .langValue("Rocket Scanner")
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .allowExtendedFacing(false)
            .tier(GTValues.EV)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("     ", "  K  ", "  K  ", "  K  ", "  K  ", "  K  ")
                    .aisle(" BBB ", "     ", "     ", "     ", "     ", "     ")
                    .aisle(" BBB ", "     ", "     ", "     ", "     ", "     ")
                    .aisle(" BBB ", "     ", "     ", "     ", "     ", "     ")
                    .aisle("     ", "  S  ", "     ", "     ", "     ", "     ")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('B', blocks(LAUNCH_PAD.get()))
                    .where('K', blocks(GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.StainlessSteel).get()))
                    .where(' ', any())
                    .build())
            .shapeInfos(definition -> {
                ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("     ", "  S  ", "     ", "     ", "     ", "     ")
                        .aisle(" BBB ", "  E  ", "     ", "     ", "     ", "     ")
                        .aisle(" BBB ", " EEE ", " TTT ", "  C  ", "     ", "     ")
                        .aisle(" BBB ", "  E  ", "     ", "     ", "     ", "     ")
                        .aisle("     ", "  K  ", "  K  ", "  K  ", "  K  ", "  K  ")
                        .where('S', definition, Direction.NORTH)
                        .where(' ', Blocks.AIR)
                        .where('K', GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.StainlessSteel).get())
                        .where('B', LAUNCH_PAD);
                shapeInfo.add(builder
                        .where('E', Blocks.AIR)
                        .where('T', Blocks.AIR)
                        .where('C', Blocks.AIR).build());
                shapeInfo.add(builder
                        .where('E', BASIC_ROCKET_MOTOR)
                        .where('T', BASIC_FUEL_TANK)
                        .where('C', SEAT).build());
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/voltage/ev/side"),
                    GTCEu.id("block/multiblock/assembly_line"), false)
            .register();

    public static final MachineDefinition SPACE_STATION_PACKAGER = REGISTRATE.multiblock("space_station_packager", SpaceStationPackagerMachine::new)
            .langValue("Space Station Packager")
            .rotationState(RotationState.NON_Y_AXIS)
            .allowFlip(false)
            .allowExtendedFacing(false)
            .tier(GTValues.LuV)
            .pattern((definition) -> FactoryBlockPattern.start()
                    .aisle("       ", "   K   ", "   K   ", "   K   ", "   K   ", "   K   ")
                    .aisle("BBBBBBB", "       ", "       ", "       ", "       ", "       ")
                    .aisle("BBBBBBB", "       ", "       ", "       ", "       ", "       ")
                    .aisle("BBBBBBB", "       ", "       ", "       ", "       ", "       ")
                    .aisle("BBBBBBB", "       ", "       ", "       ", "       ", "       ")
                    .aisle("BBBBBBB", "       ", "       ", "       ", "       ", "       ")
                    .aisle("       ", "   S   ", "       ", "       ", "       ", "       ")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('B', blocks(LAUNCH_PAD.get()))
                    .where('K', blocks(GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.StainlessSteel).get()))
                    .where(' ', any())
                    .build())
            .shapeInfos(definition -> {
                ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
                MultiblockShapeInfo.ShapeInfoBuilder builder = MultiblockShapeInfo.builder()
                        .aisle("       ", "   S   ", "       ", "       ", "       ", "       ")
                        .aisle("BBBBBBB", "       ", "       ", "       ", "       ", "       ")
                        .aisle("BBBBBBB", " CCCCC ", " CCDCC ", " CGHGC ", " CCCCC ", " CCCCC ")
                        .aisle("BBBBBBB", " CCCCC ", " C   C ", " G   G ", " C   C ", " CCCCC ")
                        .aisle("BBBBBBB", " CCCCC ", " C   C ", " G   G ", " C   C ", " CCCCC ")
                        .aisle("BBBBBBB", " CCCCC ", " C   C ", " G   G ", " C   C ", " CCCCC ")
                        .aisle("BBBBBBB", " CCCCC ", " CCDCC ", " CGGGC ", " CCDCC ", " CCCCC ")
                        .aisle("BBBBBBB", "       ", "       ", "       ", "       ", "       ")
                        .aisle("       ", "   K   ", "   K   ", "   K   ", "   K   ", "   K   ")
                        .where('S', definition, Direction.NORTH)
                        .where(' ', Blocks.AIR)
                        .where('K', GTBlocks.MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.StainlessSteel).get())
                        .where('B', LAUNCH_PAD);
                shapeInfo.add(builder
                        .where('D', Blocks.AIR)
                        .where('H', Blocks.AIR)
                        .where('G', Blocks.AIR)
                        .where('C', Blocks.AIR).build());
                shapeInfo.add(builder
                        .where('D', AIRLOCK_DOOR.getDefaultState().setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER))
                        .where('H', AIRLOCK_DOOR.getDefaultState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER))
                        .where('G', CASING_TEMPERED_GLASS)
                        .where('C', Blocks.WHITE_CONCRETE).build());
                return shapeInfo;
            })
            .workableCasingRenderer(GTCEu.id("block/casings/voltage/luv/side"),
                    GTCEu.id("block/multiblock/assembly_line"), false)
            .register();

    public static final MultiblockMachineDefinition DYSON_SYSTEM_CONTROLLER = REGISTRATE.multiblock("dyson_system_controller", DysonSystemControllerMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(() -> CASING_ATOMIC.get()) // You MUST do it like this, so that the GTBlocks/GCYMBlocks class isn't loaded too early. Because that causes a crash.
            .recipeType(GCYRRecipeTypes.DYSON_ENERGY_RECIPES)
            .tier(GTValues.UV)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("CCCCCCC", "CCCCCCC", "  F    ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("CCCCCCC", "CCCCCCC", "  F    ", "  F    ", "  F    ", "  F    ", "  F    ", "  F    ", "       ", "  H    ", " HHH   ", "  H    ")
                    .aisle("CCCCCCC", "CCGCCCC", "FFGFF  ", " FGF   ", " FGF   ", " FGF   ", " FGF   ", " FGF   ", "  G    ", " HGH   ", " HGH   ", " HHH   ")
                    .aisle("CCCCCCC", "CCCCCCC", "  F    ", "  F    ", "  F  EE", "  F E  ", "  F E  ", "  F    ", "       ", "  H    ", " HHH   ", "  H    ")
                    .aisle("CCCCCCC", "CCCCCCC", "  F  X ", "     X ", "    XEE", "    ET ", "    E  ", "       ", "       ", "       ", "       ", "       ")
                    .aisle("CCCCCCC", "CCCCCCC", "       ", "       ", "     X ", "     EE", "     EE", "       ", "       ", "       ", "       ", "       ")
                    .aisle("CCCCCCC", "CCCSCCC", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                    .where('S', controller(blocks(definition.getBlock())))
                    .where('C', blocks(CASING_ATOMIC.get()).setMinGlobalLimited(90).or(autoAbilities(definition.getRecipeTypes())))
                    .where('G', blocks(ChemicalHelper.getBlock(TagPrefix.wireGtHex, GTMaterials.RutheniumTriniumAmericiumNeutronate)))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.HastelloyC276)))
                    .where('H', blocks(CASING_BEAM_RECEIVER.get()))
                    .where('X', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.TungstenSteel)))
                    .where('E', blocks(ChemicalHelper.getBlock(TagPrefix.block, GTMaterials.HSSS)))
                    .where('T', blocks(POWER_TRANSFORMER[GTValues.UV].getBlock()))
                    .where(' ', any())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/gcym/atomic_casing"),
                    GTCEu.id("block/multiblock/assembly_line"), false)
            .register();

    public static final MultiblockMachineDefinition SPACE_ELEVATOR = REGISTRATE.multiblock("space_elevator", WorkableElectricMultiblockMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(() -> CASING_TUNGSTENSTEEL_ROBUST.get())
            .recipeType(GCYRRecipeTypes.SPACE_ELEVATOR_RECIPES)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("         KKKKKKK         ", "          BXXXB          ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("       KKKKKKKKKKK       ", "        BBBBBBBBB        ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("     KKKKKKKKKKKKKKK     ", "       B         B       ", "          B   B          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("    KKKKKKKKKKKKKKKKK    ", "                         ", "                         ", "          B   B          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("   KKKKKKKKKKKKKKKKKKK   ", "                         ", "                         ", "                         ", "          B   B          ", "          BXXXB          ", "        FFFXXXFFF        ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("  KKKKKKKKKKKKKKKKKKKKK  ", "     X             X     ", "     X             X     ", "     X             X     ", "     X             X     ", "     X             X     ", "     XFF  B   B  FFX     ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("  KKKKKKKKKKKKKKKKKKKKK  ", "                         ", "                         ", "                         ", "                         ", "                         ", "     F             F     ", "      X           X      ", "      X           X      ", "      X   B   B   X      ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle(" KKKKKKKKKKKKKKKKKKKKKKK ", "  B                   B  ", "                         ", "                         ", "                         ", "                         ", "     F             F     ", "                         ", "                         ", "       X         X       ", "       X         X       ", "       X         X       ", "       X  B   B  X       ", "          BXXXB          ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle(" KKKKKKKKKKKKKKKKKKKKKKK ", " B                     B ", "                         ", "                         ", "                         ", "                         ", "    F               F    ", "                         ", "                         ", "                         ", "                         ", "        X       X        ", "        X       X        ", "        XFF   FFX        ", "        X       X        ", "        X       X        ", "        X B   B X        ", "          BXXXB          ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("KKKKKKKKKKKKKKKKKKKKKKKKK", " B                     B ", "                         ", "                         ", "                         ", "                         ", "    F               F    ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "        F       F        ", "                         ", "         X     X         ", "         X     X         ", "         X     X         ", "         X     X         ", "         X     X         ", "         XB   BX         ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("KKKKKKKKKKKKKKKKKKKKKKKKK", "BB         BBB         BB", "  B        BWB        B  ", "   B       BWB       B   ", "    B      BWB      B    ", "    B      BWB      B    ", "    FB     BWB     BF    ", "     B     BWB     B     ", "     B     BWB     B     ", "      B    BWB    B      ", "      B    BWB    B      ", "      B    BWB    B      ", "       B   BWB   B       ", "       BF  BWB  FB       ", "       B   BWB   B       ", "       B   BWB   B       ", "        B  BWB  B        ", "        B  BWB  B        ", "        B  BWB  B        ", "        B X   X B        ", "         BX   XB         ", "         BX   XB         ", "         BX   XB         ", "          B   B          ", "           XXX           ", "           XXX           ", "           XXX           ", "           XXX           ", "                         ", "                         ", "                         ")
                    .aisle("KKKKKKKKKKKKKKKKKKKKKKKKK", "XB        B   B        BX", " X        B   B        X ", "  X       B   B       X  ", "   X      B   B      X   ", "    X     B   B     X    ", "    X     B   B     X    ", "     X    B   B    X     ", "     X    B   B    X     ", "     X    B   B    X     ", "      X   B   B   X      ", "      X   B   B   X      ", "      X   B   B   X      ", "       X  B   B  X       ", "       X  B   B  X       ", "       X  B   B  X       ", "       X  B   B  X       ", "        X B   B X        ", "        X B   B X        ", "        X  BWB  X        ", "        X  BWB  X        ", "         X BWB X         ", "         X BWB X         ", "         X BWB X         ", "          XBWBX          ", "          XBWBX          ", "          XBWBX          ", "          XBWBX          ", "            B            ", "            B            ", "            B            ")
                    .aisle("KKKKKKKKKKKKKKKKKKKKKKKKK", "XB        B G B        BX", " X        W G W        X ", "  X       W G W       X  ", "   X      W G W      X   ", "    X     W G W     X    ", "    X     W G W     X    ", "     X    W G W    X     ", "     X    W G W    X     ", "     X    W G W    X     ", "      X   W G W   X      ", "      X   W G W   X      ", "      X   W G W   X      ", "       X  W G W  X       ", "       X  W G W  X       ", "       X  W G W  X       ", "       X  W G W  X       ", "        X W G W X        ", "        X W G W X        ", "        X  WGW  X        ", "        X  WGW  X        ", "         X WGW X         ", "         X WGW X         ", "         X WGW X         ", "          XWGWX          ", "          XWGWX          ", "          XWGWX          ", "          XWGWX          ", "           BGB           ", "           BGB           ", "           BGB           ")
                    .aisle("KKKKKKKKKKKKKKKKKKKKKKKKK", "XB        B   B        BX", " X        B   B        X ", "  X       B   B       X  ", "   X      B   B      X   ", "    X     B   B     X    ", "    X     B   B     X    ", "     X    B   B    X     ", "     X    B   B    X     ", "     X    B   B    X     ", "      X   B   B   X      ", "      X   B   B   X      ", "      X   B   B   X      ", "       X  B   B  X       ", "       X  B   B  X       ", "       X  B   B  X       ", "       X  B   B  X       ", "        X B   B X        ", "        X B   B X        ", "        X  BWB  X        ", "        X  BWB  X        ", "         X BWB X         ", "         X BWB X         ", "         X BWB X         ", "          XBWBX          ", "          XBWBX          ", "          XBWBX          ", "          XBWBX          ", "            B            ", "            B            ", "            B            ")
                    .aisle("KKKKKKKKKKKKKKKKKKKKKKKKK", "BB         BBB         BB", "  B        BWB        B  ", "   B       BWB       B   ", "    B      BWB      B    ", "    B      BWB      B    ", "    FB     BWB     BF    ", "     B     BWB     B     ", "     B     BWB     B     ", "      B    BWB    B      ", "      B    BWB    B      ", "      B    BWB    B      ", "       B   BWB   B       ", "       BF  BWB  FB       ", "       B   BWB   B       ", "       B   BWB   B       ", "        B  BWB  B        ", "        B  BWB  B        ", "        B  BWB  B        ", "        B X   X B        ", "         BX   XB         ", "         BX   XB         ", "         BX   XB         ", "          B   B          ", "           XXX           ", "           XXX           ", "           XXX           ", "           XXX           ", "                         ", "                         ", "                         ")
                    .aisle("KKKKKKKKKKKKKKKKKKKKKKKKK", " B                     B ", "                         ", "                         ", "                         ", "                         ", "    F               F    ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "        F       F        ", "                         ", "         X     X         ", "         X     X         ", "         X     X         ", "         X     X         ", "         X     X         ", "         XB   BX         ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle(" KKKKKKKKKKKKKKKKKKKKKKK ", " B                     B ", "                         ", "                         ", "                         ", "                         ", "    F               F    ", "                         ", "                         ", "                         ", "                         ", "        X       X        ", "        X       X        ", "        XFF   FFX        ", "        X       X        ", "        X       X        ", "        X B   B X        ", "          BXXXB          ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle(" KKKKKKKKKKKKKKKKKKKKKKK ", "  B                   B  ", "                         ", "                         ", "                         ", "                         ", "     F             F     ", "                         ", "                         ", "       X         X       ", "       X         X       ", "       X         X       ", "       X  B   B  X       ", "          BXXXB          ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("  KKKKKKKKKKKKKKKKKKKKK  ", "                         ", "                         ", "                         ", "                         ", "                         ", "     F             F     ", "      X           X      ", "      X           X      ", "      X   B   B   X      ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("  KKKKKKKKKKKKKKKKKKKKK  ", "     X             X     ", "     X             X     ", "     X             X     ", "     X             X     ", "     X             X     ", "     XFF  B   B  FFX     ", "          BXXXB          ", "          BXXXB          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("   KKKKKKKKKKKKKKKKKKK   ", "                         ", "                         ", "                         ", "          B   B          ", "          BXXXB          ", "        FFFXXXFFF        ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("    KKKKKKKKKKKKKKKKK    ", "                         ", "                         ", "          B   B          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("     KKKKKKKKKKKKKKK     ", "       B         B       ", "          B   B          ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("       KKKKKKKKKKK       ", "        BBBBBBBBB        ", "           XXX           ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .aisle("         KKKKKKK         ", "          BXCXB          ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ", "                         ")
                    .where('C', controller(blocks(definition.getBlock())))
                    .where('K', blocks(CASING_TUNGSTENSTEEL_ROBUST.get())
                            .or(autoAbilities(definition.getRecipeTypes()))
                            .or(autoAbilities(true, false, true)))
                    .where('X', blocks(CASING_SUPPORT.get()))
                    .where('F', blocks(ChemicalHelper.getBlock(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)))
                    .where('G', blocks(ChemicalHelper.getBlock(TagPrefix.wireGtHex, GTMaterials.EnrichedNaquadahTriniumEuropiumDuranide)))
                    .where('B', blocks(CASING_BEAM_RECEIVER.get()))
                    .where('W', blocks(FUSION_GLASS.get()))
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel"),
                    GTCEu.id("block/multiblock/assembly_line"), false)
            .register();

    public static final MultiblockMachineDefinition DRONE_HANGAR = REGISTRATE.multiblock("drone_hangar", DroneHangarMachine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .appearanceBlock(() -> CASING_ALUMINIUM_FROSTPROOF.get())
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("XXX", "XXX", "XXX")
                    .aisle("XXX", "X#X", "XXX")
                    .aisle("XXX", "XSX", "XXX")
                    .where('S', Predicates.controller(blocks(definition.getBlock())))
                    .where('X', blocks(CASING_ALUMINIUM_FROSTPROOF.get()).setMinGlobalLimited(14)
                            .or(Predicates.autoAbilities(definition.getRecipeTypes())))
                    .where('#', Predicates.air())
                    .build())
            .workableCasingRenderer(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                    GTCEu.id("block/multiblock/vacuum_freezer"), false)
            .register();


    public static MachineDefinition[] registerSimpleMachines(String name,
                                                             GTRecipeType recipeType,
                                                             Int2IntFunction tankScalingFunction,
                                                             int... tiers) {
        return registerTieredMachines(name, (holder, tier) -> new SimpleTieredMachine(holder, tier, tankScalingFunction), (tier, builder) -> builder
                .langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                .editableUI(SimpleTieredMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                .rotationState(RotationState.NON_Y_AXIS)
                .recipeType(recipeType)
                .recipeModifier(GTRecipeModifiers.ELECTRIC_OVERCLOCK.apply(OverclockingLogic.NON_PERFECT_OVERCLOCK))
                .workableTieredHullRenderer(GTCEu.id("block/machines/" + name))
                .tooltips(workableTiered(tier, GTValues.V[tier], GTValues.V[tier] * 64, recipeType, tankScalingFunction.apply(tier), true))
                .compassNode(name)
                .register(), tiers);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType, Int2IntFunction tankScalingFunction) {
        return registerSimpleMachines(name, recipeType, tankScalingFunction, ELECTRIC_TIERS);
    }

    public static MachineDefinition[] registerSimpleMachines(String name, GTRecipeType recipeType) {
        return registerSimpleMachines(name, recipeType, defaultTankSizeFunction);
    }
    
    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            var register =  REGISTRATE.machine(GTValues.VN[tier].toLowerCase() + "_" + name, holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[i] = builder.apply(tier, register);
        }
        return definitions;
    }

    public static Component explosion() {
        if (ConfigHolder.INSTANCE.machines.doTerrainExplosion)
            return Component.translatable("gtceu.universal.tooltip.terrain_resist");
        return null;
    }

    public static Component[] workableTiered(int tier, long voltage, long energyCapacity, GTRecipeType recipeType, long tankCapacity, boolean input) {
        List<Component> tooltipComponents = new ArrayList<>();
        tooltipComponents.add(input ? Component.translatable("gtceu.universal.tooltip.voltage_in", voltage, GTValues.VNF[tier]) :
                Component.translatable("gtceu.universal.tooltip.voltage_out", voltage, GTValues.VNF[tier]));
        tooltipComponents.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity", energyCapacity));
        if (recipeType.getMaxInputs(FluidRecipeCapability.CAP) > 0 || recipeType.getMaxOutputs(FluidRecipeCapability.CAP) > 0)
            tooltipComponents.add(Component.translatable("gtceu.universal.tooltip.fluid_storage_capacity", tankCapacity));
        return tooltipComponents.toArray(Component[]::new);
    }

    public static void init() {

    }
}
