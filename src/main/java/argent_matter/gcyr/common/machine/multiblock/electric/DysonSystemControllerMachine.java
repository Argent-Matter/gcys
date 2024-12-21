package argent_matter.gcyr.common.machine.multiblock.electric;

import argent_matter.gcyr.api.capability.GCYRCapabilityHelper;
import argent_matter.gcyr.api.capability.IDysonSystem;
import argent_matter.gcyr.common.data.GCYRParticles;
import argent_matter.gcyr.config.GCYRConfig;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DysonSystemControllerMachine extends WorkableElectricMultiblockMachine {
    public DysonSystemControllerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (!isRemote()) {
            IDysonSystem system = GCYRCapabilityHelper.getDysonSystem((ServerLevel) this.getLevel());
            if (system == null || !system.isDysonSphereActive()) return;
            system.activeDysonSphere().setControllerPos(null);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!isRemote()) {
            IDysonSystem system = GCYRCapabilityHelper.getDysonSystem((ServerLevel) this.getLevel());
            if (system == null) return;
            if (system.isDysonSphereActive() && system.activeDysonSphere().getControllerPos() == null) {
                system.activeDysonSphere().setControllerPos(this.getPos());
            } else if (!system.isDysonSphereActive()) {
                system.addDysonSphere(this.getPos());
            }
        }
    }

    @Override
    protected @Nullable GTRecipe getRealRecipe(GTRecipe recipe) {
        if (this.getLevel().dimensionType().hasCeiling()) return null;
        if (recipe.data.contains("gcyr:repair_dyson_sphere")) {
            IDysonSystem system = GCYRCapabilityHelper.getDysonSystem((ServerLevel) this.getLevel());
            if (system != null && system.isDysonSphereActive() && (!system.activeDysonSphere().isNeedsMaintenance() || !this.getPos().equals(system.activeDysonSphere().getControllerPos()))) return null;
        }
        return super.getRealRecipe(recipe);
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        Level level = this.getLevel();
        // THE RECEIVER IS A DEADLY LAZER
        Direction frontFacing = this.getFrontFacing();
        Direction backFacing = frontFacing.getOpposite();
        Direction rightFacing = frontFacing.getClockWise();
        BlockPos pos = this.getPos().mutable().move(backFacing, 4).move(rightFacing, 1).move(0, 7 + 256 /*pre offset up by half distance*/, 0).immutable();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(Vec3.atCenterOf(pos), 3, 512, 3), EntitySelector.LIVING_ENTITY_STILL_ALIVE)) {
            entity.hurt(GTDamageTypes.ELECTRIC.source(level), GCYRConfig.INSTANCE.machine.dysonControllerBeamDamage);
        }
        return value;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        GTRecipe recipe = this.getRecipeLogic().getLastRecipe();
        if (recipe == null || this.isRemote()) return;
        if (recipe.getInputContents(ItemRecipeCapability.CAP).isEmpty()) return; // assume the recipe is a dyson launch or repair if it has item inputs.

        IDysonSystem system = GCYRCapabilityHelper.getDysonSystem((ServerLevel) this.getLevel());
        if (system == null) return;
        if (!system.isDysonSphereActive() && recipe.data.contains("gcyr:launch_dyson_sphere")) {
            system.addDysonSphere(this.getPos());
        } else if (system.isDysonSphereActive() && recipe.data.contains("gcyr:repair_dyson_sphere") && system.activeDysonSphere().getControllerPos().equals(this.getPos())) {
            system.activeDysonSphere().fixMaintenance();
        }
    }

    @Override
    public void animateTick(RandomSource random) {
        if (this.isActive() && this.getOffsetTimer() % 20 == 0) {
            var level = this.getLevel();

            Direction frontFacing = this.getFrontFacing();
            Direction backFacing = frontFacing.getOpposite();
            Direction rightFacing = frontFacing.getClockWise();
            BlockPos pos = this.getPos().mutable().move(backFacing, 4).move(rightFacing, 1).move(0, 9, 0).immutable();

            double x = pos.getX() + 0.5;
            double z = pos.getZ() + 0.5;
            for (int y = pos.getY(); y < 512; y += 2) {
                level.addAlwaysVisibleParticle(GCYRParticles.DYSON_BEAM, true, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!isFormed) return;

        if (!isRemote()) {
            IDysonSystem system = GCYRCapabilityHelper.getDysonSystem((ServerLevel) this.getLevel());
            if (system == null) return;

            if (system.isDysonSphereActive()) {
                if (system.activeDysonSphere().isNeedsMaintenance() && !system.activeDysonSphere().isCollapsed()) {
                    textList.add(Component.translatable("menu.gcyr.dyson_sphere.needs_maintenance").withStyle(this.getOffsetTimer() % 10 >= 5 ? ChatFormatting.RED : ChatFormatting.DARK_RED));
                    textList.add(Component.translatable("menu.gcyr.dyson_sphere.time_since_needed_maintenance", system.activeDysonSphere().getTimeNeededMaintenance()));
                    textList.add(Component.translatable("menu.gcyr.dyson_sphere.implosion_chance", system.activeDysonSphere().getCollapseChance() * 100.0f));
                } else if (system.activeDysonSphere().isCollapsed()) {
                    textList.add(Component.translatable("menu.gcyr.dyson_sphere.collapsed"));
                }
            }
        }

    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("dbg_start_sphere")) {
                GCYRCapabilityHelper.getDysonSystem((ServerLevel) this.getLevel()).addDysonSphere(this.getPos());
            } else if (componentData.equals("dbg_delete_sphere")) {
                GCYRCapabilityHelper.getDysonSystem((ServerLevel) this.getLevel()).disableDysonSphere(this.getPos());
            }
        }
    }
}
