package argent_matter.gcyr.data.recipe;

import argent_matter.gcyr.GCYR;
import argent_matter.gcyr.common.data.GCYRBlocks;
import argent_matter.gcyr.common.data.GCYRItems;
import argent_matter.gcyr.common.data.GCYRMachines;
import argent_matter.gcyr.common.data.GCYRMaterials;
import argent_matter.gcyr.data.recipe.builder.SmithingSpaceSuitRecipeBuilder;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

import static argent_matter.gcyr.common.data.GCYRMaterials.*;
import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.*;

public class MiscRecipes {
    public static void init(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, GCYR.id("casing_atomic"),
                GCYMBlocks.CASING_ATOMIC.asStack(2),
                "PhP", "PFP", "PwP",
                'P', new UnificationEntry(TagPrefix.plateDouble, GCYRMaterials.Trinaquadalloy),
                'F', new UnificationEntry(TagPrefix.frameGt, GTMaterials.NaquadahAlloy));

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("casing_atomic"))
                .inputItems(TagPrefix.plateDouble, GCYRMaterials.Trinaquadalloy, 6)
                .inputItems(TagPrefix.frameGt, GTMaterials.NaquadahAlloy)
                .circuitMeta(6)
                .outputItems(GCYMBlocks.CASING_ATOMIC.asStack(2))
                .duration(50).EUt(16).save(provider);

        VanillaRecipeHelper.addShapelessRecipe(provider, GCYR.id("id_chip"), GCYRItems.ID_CHIP.asStack(),
                new UnificationEntry(foil, IronMagnetic),
                new UnificationEntry(plate, Polyethylene)
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("photovoltaic_cell"), GCYRItems.PHOTOVOLTAIC_CELL.asStack(),
                "WVW", "VGV", "CVC",
                'W', GTItems.NAQUADAH_WAFER.asStack(), 'G', GTBlocks.CASING_TEMPERED_GLASS.asStack(), 'C', CustomTags.LuV_CIRCUITS, 'V', new UnificationEntry(wireFine, Gold)
        );

        GTRecipeTypes.CHEMICAL_BATH_RECIPES.recipeBuilder(GCYR.id("fiberglass"))
                .inputItems(dust, SiliconDioxide, 2)
                .inputFluids(Epoxy.getFluid(250))
                .outputFluids(FiberGlass.getFluid(250))
                .duration(200).EUt(VA[EV]).save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("space_station_package"))
                .inputItems(plate, PolyvinylChloride, 8)
                .inputItems(plate, Paper, 32)
                .inputFluids(Glue.getFluid(4000))
                .outputItems(GCYRItems.SPACE_STATION_PACKAGE.asStack(1))
                .duration(100).EUt(VA[HV]).save(provider);

        //region Spacesuit

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("space_fabric"))
                .inputItems(foil, Polytetrafluoroethylene, 4)
                .inputItems(foil, ParaAramid, 4)
                .inputItems(foil, Polybenzimidazole, 4)
                .inputItems(dust, FiberGlass, 4)
                .outputItems(GCYRItems.SPACE_FABRIC.asStack(1))
                .duration(100).EUt(VA[HV]).save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("space_helmet"), GCYRItems.SPACE_SUIT_HELMET.asStack(),
                "SFS", "FFF", "SCS",
                'S', GCYRItems.SPACE_FABRIC.asStack(), 'F', new UnificationEntry(foil, Gold), 'C', CustomTags.EV_CIRCUITS
        );
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("space_chest"), GCYRItems.SPACE_SUIT_CHEST.asStack(),
                "STS", "SCS", "SCS",
                'S', GCYRItems.SPACE_FABRIC.asStack(), 'T', GTItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL.asStack(), 'C', CustomTags.EV_CIRCUITS
        );
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("space_legs"), GCYRItems.SPACE_SUIT_LEGS.asStack(),
                "SCS", "S S", "S S",
                'S', GCYRItems.SPACE_FABRIC.asStack(), 'C', CustomTags.EV_CIRCUITS
        );
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("space_boots"), GCYRItems.SPACE_SUIT_BOOTS.asStack(),
                "S S", "S S",
                'S', GCYRItems.SPACE_FABRIC.asStack()
        );

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("space_upgrade_smithing_template"))
                .inputItems(GCYRItems.SPACE_FABRIC.asStack(5))
                .inputItems(plate, Titanium, 2)
                .inputItems(foil, Polybenzimidazole, 4)
                .outputItems(GCYRItems.SPACE_UPGRADE_SMITHING_TEMPLATE.asStack(1))
                .EUt(VA[IV]).duration(50)
                .save(provider);

        SmithingSpaceSuitRecipeBuilder.smithingSpacesuit(Ingredient.of(GCYRItems.SPACE_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(Tags.Items.ARMORS_BOOTS),
                        Ingredient.of(GCYRItems.SPACE_SUIT_BOOTS.get()))
                .save(provider, GCYR.id("space_suit_boots_smithing_trim"));
        SmithingSpaceSuitRecipeBuilder.smithingSpacesuit(Ingredient.of(GCYRItems.SPACE_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(Tags.Items.ARMORS_LEGGINGS),
                        Ingredient.of(GCYRItems.SPACE_SUIT_LEGS.get()))
                .save(provider, GCYR.id("space_suit_leggings_smithing_trim"));
        SmithingSpaceSuitRecipeBuilder.smithingSpacesuit(Ingredient.of(GCYRItems.SPACE_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(Tags.Items.ARMORS_CHESTPLATES),
                        Ingredient.of(GCYRItems.SPACE_SUIT_CHEST.get()))
                .save(provider, GCYR.id("space_suit_chestplate_smithing_trim"));
        SmithingSpaceSuitRecipeBuilder.smithingSpacesuit(Ingredient.of(GCYRItems.SPACE_UPGRADE_SMITHING_TEMPLATE.get()),
                        Ingredient.of(Tags.Items.ARMORS_HELMETS),
                        Ingredient.of(GCYRItems.SPACE_SUIT_HELMET.get()))
                .save(provider, GCYR.id("space_suit_helmet_smithing_trim"));

        //endregion

        //region machines

        MetaTileEntityLoader.registerMachineRecipe(provider, GCYRMachines.OXYGEN_SPREADER, "PCP", "FHF", "PCP", 'H', HULL, 'P', PUMP, 'F', GTItems.FLUID_FILTER, 'C', CIRCUIT);
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("rocket_scanner"), GCYRMachines.ROCKET_SCANNER.asStack(), "PCP", "CHC", "SCS", 'H', GTMachines.HULL[EV].asStack(), 'P', GCYRBlocks.LAUNCH_PAD.asStack(), 'C', CustomTags.EV_CIRCUITS, 'S', new UnificationEntry(plate, Titanium));
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("space_station_packager"), GCYRMachines.SPACE_STATION_PACKAGER.asStack(), "PCP", "FHF", "PCP", 'H', GTMachines.HULL[LuV].asStack(), 'P', new UnificationEntry(frameGt, StainlessSteel), 'C', CustomTags.LuV_CIRCUITS, 'F', GTBlocks.PLASTCRETE.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("dyson_system_controller"), GCYRMachines.DYSON_SYSTEM_CONTROLLER.asStack(), "PCP", "FHF", "PCP", 'H', GTMachines.HULL[UHV].asStack(), 'P', new UnificationEntry(plate, Darmstadtium), 'C', CustomTags.UHV_CIRCUITS, 'F', GCYMBlocks.CASING_ATOMIC.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("space_elevator"), GCYRMachines.SPACE_ELEVATOR.asStack(), "FFF", "PHP", "PCP", 'H', GTCEuAPI.isHighTier() ? GTMachines.HULL[UEV].asStack() : GTMachines.HULL[UHV].asStack(), 'F', new UnificationEntry(plateDense, Trinaquadalloy), 'C', CustomTags.UHV_CIRCUITS, 'P', GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.asStack());

        //endregion

        //region rocket parts

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("basic_fuel_tank"))
                .inputItems(GTMachines.STAINLESS_STEEL_DRUM, 2)
                .inputItems(plate, KaptonK, 6)
                .inputItems(frameGt, StainlessSteel)
                .outputItems(GCYRBlocks.BASIC_FUEL_TANK.asStack(1))
                .EUt(VA[HV]).duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("basic_rocket_motor"))
                .inputItems(GTItems.POWER_THRUSTER, 4)
                .inputItems(plate, KaptonK, 6)
                .inputItems(frameGt, StainlessSteel)
                .outputItems(GCYRBlocks.BASIC_ROCKET_MOTOR.asStack(1))
                .EUt(VA[HV]).duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("advanced_fuel_tank"))
                .inputItems(GTMachines.TUNGSTENSTEEL_DRUM, 2)
                .inputItems(plate, KaptonK, 6)
                .inputItems(frameGt, TungstenSteel)
                .outputItems(GCYRBlocks.ADVANCED_FUEL_TANK.asStack(1))
                .EUt(VA[IV]).duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("advanced_rocket_motor"))
                .inputItems(GTItems.POWER_THRUSTER_ADVANCED, 4)
                .inputItems(plate, KaptonK, 6)
                .inputItems(frameGt, TungstenSteel)
                .outputItems(GCYRBlocks.ADVANCED_ROCKET_MOTOR.asStack(1))
                .EUt(VA[IV]).duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("elite_fuel_tank"))
                .inputItems(GTMachines.QUANTUM_TANK[ZPM], 2)
                .inputItems(plate, KaptonK, 6)
                .inputItems(frameGt, NaquadahAlloy)
                .outputItems(GCYRBlocks.ELITE_FUEL_TANK.asStack(1))
                .EUt(VA[ZPM]).duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("elite_rocket_motor"))
                .inputItems(GTItems.GRAVITATION_ENGINE, 4)
                .inputItems(plate, KaptonK, 6)
                .inputItems(frameGt, NaquadahAlloy)
                .outputItems(GCYRBlocks.ELITE_ROCKET_MOTOR.asStack(1))
                .EUt(VA[ZPM]).duration(300)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("launch_pad"))
                .inputItems(Items.BLACK_CONCRETE, 1)
                .inputItems(frameGt, Steel, 1)
                .inputFluids(DyeYellow.getFluid(200))
                .outputItems(GCYRBlocks.LAUNCH_PAD.asStack(2))
                .EUt(VA[HV]).duration(40)
                .save(provider);

        GTRecipeTypes.ASSEMBLER_RECIPES.recipeBuilder(GCYR.id("seat"))
                .inputItems(Items.WHITE_CARPET, 1)
                .inputItems(rod, Steel, 6)
                .inputItems(plate, Steel, 2)
                .outputItems(GCYRBlocks.SEAT.asStack(1))
                .EUt(VA[MV]).duration(50)
                .save(provider);

        //endregion

        // region Decoration Stuff
        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("venus_cobblestone_slab"), GCYRBlocks.VENUS_COBBLESTONE_SLAB.asStack(3),
                "SSS",
                'S', GCYRBlocks.VENUS_COBBLESTONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("venus_rock_slab"), GCYRBlocks.VENUS_ROCK_SLAB.asStack(3),
                "SSS",
                'S', GCYRBlocks.VENUS_ROCK.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("mercury_cobblestone_slab"), GCYRBlocks.MERCURY_COBBLESTONE_SLAB.asStack(3),
                "SSS",
                'S', GCYRBlocks.MERCURY_COBBLESTONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("mercury_rock_slab"), GCYRBlocks.MERCURY_ROCK_SLAB.asStack(3),
                "SSS",
                'S', GCYRBlocks.MERCURY_ROCK.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("martian_cobblestone_slab"), GCYRBlocks.MARTIAN_COBBLESTONE_SLAB.asStack(3),
                "SSS",
                'S', GCYRBlocks.MARTIAN_ROCK.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("martian_rock_slab"), GCYRBlocks.MARTIAN_ROCK_SLAB.asStack(3),
                "SSS",
                'S', GCYRBlocks.MARTIAN_ROCK.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("moon_cobblestone_slab"), GCYRBlocks.MOON_COBBLESTONE_SLAB.asStack(3),
                "SSS",
                'S', GCYRBlocks.MOON_COBBLESTONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("moon_stone_slab"), GCYRBlocks.MOON_STONE.asStack(3),
                "SSS",
                'S', GCYRBlocks.MOON_STONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("venus_cobblestone_stairs"), GCYRBlocks.VENUS_COBBLESTONE_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.VENUS_COBBLESTONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("venus_rock_stairs"), GCYRBlocks.VENUS_ROCK_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.VENUS_ROCK.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("mercury_cobblestone_stairs"), GCYRBlocks.MERCURY_COBBLESTONE_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.MERCURY_COBBLESTONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("mercury_rock_stairs"), GCYRBlocks.MERCURY_ROCK_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.MERCURY_ROCK.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("martian_cobblestone_stairs"), GCYRBlocks.MARTIAN_COBBLESTONE_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.MARTIAN_COBBLESTONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("martian_rock_stairs"), GCYRBlocks.MARTIAN_ROCK_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.MARTIAN_ROCK.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("moon_cobblestone_stairs"), GCYRBlocks.MOON_COBBLESTONE_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.MOON_COBBLESTONE.asStack()
        );

        VanillaRecipeHelper.addShapedRecipe(provider, GCYR.id("moon_stone_stairs"), GCYRBlocks.MOON_STONE_STAIRS.asStack(4),
                "S  ", "SS ", "SSS",
                'S', GCYRBlocks.MOON_STONE.asStack()
        );

        VanillaRecipeHelper.addShapelessRecipe(provider, "venus_button", GCYRBlocks.VENUS_ROCK_BUTTON.asStack(), GCYRBlocks.VENUS_ROCK.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "mercury_button", GCYRBlocks.MERCURY_ROCK_BUTTON.asStack(), GCYRBlocks.MERCURY_ROCK.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "martian_button", GCYRBlocks.MARTIAN_ROCK_BUTTON.asStack(), GCYRBlocks.MARTIAN_ROCK.asStack());
        VanillaRecipeHelper.addShapelessRecipe(provider, "moon_button", GCYRBlocks.MOON_STONE_BUTTON.asStack(), GCYRBlocks.MOON_STONE.asStack());

        // endregion
    }

}
