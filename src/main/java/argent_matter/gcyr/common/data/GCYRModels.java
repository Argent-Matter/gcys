package argent_matter.gcyr.common.data;

import argent_matter.gcyr.GCYR;
import argent_matter.gcyr.api.block.IRocketMotorType;
import argent_matter.gcyr.common.block.FuelTankBlock;
import argent_matter.gcyr.common.block.RocketMotorBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;

public class GCYRModels {

    public static void rocketMotorModel(DataGenContext<Block, RocketMotorBlock> ctx, RegistrateBlockstateProvider prov, IRocketMotorType type) {
        prov.simpleBlock(ctx.getEntry(), prov.models().cubeBottomTop("%s_rocket_motor".formatted(type.getSerializedName()),
                GCYR.id("block/casings/%s_rocket_motor/rocket_motor_side".formatted(type.getSerializedName())),
                GCYR.id("block/casings/%s_rocket_motor/rocket_motor_bottom".formatted(type.getSerializedName())),
                GCYR.id("block/casings/%s_rocket_motor/rocket_motor_top".formatted(type.getSerializedName()))
        ));
    }

    public static void airlockDoorModel(DataGenContext<Block, DoorBlock> ctx, RegistrateBlockstateProvider prov) {
        prov.doorBlock(ctx.getEntry(), GCYR.id("block/airlock_door_bottom"), GCYR.id("block/airlock_door_top"));
    }

    public static void fuelTankModel(DataGenContext<Block, FuelTankBlock> ctx, RegistrateBlockstateProvider prov) {
        prov.axisBlock(ctx.getEntry());
    }

    public static void seatModel(DataGenContext<Block, CarpetBlock> ctx, RegistrateBlockstateProvider prov) {
        prov.simpleBlock(ctx.getEntry(), prov.models().carpet("seat", new ResourceLocation("block/light_gray_wool")));
    }

    public static void randomRotatedModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ModelFile cubeAll = prov.cubeAll(block);
        ModelFile cubeMirroredAll = prov.models().singleTexture(ctx.getName() + "_mirrored", prov.mcLoc(ModelProvider.BLOCK_FOLDER + "/cube_mirrored_all"), "all", prov.blockTexture(block));
        ConfiguredModel[] models = ConfiguredModel.builder()
                .modelFile(cubeAll)
                .rotationY(0)
                .nextModel()
                .modelFile(cubeAll)
                .rotationY(180)
                .nextModel()
                .modelFile(cubeMirroredAll)
                .rotationY(0)
                .nextModel()
                .modelFile(cubeMirroredAll)
                .rotationY(180)
                .build();
        prov.simpleBlock(block, models);
    }

    public static void crossModel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        Block block = ctx.getEntry();
        ModelFile cross = prov.models().cross(ctx.getName(), prov.blockTexture(block));
        prov.simpleBlock(block, cross);
    }

    public static void blockTextureGeneratedModel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov) {
        prov.generated(ctx::getEntry, prov.modLoc("block/" + ctx.getName()));
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> cubeAllModel(String name,
                                                                                                             ResourceLocation texture) {
        return (ctx, prov) -> {
            prov.simpleBlock(ctx.getEntry(), prov.models().cubeAll(name, texture));
        };
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> grassBlockModel(String name,
                                                                                                                ResourceLocation bottomTexture) {
        return grassBlockModel(name,
                GCYR.id("block/" + name + "_side"),
                bottomTexture,
                GCYR.id("block/" + name + "_top"));
    }

    public static NonNullBiConsumer<DataGenContext<Block, Block>, RegistrateBlockstateProvider> grassBlockModel(String name,
                                                                                                                ResourceLocation sideTexture,
                                                                                                                ResourceLocation bottomTexture,
                                                                                                                ResourceLocation topTexture) {
        return (ctx, prov) -> {
            ModelFile base = prov.models().cubeBottomTop(name, sideTexture, bottomTexture, topTexture);
            ConfiguredModel modelY0 = new ConfiguredModel(base, 0, 0, false);
            ConfiguredModel modelY90 = new ConfiguredModel(base, 0, 90, false);
            ConfiguredModel modelY180 = new ConfiguredModel(base, 0, 180, false);
            ConfiguredModel modelY270 = new ConfiguredModel(base, 0, 270, false);
            prov.getVariantBuilder(ctx.getEntry())
                    .partialState()
                    .setModels(modelY0, modelY90, modelY180, modelY270);
        };
    }

}
