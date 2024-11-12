package argent_matter.gcyr.common.entity;

import argent_matter.gcyr.api.block.IRocketPart;
import argent_matter.gcyr.api.capability.GCYRCapabilityHelper;
import argent_matter.gcyr.api.capability.ISpaceStationHolder;
import argent_matter.gcyr.api.gui.factory.EntityUIFactory;
import argent_matter.gcyr.api.registries.GCYRRegistries;
import argent_matter.gcyr.api.space.planet.Planet;
import argent_matter.gcyr.api.space.satellite.SatelliteType;
import argent_matter.gcyr.api.space.station.SpaceStation;
import argent_matter.gcyr.common.block.FuelTankBlock;
import argent_matter.gcyr.common.block.RocketMotorBlock;
import argent_matter.gcyr.common.data.*;
import argent_matter.gcyr.common.entity.data.EntityOxygenSystem;
import argent_matter.gcyr.common.entity.data.EntityTemperatureSystem;
import argent_matter.gcyr.common.item.KeyCardBehaviour;
import argent_matter.gcyr.common.item.PlanetIdChipBehaviour;
import argent_matter.gcyr.common.item.SatelliteItemBehaviour;
import argent_matter.gcyr.common.item.StationContainerBehaviour;
import argent_matter.gcyr.config.GCYRConfig;
import argent_matter.gcyr.data.loader.PlanetData;
import argent_matter.gcyr.data.recipe.GCYRTags;
import argent_matter.gcyr.mixin.LivingEntityAccessor;
import argent_matter.gcyr.util.PlatformUtils;
import argent_matter.gcyr.util.PosWithState;
import com.google.common.collect.Sets;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RocketEntity extends Entity implements HasCustomInventoryScreen, IUIHolder, PlayerRideable, IEntityAdditionalSpawnData /*, IManaged, IAutoPersistEntity*/ {

    public static final Object2BooleanMap<Fluid> FUEL_CACHE = new Object2BooleanOpenHashMap<>();

//    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(RocketEntity.class);
    public static final EntityDataAccessor<Boolean> ROCKET_STARTED = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> FUEL_CAPACITY = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> FUEL_AMOUNT = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> THRUSTER_COUNT = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> WEIGHT = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> RECIPE_DURATION = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> START_TIMER = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<List<PosWithState>> POSITIONED_STATES = SynchedEntityData.defineId(RocketEntity.class, GCYREntityDataSerializers.POSITIONED_BLOCK_STATE_LIST);
    public static final EntityDataAccessor<BlockPos> SIZE = SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.BLOCK_POS);
    public static final EntityDataAccessor<List<BlockPos>> SEAT_POSITIONS = SynchedEntityData.defineId(RocketEntity.class, GCYREntityDataSerializers.BLOCK_POS_LIST);
    public static final EntityDataAccessor<Optional<Planet>> DESTINATION = SynchedEntityData.defineId(RocketEntity.class, GCYREntityDataSerializers.PLANET);

//    @Getter
//    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    private final CustomFluidTank fuelTank;
    private final ItemStackTransfer configSlot, satelliteSlot;
    private boolean destinationIsSpaceStation;
    private final Object2IntMap<IRocketPart> partCounts = new Object2IntOpenHashMap<>();
    private boolean returnToStart;
    private SatelliteType<?> satelliteToLaunch;
    private int motorTiersTotal, fuelTankTiersTotal;
    private int motorTier, fuelTankTier, partsTier;
    private double speed;
    @Nullable
    private GTRecipe selectedFuelRecipe;

    private final Set<BlockPos> thrusterPositions = new HashSet<>();


    public RocketEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.configSlot = new ItemStackTransfer(1);
        this.configSlot.setFilter(stack -> GCYRItems.ID_CHIP.isIn(stack) || GCYRItems.KEYCARD.isIn(stack));
        this.satelliteSlot = new ItemStackTransfer(1);
        this.satelliteSlot.setFilter(stack -> GCYRItems.SPACE_STATION_PACKAGE.isIn(stack) || stack.is(GCYRTags.SATELLITES));

        this.fuelTank = new CustomFluidTank(0, fluid -> FUEL_CACHE.computeIfAbsent(fluid.getFluid(), f-> {
            return this.getServer().getRecipeManager().getAllRecipesFor(GCYRRecipeTypes.ROCKET_FUEL_RECIPES).stream().anyMatch(recipe -> {
                if (RecipeHelper.getInputEUt(recipe) > motorTier)
                    return false; // don't allow > motor tier fuels to be used.

                var list = recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
                if (!list.isEmpty()) {
                    return Arrays.stream(FluidRecipeCapability.CAP.of(list.get(0).content).getStacks()).anyMatch(stack -> stack.getFluid() == f);
                }
                return false;
            });
        }));

        // determine fuel recipe when the fuel changes
        // this happens on every tick when the rocket is fired
        this.fuelTank.setOnContentsChanged(() -> {
            // checking the selectedFuelRecipe's fuel against the tank is probably faster?
            entityData.set(FUEL_AMOUNT, fuelTank.getFluidAmount());

            if (selectedFuelRecipe != null) {
                var list = selectedFuelRecipe.inputs.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
                if (!list.isEmpty()) {
                    if (Arrays.stream(FluidRecipeCapability.CAP.of(list.get(0).content).getStacks()).anyMatch(stack -> stack.isFluidEqual(fuelTank.getFluid()))) {
                        return;
                    }
                }
            }

            this.selectedFuelRecipe = this.getServer().getRecipeManager().getAllRecipesFor(GCYRRecipeTypes.ROCKET_FUEL_RECIPES).stream().filter(recipe -> {
                var list = recipe.inputs.getOrDefault(FluidRecipeCapability.CAP, Collections.emptyList());
                if (!list.isEmpty()) {
                    return Arrays.stream(FluidRecipeCapability.CAP.of(list.get(0).content).getStacks()).anyMatch(stack -> stack.isFluidEqual(fuelTank.getFluid()));
                }
                return false;
            }).findFirst().orElse(null);

            if (selectedFuelRecipe != null) {
                setRecipeDuration(selectedFuelRecipe.duration);
            }
        });

    }

    public void reinitializeFluidStorage() {
        this.fuelTank.setCapacity(this.getFuelCapacity());
    }

    @Override
    public void tick() {
        super.tick();

        this.rotateRocket();
        this.burnEntities();

        boolean started = this.entityData.get(ROCKET_STARTED);
        if (started && consumeFuel()) {
            this.spawnParticles();
            if (countdown()) {
                this.flightMovement();
                this.goToDestination();
            }
        } else if (!started) {
            this.fall();
        }

        this.move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    protected AABB makeBoundingBox() {
        Vec3 pos = this.position();
        BlockPos size = this.entityData.get(SIZE);
        double x = size.getX() + 1;
        double y = size.getY() + 1;
        double z = size.getZ() + 1;
        return new AABB(pos.x, pos.y, pos.z, pos.x + x, pos.y + y, pos.z + z);
    }

    @Override
    public void refreshDimensions() {
        Vec3 pos = this.position();
        super.refreshDimensions();
        this.setPos(pos);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        super.interact(player, hand);
        InteractionResult result = InteractionResult.sidedSuccess(this.level().isClientSide);

        if (!this.level().isClientSide) {
            if (player.isSecondaryUseActive()) {
                this.openCustomInventoryScreen(player);
                return InteractionResult.CONSUME;
            }

            player.startRiding(this);
            return result;
        }

        return result;
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        return new ModularUI(176, 166, this, entityPlayer)
                .widget(new LabelWidget(7, 7, this.getDisplayName().getString()))
                .widget(new TankWidget(this.fuelTank, 16, 20, 20, 58, true, true).setBackground(GuiTextures.FLUID_TANK_BACKGROUND).setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP))
                .widget(new SlotWidget(configSlot, 0, 40, 20))
                .widget(new SlotWidget(satelliteSlot, 0, 60, 20))
                .widget(new ButtonWidget(40, 60, 38, 18, new GuiTextureGroup(GuiTextures.BUTTON.copy().setColor(0xFFAA0000), new TextTexture("menu.gcyr.launch")), (clickData) -> this.startRocket()))
                .widget(new ButtonWidget(40, 40, 38, 18, new GuiTextureGroup(GuiTextures.BUTTON.copy().setColor(0xFFE0B900), new TextTexture("menu.gcyr.rocket.unbuild")), (clickData) -> this.unBuild()))
                .widget(new LabelWidget(84, 25, this.getDisplayThrust()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true))
                .background(GuiTextures.BACKGROUND);
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            EntityUIFactory.INSTANCE.openUI(this, serverPlayer);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < this.getMaxPassengers();
    }

    public int getMaxPassengers() {
        return getSeatPositions().size();
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction callback) {
        if (this.hasPassenger(passenger)) {
            int passengerIndex = this.getPassengers().indexOf(passenger);
            if (this.getSeatPositions().isEmpty()) {
                passenger.stopRiding();
                return;
            }
            BlockPos seatPos = this.getSeatPositions().get(passengerIndex);
            callback.accept(passenger, this.getX() + seatPos.getX() + 0.5, this.getY() + seatPos.getY() - 0.5, this.getZ() + seatPos.getZ() + 0.5);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3[] escapeVectors = new Vec3[]{
                getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot()),
                getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() - 22.5F),
                getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() + 22.5F),
                getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() - 45.0F),
                getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() + 45.0F)
        };
        Set<BlockPos> set = Sets.newLinkedHashSet();
        double maxY = this.getBoundingBox().maxY;
        double minY = this.getBoundingBox().minY - 0.5D;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for(Vec3 vector : escapeVectors) {
            mutableBlockPos.set(this.getX() + vector.x, maxY, this.getZ() + vector.z);

            for(double d = maxY; d > minY; --d) {
                set.add(mutableBlockPos.immutable());
                mutableBlockPos.move(Direction.DOWN);
            }
        }

        for(BlockPos blockpos : set) {
            if (!this.level().getFluidState(blockpos).is(FluidTags.LAVA)) {
                double floor = this.level().getBlockFloorHeight(blockpos);
                if (DismountHelper.isBlockFloorValid(floor)) {
                    Vec3 vector3d1 = Vec3.upFromBottomCenterOf(blockpos, floor);

                    for(Pose pose : livingEntity.getDismountPoses()) {
                        if (DismountHelper.isBlockFloorValid(this.level().getBlockFloorHeight(blockpos))) {
                            livingEntity.setPose(pose);
                            return vector3d1;
                        }
                    }
                }
            }
        }

        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    public void spawnParticles() {
        Vec3 vec = this.getDeltaMovement();

        if (this.level() instanceof ServerLevel serverLevel) {
            for (BlockPos pos : this.thrusterPositions) {
                if (this.getStartTimer() >= 200) {
                    for (ServerPlayer p : serverLevel.getServer().getPlayerList().getPlayers()) {
                        serverLevel.sendParticles(p, ParticleTypes.FLAME, true,
                                this.getX() - vec.x + pos.getX() + 0.5, this.getY() - vec.y - 2.2 + pos.getY() + 0.5, this.getZ() - vec.z + pos.getZ() + 0.5,
                                20, 0.1, 0.1, 0.1, 0.001);
                        serverLevel.sendParticles(p, ParticleTypes.LARGE_SMOKE, true,
                                this.getX() - vec.x + pos.getX() + 0.5, this.getY() - vec.y - 3.2 + pos.getY() + 0.5, this.getZ() - vec.z + pos.getZ() + 0.5,
                                10, 0.1, 0.1, 0.1, 0.04);
                    }
                } else {
                    for (ServerPlayer p : serverLevel.getServer().getPlayerList().getPlayers()) {
                        serverLevel.sendParticles(p, ParticleTypes.CAMPFIRE_COSY_SMOKE, true,
                                this.getX() - vec.x + pos.getX() + 0.5, this.getY() - vec.y - 0.1 + pos.getY() + 0.5, this.getZ() - vec.z + pos.getZ() + 0.5,
                                6, 0.1, 0.1, 0.1, 0.023);
                    }
                }
            }
        }
    }

    @Nullable
    public Player getFirstPlayerPassenger() {
        if (!this.getPassengers().isEmpty() && this.getPassengers().get(0) instanceof Player player) {
            return player;
        }
        return null;
    }


    @Nullable
    public LivingEntity getControllingPassenger() {
        if (this.getFirstPassenger() instanceof LivingEntity livingEntity) {
            return livingEntity;
        }
        return null;
    }

    public void rotateRocket() {
        Player player = this.getFirstPlayerPassenger();

        if (player != null) {
            if (player.xxa > 0) {
                setEntityRotation(this, 1);
            }

            if (player.xxa < 0) {
                setEntityRotation(this, -1);
            }
        }
    }

    public int computeRequiredFuelAmountForDestination(@Nullable Planet destination) {
        Planet current = PlanetData.getPlanetFromLevelOrOrbit(this.level().dimension()).orElse(null);
        if (current == null || destination == null) {
            return (int)(this.getFuelCapacity() * 0.85);
        } else if (destination.parentWorld() == current.level() || current.parentWorld() == destination.level() || current == destination) {
            return GCYRConfig.INSTANCE.rocket.moonFuelAmount;
        } else if (current.solarSystem().equals(destination.solarSystem())) {
            return GCYRConfig.INSTANCE.rocket.solarSystemFuelAmount;
        } else if (current.galaxy().equals(destination.galaxy())) {
            return GCYRConfig.INSTANCE.rocket.galaxyFuelAmount;
        } else {
            return GCYRConfig.INSTANCE.rocket.anywhereFuelAmount;
        }
    }

    public void startRocket() {
        // only start on the server side; ROCKET_STARTED is synced to client if successful
        if (this.isRemote()) return;

        // abort if there are no passengers
        Player player = this.getFirstPlayerPassenger();
        if (player == null) return;

        SynchedEntityData data = this.getEntityData();
        ItemStack config = this.configSlot.getStackInSlot(0);

        if (!config.isEmpty()) {
            // do not start the rocket twice
            if (data.get(RocketEntity.ROCKET_STARTED)) return;

            if (GCYRItems.ID_CHIP.isIn(config)) {
                this.setDestination(PlanetIdChipBehaviour.getPlanetFromStack(config));
            } else if (GCYRItems.KEYCARD.isIn(config)) {
                this.setDestination(KeyCardBehaviour.getSavedPlanet(config));
            }

            if (this.partsTier < this.getDestination().rocketTier()) {
                sendVehicleNotGoodEnoughMessage(player, this.getDestination().rocketTier());
                return;
            }

            long requiredFuel = computeRequiredFuelAmountForDestination(this.getDestination());
            if (this.fuelTank.getFluidAmount() < requiredFuel) {
                sendVehicleHasNoFuelMessage(player, this.fuelTank.getFluidAmount(), requiredFuel);
                return;
            }

            if (PlanetIdChipBehaviour.getSpaceStationId(config) != null || KeyCardBehaviour.getSavedStation(config) != null) {
                this.destinationIsSpaceStation = true;
            }

            // if the destination is the same as the current location, don't start
            if (!destinationIsSpaceStation && this.level().dimension() == this.getDestination().level()) return;

            data.set(RocketEntity.ROCKET_STARTED, true);
            //GCYRSoundEntries.ROCKET.play(this.level(), null, this.getX(), this.getY(), this.getZ(), 1, 1);
            this.level().playSound(null, this, GCYRSoundEntries.ROCKET.getMainEvent(), SoundSource.NEUTRAL, 1, 1);
        } else {
            sendVehicleHasInvalidIdMessage(player);
        }
    }

    // countdown returns true if the countdown is over, false otherwise
    public boolean countdown() {
        var timer = getStartTimer();
        if (timer < 200) {
            this.setStartTimer(timer+1);
        }
        return timer == 200;
    }

    // movement must happen both server + client side
    public void flightMovement() {
        var vec = getDeltaMovement();
        if (speed < getRocketSpeed() - 0.01) {
            speed += 0.05;
        }

        setDeltaMovement(vec.x, speed, vec.z);
    }

    public void fall() {
        if (this.isNoGravity()) return;
        Vec3 delta = this.getDeltaMovement();
        if (delta.y > -2.0) {
            delta = delta.add(0, -LivingEntity.DEFAULT_BASE_GRAVITY, 0);
        }
        this.setDeltaMovement(delta);

        // braking
        if (getControllingPassenger() != null && ((LivingEntityAccessor)getControllingPassenger()).isJumping() && consumeFuel()) {
            this.setDeltaMovement(delta.x, Math.min(delta.y + 0.05, -0.25), delta.z);
            this.fallDistance *= 0.9f;
            this.spawnParticles();
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (level().isClientSide()) return false;
        if (GCYRConfig.INSTANCE.rocket.doCrashLandingExplosion && fallDistance > 48 && onGround()) {
            Vec3 bbCenter = this.getBoundingBox().getCenter();
            this.unBuild();
            this.level().explode(this, bbCenter.x, this.getBoundingBox().minY, bbCenter.z, 10, EntityOxygenSystem.levelHasOxygen(this.level()), Level.ExplosionInteraction.MOB);
            return true;
        }
        return false;
    }

    public void burnEntities() {
        if (this.getStartTimer() == 200) {
            BlockPos size = this.entityData.get(SIZE);
            AABB aabb = AABB.ofSize(new Vec3(this.getX() + size.getX() / 2f, this.getY() - 2, this.getZ() + size.getZ() / 2f), size.getX() + 2, 2, size.getZ() + 2);
            List<LivingEntity> entities = this.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, aabb);

            for (LivingEntity entity : entities) {
                if (!entity.fireImmune() && !entity.hasEffect(MobEffects.FIRE_RESISTANCE) && !EntityTemperatureSystem.armourIsHeatResistant(entity)) {
                    entity.setSecondsOnFire(15);
                }
            }
        }
    }

    private boolean doesDrop(BlockState state, BlockPos pos) {
        if (this.onGround()) {
            BlockState state2 = this.level().getBlockState(new BlockPos((int)Math.floor(this.getX()), (int)(this.getY() - 0.2), (int)Math.floor(this.getZ())));
            if (!this.level().isEmptyBlock(pos) && (state2.is(GCYRBlocks.LAUNCH_PAD.get()) || !state.is(GCYRBlocks.LAUNCH_PAD.get()))) {
                this.unBuild();
                return true;
            }
        }
        return false;
    }

    protected void checkOnBlocks() {
        AABB aabb = this.getBoundingBox();
        BlockPos blockPos1 = new BlockPos((int) aabb.minX, (int) (aabb.minY - 0.2), (int) aabb.minZ);
        BlockPos blockPos2 = new BlockPos((int) aabb.maxX, (int) aabb.minY, (int) aabb.maxZ);

        //noinspection deprecation
        if (this.level().hasChunksAt(blockPos1, blockPos2)) {
            for (int i = blockPos1.getX(); i <= blockPos2.getX(); ++i) {
                for (int j = blockPos1.getY(); j <= blockPos2.getY(); ++j) {
                    for (int k = blockPos1.getZ(); k <= blockPos2.getZ(); ++k) {
                        BlockPos pos = new BlockPos(i, j, k);
                        BlockState state = this.level().getBlockState(pos);

                        if (this.doesDrop(state, pos)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean consumeFuel() {
        // Fuel Consumption = (thruster count + destination planet tier) / (fuel duration / 20 + 1 /*in case of bad recipe somehow*/) * 2
        int recipeDuration = this.getRecipeDuration();

        int destinationRocketTier = 1;
        if (this.getDestination() != null) {
            destinationRocketTier = this.getDestination().rocketTier();
        }

        var drain = (getThrusterCount() + destinationRocketTier) / (recipeDuration / 20.0 + 1) * 2;
        var ldrain = (int)drain;

        // estimate fuel consumption client side; client needs to know if
        // it should run the flight tick and if it should slow down in fall(),
        // but its fuelTank.isEmpty() = true
        if (isRemote()) {
            var fuelAmount = entityData.get(FUEL_AMOUNT);
            return (fuelAmount - ldrain) > 0;
        }

        if (!this.fuelTank.drain(ldrain, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
            return !this.fuelTank.drain(ldrain, IFluidHandler.FluidAction.EXECUTE).isEmpty();
        }
        return false;
    }

    @SuppressWarnings("DataFlowIssue")
    private void goToDestination() {
        if (getY() < 600) return;
        this.speed = 0.0;
        if (isRemote()) return;

        ItemStack configStack = configSlot.getStackInSlot(0);
        ItemStack satelliteStack = satelliteSlot.getStackInSlot(0);
        if (this.getDestination() == null && GCYRItems.ID_CHIP.isIn(configStack)) {
            this.setDestination(PlanetIdChipBehaviour.getPlanetFromStack(configStack));
        } else if (GCYRItems.KEYCARD.isIn(configStack) && KeyCardBehaviour.getSavedStation(configStack) != null) {
            this.destinationIsSpaceStation = true;
            // return if no valid station & no station kit
            if (!satelliteStack.is(GCYRItems.SPACE_STATION_PACKAGE.get()) && GCYRCapabilityHelper.getSpaceStations(this.getServer().getLevel(getDestination().orbitWorld())).getStation(KeyCardBehaviour.getSavedStation(configStack)) == null) {
                this.setDestination(null);
                this.destinationIsSpaceStation = false;
                this.entityData.set(ROCKET_STARTED, false);
                this.setDeltaMovement(0, -0.5, 0);
                return;
            }
        } else if (satelliteStack.is(GCYRTags.SATELLITES) && satelliteStack.getItem() instanceof ComponentItem componentItem) {
            for (IItemComponent component : componentItem.getComponents()) {
                if (component instanceof SatelliteItemBehaviour satelliteItem) {
                    this.returnToStart = true;
                    this.satelliteToLaunch = satelliteItem.type;
                }
            }
        }
        ResourceKey<Level> destinationDim = this.destinationIsSpaceStation ? getDestination().orbitWorld() : getDestination().level();

        // Go to a random valid planet if rocket doesn't have enough fuel to get to actual destination somehow.
        if (this.fuelTank.drain(computeRequiredFuelAmountForDestination(this.getDestination()) / 3, IFluidHandler.FluidAction.SIMULATE).isEmpty()) {
            List<Planet> validPlanets = new ArrayList<>();
            for (Planet planet : PlanetData.planets().values()) {
                if (planet.solarSystem().equals(this.getDestination().solarSystem())) {
                    validPlanets.add(planet);
                }
            }
            var destPlanet = validPlanets.get(GTValues.RNG.nextInt(validPlanets.size()));
            destinationDim = destPlanet.level();
        }

        final ServerLevel destinationLevel;
        BlockPos destinationPos = null;
        GlobalPos destinationData = PlanetIdChipBehaviour.getSavedPosition(configStack);
        if (destinationData != null) {
            destinationLevel = this.getServer().getLevel(destinationData.dimension());
            destinationPos = destinationData.pos();
        } else {
            destinationLevel = this.getServer().getLevel(destinationDim);
        }

        List<Entity> passengers = this.getPassengers();
        Entity newRocket;
        if (this.returnToStart) {
            newRocket = this;
        } else {
            newRocket = PlatformUtils.changeDimension(this, destinationLevel);
        }
        if (newRocket == null) {
            this.setDestination(null);
            this.destinationIsSpaceStation = false;
            this.entityData.set(ROCKET_STARTED, false);
            this.setDeltaMovement(0, -0.5, 0);
            return;
        }
        Set<Entity> newPassengers = new HashSet<>();
        passengers.forEach(passenger -> {
            Entity newPassenger = PlatformUtils.changeDimension(passenger, destinationLevel);
            if (newPassenger != null) {
                newPassenger.startRiding(newRocket);
                newPassengers.add(newPassenger);
            } else {
                passenger.startRiding(newRocket);
                newPassengers.add(passenger);
            }
        });

        Vec3 pos = this.position();
        if (this.destinationIsSpaceStation) {
            ISpaceStationHolder stations = GCYRCapabilityHelper.getSpaceStations(destinationLevel);
            Integer stationId;
            boolean didChange = false;
            if (GCYRItems.KEYCARD.isIn(configStack)) {
                stationId = KeyCardBehaviour.getSavedStation(configStack);
                if (stations.getStation(stationId) == null) {
                    Pair<Integer, SpaceStation> allocated = stations.allocateStation(this.getDestination());
                    stations.addStation(allocated.getFirst(), allocated.getSecond());
                    stationId = allocated.getFirst();
                    KeyCardBehaviour.setSavedStation(configStack, stationId, KeyCardBehaviour.getSavedPlanet(configStack));
                    didChange = true;
                }
            } else if (GCYRItems.ID_CHIP.isIn(configStack)) {
                stationId = PlanetIdChipBehaviour.getSpaceStationId(configStack);
                if (stations.getStation(stationId) == null) {
                    Pair<Integer, SpaceStation> allocated = stations.allocateStation(this.getDestination());
                    stations.addStation(allocated.getFirst(), allocated.getSecond());
                    stationId = allocated.getFirst();
                    PlanetIdChipBehaviour.setSpaceStation(configStack, stationId);
                    didChange = true;
                }
            } else {
                stationId = null;
            }

            if (didChange) {
                newPassengers.forEach(entity -> {
                    if (entity instanceof Player player) {
                        player.sendSystemMessage(Component.translatable("message.gcyr.notice_id_changed"));
                    }
                });
            }

            if (destinationPos == null) {
                BlockPos stationPos = stations.getStationWorldPos(stationId);
                destinationPos = new BlockPos(stationPos.getX(), (int) pos.y, stationPos.getZ());
            }

            if (newRocket instanceof RocketEntity rocketEntity && GCYRItems.SPACE_STATION_PACKAGE.isIn(this.satelliteSlot.getStackInSlot(0))) {
                ItemStack stack = this.satelliteSlot.getStackInSlot(0);
                rocketEntity.buildSpaceStation(stack, new BlockPos(destinationPos.getX(), 64, destinationPos.getZ()));
            }
        } else {
            double scale = DimensionType.getTeleportationScale(this.level().dimensionType(), destinationLevel.dimensionType());
            if (destinationPos == null) {
                destinationPos = BlockPos.containing(pos.multiply(scale, 1, scale));
            }
        }
        newRocket.setPos(destinationPos.getX(), destinationPos.getY(), destinationPos.getZ());

        Vec3 delta = this.getDeltaMovement();
        newRocket.setDeltaMovement(delta.x, -0.5, delta.z);
        if (newRocket instanceof RocketEntity rocketEntity) {
            rocketEntity.setDestination(null);
            rocketEntity.destinationIsSpaceStation = false;
            rocketEntity.entityData.set(ROCKET_STARTED, false);
            rocketEntity.entityData.set(START_TIMER, 0);
        }
    }

    public void unBuild() {
        if (this.level().isClientSide) return;

        if (!configSlot.getStackInSlot(0).isEmpty())
            this.spawnAtLocation(configSlot.getStackInSlot(0));
        if (!satelliteSlot.getStackInSlot(0).isEmpty())
            this.spawnAtLocation(satelliteSlot.getStackInSlot(0));

        BlockPos origin = this.blockPosition();
        for (PosWithState state : this.getBlocks()) {
            BlockPos offset = origin.offset(state.pos());
            BlockHitResult result = new BlockHitResult(
                    offset.getCenter(),
                    Direction.DOWN,
                    offset,
                    false
            );
            if (!this.level().getBlockState(offset).isAir() && !this.level().getBlockState(offset).canBeReplaced(new BlockPlaceContext(this.level(), null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, result))) {
                this.spawnAtLocation(state.state().getBlock().asItem());
                continue;
            }
            this.level().setBlock(offset, state.state(), Block.UPDATE_ALL);
            if (state.entityTag() == null) continue;
            BlockEntity blockEntity = level().getBlockEntity(offset);
            if (blockEntity != null) {
                blockEntity.load(state.entityTag());
            }
        }

        this.remove(RemovalReason.DISCARDED);
    }

    private void buildSpaceStation(ItemStack stack, BlockPos origin) {
        if (!GCYRItems.SPACE_STATION_PACKAGE.isIn(stack)) return;
        Set<PosWithState> blocks = StationContainerBehaviour.getStationBlocks(stack);
        if (blocks == null || blocks.isEmpty()) return;

        boolean start = true;
        BlockPos original = origin;
        for (PosWithState state : blocks) {
            BlockPos pos = state.pos();
            if (start) origin = original.offset(pos);
            start = false;
            if (origin.compareTo(pos.offset(original)) > 0) origin = new BlockPos(
                    Math.min(original.getX() - pos.getX(), origin.getX()),
                    Math.min(original.getY() - pos.getY(), origin.getY()),
                    Math.min(original.getZ() - pos.getZ(), origin.getZ()));
        }

        for (PosWithState state : blocks) {
            BlockPos offset = origin.offset(state.pos());
            BlockState originalState = this.level().getBlockState(offset);
            if (!originalState.isAir() && !originalState.canBeReplaced()) {
                this.spawnAtLocation(state.state().getBlock().asItem());
                continue;
            }
            this.level().setBlock(offset, state.state(), Block.UPDATE_ALL);
            if (state.entityTag() == null) continue;
            BlockEntity blockEntity = level().getBlockEntity(offset);
            if (blockEntity != null) {
                blockEntity.load(state.entityTag());
            }
        }
    }

    public void placeSatellite() {

    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void push(Entity entity) {

    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    public int getFuelCapacity() {
        return this.entityData.get(FUEL_CAPACITY);
    }

    public void setFuelCapacity(int fuelCapacity) {
        this.entityData.set(FUEL_CAPACITY, fuelCapacity);
        this.reinitializeFluidStorage();
    }

    public int getThrusterCount() {
        return this.entityData.get(THRUSTER_COUNT);
    }

    public void setThrusterCount(int count) {
        this.entityData.set(THRUSTER_COUNT, count);
    }

    public int getRecipeDuration() {
        return this.entityData.get(RECIPE_DURATION);
    }

    public void setRecipeDuration(int duration) {
        this.entityData.set(RECIPE_DURATION, duration);
    }

    public int getWeight() {
        return this.entityData.get(WEIGHT);
    }

    public void setWeight(int weight) {
        this.entityData.set(WEIGHT, weight);
    }

    public int getStartTimer() {
        return this.entityData.get(START_TIMER);
    }

    public void setStartTimer(int timer) {
        this.entityData.set(START_TIMER, timer);
    }

    public void addBlock(BlockPos pos, BlockState state, @Nullable CompoundTag entityTag) {
        this.addBlock(new PosWithState(pos, state, entityTag));
    }

    public void setDestination(@Nullable Planet destination) {
        this.entityData.set(DESTINATION, Optional.ofNullable(destination));
    }

    @Nullable
    public Planet getDestination() {
        return this.entityData.get(DESTINATION).orElse(null);
    }

    public void addBlock(PosWithState state) {
        List<PosWithState> blocks = this.getBlocks();
        if (blocks.stream().anyMatch(pws -> pws.pos().equals(state.pos()))) {
            return;
        }

        blocks.add(state);

        this.setBlocks(blocks);
        BlockPos pos = state.pos();
        BlockPos size = this.entityData.get(SIZE);
        this.entityData.set(SIZE, new BlockPos(
                        Math.max(size.getX(), pos.getX()),
                        Math.max(size.getY(), pos.getY()),
                        Math.max(size.getZ(), pos.getZ())
        ));
        Block block = state.state().getBlock();
        float destroyTime = block.defaultDestroyTime();
        if (destroyTime > 0) {
            this.setWeight(this.getWeight() + (int) (destroyTime / 2.5f));
        }

        // count parts
        if (block instanceof IRocketPart part) {
            this.partCounts.put(part, this.partCounts.getOrDefault(part, 0) + 1);
        }

        if (block instanceof RocketMotorBlock rocketMotorBlock) {
            this.setThrusterCount(this.getThrusterCount() + rocketMotorBlock.getMotorType().getMotorCount());
            this.thrusterPositions.add(pos);

            // resolve average tier of used motors
            this.motorTiersTotal += rocketMotorBlock.getTier();
            this.motorTier = this.motorTiersTotal / this.partCounts.object2IntEntrySet()
                    .stream()
                    .filter(p -> p.getKey() instanceof RocketMotorBlock)
                    .map(Map.Entry::getValue)
                    .reduce(0, Integer::sum);
        } else if (block instanceof FuelTankBlock fuelTankBlock) {
            this.setFuelCapacity(this.getFuelCapacity() + fuelTankBlock.getTankProperties().getFuelStorage());

            // resolve average tier of used fuel tanks
            this.fuelTankTiersTotal += fuelTankBlock.getTier();
            this.fuelTankTier = this.fuelTankTiersTotal / this.partCounts.object2IntEntrySet()
                    .stream()
                    .filter(p -> p.getKey() instanceof FuelTankBlock)
                    .map(Map.Entry::getValue)
                    .reduce(0, Integer::sum);
        } else if (state.state().is(GCYRBlocks.SEAT.get())) {
            this.addSeatPos(pos);
        }
        // resolve average tier of parts
        this.partsTier = (this.motorTier + this.fuelTankTier) / 2;

        this.setBoundingBox(makeBoundingBox());
    }

    public List<PosWithState> getBlocks() {
        return this.entityData.get(POSITIONED_STATES);
    }

    public void setBlocks(List<PosWithState> blocks) {
        this.entityData.set(POSITIONED_STATES, blocks, true);
    }

    public void addSeatPos(BlockPos pos) {
        List<BlockPos> seats = this.entityData.get(SEAT_POSITIONS);
        seats.add(pos);
        this.entityData.set(SEAT_POSITIONS, seats, true);
    }

    public List<BlockPos> getSeatPositions() {
        return this.entityData.get(SEAT_POSITIONS);
    }

    public String getDisplayThrust() {
        ChatFormatting style;
        var thrust = getRocketSpeed();

        if (thrust < 0.01) {
            style = ChatFormatting.RED;
        } else if (thrust < 1.01) {
            style = ChatFormatting.GOLD;
        } else {
            style = ChatFormatting.GREEN;
        }
        var thrustComponent = Component.literal(String.format("%.1f", thrust)).withStyle(style);
        return Component.translatable("menu.gcyr.rocket.thrust", thrustComponent).getString();
    }

    public double getRocketSpeed() {
        return this.getThrusterCount() * 4.0 - (getWeight() + 1);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(ROCKET_STARTED, false);
        this.entityData.define(FUEL_CAPACITY, 0);
        this.entityData.define(THRUSTER_COUNT, 0);
        this.entityData.define(WEIGHT, 0);
        this.entityData.define(RECIPE_DURATION, 0);
        this.entityData.define(START_TIMER, 0);
        this.entityData.define(FUEL_AMOUNT, 0);
        this.entityData.define(POSITIONED_STATES, new ArrayList<>());
        this.entityData.define(SEAT_POSITIONS, new ArrayList<>());
        this.entityData.define(SIZE, BlockPos.ZERO);
        this.entityData.define(DESTINATION, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.getBlocks().clear();
        ListTag blocks = compound.getList("blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < blocks.size(); ++i) {
            this.addBlock(PosWithState.readFromTag(blocks.getCompound(i)));
        }

        this.setFuelCapacity(compound.getInt("fuelCapacity"));
        this.fuelTank.setFluid(FluidStack.loadFluidStackFromNBT(compound.getCompound("fuel")));
        this.configSlot.deserializeNBT(compound.getCompound("config"));
        this.returnToStart = compound.getBoolean("returnToStart");
        if (compound.contains("satelliteToLaunch")) {
            this.satelliteToLaunch = GCYRRegistries.SATELLITES.get(new ResourceLocation(compound.getString("satelliteToLaunch")));
        }
        this.setThrusterCount(compound.getInt("thrusterCount"));
        this.setStartTimer(compound.getInt("startTimer"));
        this.entityData.set(ROCKET_STARTED, compound.getBoolean("isStarted"));
        this.setWeight(compound.getInt("weight"));
        this.setDestination(compound.contains("destination", Tag.TAG_STRING) ? PlanetData.getPlanet(new ResourceLocation(compound.getString("destination"))) : null);
        if (compound.contains("selectedFuelRecipe")) this.selectedFuelRecipe = (GTRecipe) this.getServer().getRecipeManager().byKey(new ResourceLocation(compound.getString("selectedFuelRecipe"))).orElse(null);

        if (compound.contains("recipeDuration")) {
            this.setRecipeDuration(compound.getInt("recipeDuration"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        var blocks = this.getBlocks();
        ListTag blockTag = new ListTag();
        compound.put("blocks", blockTag);
        for (PosWithState state : blocks) {
            blockTag.add(state.writeToTag());
        }

        compound.putLong("fuelCapacity", this.getFuelCapacity());
        CompoundTag fuel = new CompoundTag();
        fuelTank.getFluid().writeToNBT(fuel);
        compound.put("fuel", fuel);
        compound.put("config", this.configSlot.serializeNBT());
        compound.putBoolean("returnToStart", this.returnToStart);
        if (this.satelliteToLaunch != null) {
            compound.putString("satelliteToLaunch", GCYRRegistries.SATELLITES.getKey(satelliteToLaunch).toString());
        }
        compound.putInt("thrusterCount", this.getThrusterCount());
        compound.putInt("startTimer", this.getStartTimer());
        compound.putBoolean("isStarted", this.entityData.get(ROCKET_STARTED));
        compound.putInt("weight", this.getWeight());
        if (this.getDestination() != null) compound.putString("destination", PlanetData.getPlanetId(getDestination()).toString());
        if (this.selectedFuelRecipe != null) compound.putString("selectedFuelRecipe", selectedFuelRecipe.id.toString());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (POSITIONED_STATES.equals(key) || SIZE.equals(key)) {
            this.setBoundingBox(makeBoundingBox());
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buf) {
        GCYREntityDataSerializers.POSITIONED_BLOCK_STATE_LIST.write(buf, getBlocks());
        GCYREntityDataSerializers.BLOCK_POS_LIST.write(buf, getSeatPositions());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buf) {
        setBlocks(GCYREntityDataSerializers.POSITIONED_BLOCK_STATE_LIST.read(buf));
        this.entityData.set(SEAT_POSITIONS, GCYREntityDataSerializers.BLOCK_POS_LIST.read(buf));
    }

    public static void setEntityRotation(Entity vehicle, float rotation) {
        vehicle.setYRot(vehicle.getYRot() + rotation);
        vehicle.setYBodyRot(vehicle.getYRot());
        vehicle.yRotO = vehicle.getYRot();
    }

    public static void sendVehicleHasNoFuelMessage(Player player, long fuel, long required) {
        if (!player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("message.gcyr.no_fuel", fuel, required), false);
        }
    }

    public static void sendVehicleHasInvalidIdMessage(Player player) {
        if (!player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("message.gcyr.invalid_id"), false);
        }
    }

    public static void sendVehicleNotGoodEnoughMessage(Player player, int planetTier) {
        if (!player.level().isClientSide) {
            player.displayClientMessage(Component.translatable("message.gcyr.rocket_not_good_enough", planetTier), false);
        }
    }

    @Override
    public boolean isInvalid() {
        return this.isRemoved();
    }

    @Override
    public boolean isRemote() {
        return this.level().isClientSide;
    }

    @Override
    public void markAsDirty() {

    }
}
