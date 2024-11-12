package argent_matter.gcyr;

import argent_matter.gcyr.api.registries.GCYRRegistries;
import argent_matter.gcyr.common.data.GCYRBlocks;
import argent_matter.gcyr.common.data.GCYRMaterials;
import argent_matter.gcyr.common.data.GCYRRecipes;
import argent_matter.gcyr.common.data.GCYRSoundEntries;
import argent_matter.gcyr.common.worldgen.GCYROres;
import argent_matter.gcyr.common.worldgen.GCYRWorldGenLayers;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.MaterialCasingCollectionEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Consumer;

@GTAddon
public class GCYRGTAddon implements IGTAddon {
    @Override
    public GTRegistrate getRegistrate() {
        return GCYRRegistries.REGISTRATE;
    }

    @Override
    public void initializeAddon() {

    }

    @Override
    public String addonModId() {
        return GCYR.MOD_ID;
    }

    @Override
    public void registerTagPrefixes() {
        TagPrefix.oreTagPrefix("moon", BlockTags.MINEABLE_WITH_PICKAXE)
                .langValue("Moon %s Ore")
                .registerOre(() -> GCYRBlocks.MOON_STONE.orElse(Blocks.STONE).defaultBlockState(), null, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).requiresCorrectToolForDrops().strength(3.0F, 3.0F), GCYR.id("block/moon_stone"));
        TagPrefix.oreTagPrefix("mars", BlockTags.MINEABLE_WITH_PICKAXE)
                .langValue("Mars %s Ore")
                .registerOre(() -> GCYRBlocks.MARTIAN_ROCK.orElse(Blocks.DEEPSLATE).defaultBlockState(), () -> GCYRMaterials.MartianRockDust, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).requiresCorrectToolForDrops().strength(3.0F, 3.0F), GCYR.id("block/martian_rock"));
        TagPrefix.oreTagPrefix("venus", BlockTags.MINEABLE_WITH_PICKAXE)
                .langValue("Venus %s Ore")
                .registerOre(() -> GCYRBlocks.VENUS_ROCK.orElse(Blocks.DEEPSLATE).defaultBlockState(), null, BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_ORANGE).requiresCorrectToolForDrops().strength(3.0F, 3.0F), GCYR.id("block/venus_rock"));
        TagPrefix.oreTagPrefix("mercury", BlockTags.MINEABLE_WITH_PICKAXE)
                .langValue("Mercury %s Ore")
                .registerOre(() -> GCYRBlocks.MERCURY_ROCK.orElse(Blocks.STONE).defaultBlockState(), null, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).requiresCorrectToolForDrops().strength(3.0F, 3.0F), GCYR.id("block/mercury_rock"));
    }

    @Override
    public void registerElements() {
        IGTAddon.super.registerElements();
    }

    @Override
    public void registerSounds() {
        GCYRSoundEntries.init();
    }

    @Override
    public void registerCovers() {
        IGTAddon.super.registerCovers();
    }

    @Override
    public void registerWorldgenLayers() {
        GCYRWorldGenLayers.init();
    }

    @Override
    public boolean requiresHighTier() {
        return false;
    }

    @Override
    public void registerVeinGenerators() {
        IGTAddon.super.registerVeinGenerators();
    }

    @Override
    public void registerOreVeins() {
        GCYROres.init();
    }

    @Override
    public void collectMaterialCasings(MaterialCasingCollectionEvent event) {
        IGTAddon.super.collectMaterialCasings(event);
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        GCYRRecipes.init(provider);
    }
}
