package argent_matter.gcyr.common.machine.multiblock;

import argent_matter.gcyr.common.data.GCYRBlocks;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.BlockPattern;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import static com.gregtechceu.gtceu.common.data.GTMaterialBlocks.MATERIAL_BLOCKS;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Arrays;

import static com.gregtechceu.gtceu.api.pattern.Predicates.*;

public class PlatformMultiblockMachine extends MultiblockControllerMachine implements IDisplayUIMachine {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(PlatformMultiblockMachine.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);

    public static final int MAX_RADIUS = 8;
    public static final int MIN_RADIUS = 1;
    public static final int MIN_TOWER_HEIGHT = 2;
    public static final int MAX_TOWER_HEIGHT = 15;

    @Persisted
    protected int lDist = 0, rDist = 0, bDist = 0, hDist = 0;

    public PlatformMultiblockMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    public void updateStructureDimensions() {
        Level world = getLevel();
        Direction front = getFrontFacing();
        Direction back = front.getOpposite();
        Direction left = front.getCounterClockWise();
        Direction right = left.getOpposite();

        BlockPos pos = getPos().mutable().move(back, 1).move(Direction.DOWN, 1);
        BlockPos.MutableBlockPos lPos = pos.mutable();
        BlockPos.MutableBlockPos rPos = pos.mutable();
        BlockPos.MutableBlockPos bPos = pos.mutable();
        BlockPos.MutableBlockPos hPos = pos.mutable();

        // find the distances from the controller to the launch pad blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int hDist = 0;

        // find the left, right, back, and front distances for the structure pattern
        // maximum size is 15x15x15 including walls, so check 7 block radius around the controller for blocks
        for (int i = 1; i < MAX_RADIUS; i++) {
            if (lDist == 0 && isBlockEdge(world, lPos, left))  lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back))  bDist = i + 1; // +1 because the controller isn't in the middle, unlike the cleanroom.
            if (lDist != 0 && rDist != 0 && bDist != 0) break;
        }

        hPos.move(back, bDist);
        // height is diameter instead of radius, so it needs to be done separately
        for (int i = 1; i < MAX_TOWER_HEIGHT; i++) {
            if (isBlockTowerEnd(world, hPos, Direction.UP)) hDist = i;
            if (hDist != 0) break;
        }

        if (lDist < MIN_RADIUS || rDist < MIN_RADIUS || bDist < MIN_RADIUS || hDist < MIN_TOWER_HEIGHT) {
            this.isFormed = false;
            return;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;
        this.hDist = hDist;
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid wall block at pos moved in direction
     */
    public boolean isBlockEdge(@Nonnull Level world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull Direction direction) {
        return world.getBlockState(pos.move(direction)).is(GCYRBlocks.LAUNCH_PAD.get()) && !world.getBlockState(pos.relative(direction, 1)).is(GCYRBlocks.LAUNCH_PAD.get());
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid floor block at pos moved in direction
     */
    public boolean isBlockTowerEnd(@Nonnull Level world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull Direction direction) {
        return world.getBlockState(pos.move(Direction.UP, 1)).is(MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.StainlessSteel).get()) && !world.getBlockState(pos.relative(Direction.UP, 1)).is(MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.StainlessSteel).get());
    }

    @Override
    public BlockPattern getPattern() {
        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        //noinspection ConstantValue
        if (getLevel() != null) updateStructureDimensions();

        // these can sometimes get set to 0 when loading the game, breaking JEI
        if (lDist < MIN_RADIUS) lDist = MIN_RADIUS;
        if (rDist < MIN_RADIUS) rDist = MIN_RADIUS;
        if (bDist < MIN_RADIUS) bDist = MIN_RADIUS;
        if (hDist < MIN_TOWER_HEIGHT) hDist = MIN_TOWER_HEIGHT;

        if (this.getFrontFacing() == Direction.EAST || this.getFrontFacing() == Direction.WEST) {
            int tmp = lDist;
            lDist = rDist;
            rDist = tmp;
        }
        // build each row of the structure
        StringBuilder backBuilder = new StringBuilder();        // "  K  "
        StringBuilder controllerBuilder = new StringBuilder();  // "  S  "
        StringBuilder emptyBuilder = new StringBuilder();       // "     "
        StringBuilder fullBuilder = new StringBuilder();        // "BBBBB"

        // everything to the left of the controller
        for (int i = 0; i < lDist; i++) {
            backBuilder.append(" ");
            controllerBuilder.append(" ");
            emptyBuilder.append(" ");
            fullBuilder.append("B");
        }

        // everything in-line with the controller
        backBuilder.append("K");
        controllerBuilder.append("S");
        emptyBuilder.append(" ");
        fullBuilder.append("B");

        // everything to the right of the controller
        for (int i = 0; i < rDist; i++) {
            backBuilder.append(" ");
            controllerBuilder.append(" ");
            emptyBuilder.append(" ");
            fullBuilder.append("B");
        }

        // build each slice of the structure
        String[] back = new String[hDist]; // "     ", "  K  ", "  K  ", "  K  ", "  K  "
        Arrays.fill(back, backBuilder.toString());
        back[0] = emptyBuilder.toString();

        String[] center = new String[hDist]; // "BBBBB", "     ", "     ", "     ", "     "
        Arrays.fill(center, emptyBuilder.toString());
        center[0] = fullBuilder.toString();

        String[] front = Arrays.copyOf(center, center.length); // "     ", "  S  ", "     ", "     ", "     "
        front[0] = emptyBuilder.toString();
        front[1] = controllerBuilder.toString();

        TraceabilityPredicate towerPredicate = blocks(MATERIAL_BLOCKS.get(TagPrefix.frameGt, GTMaterials.StainlessSteel).get());
        TraceabilityPredicate basePredicate = Predicates.autoAbilities(true, false, false)
                .or(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3));

        // layer the slices one behind the next
        return FactoryBlockPattern.start()
                .aisle(back)
                .aisle(center).setRepeatable(bDist)
                .aisle(front)
                .where('S', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .where('B', blocks(GCYRBlocks.LAUNCH_PAD.get()))
                .where('K', towerPredicate)
                .where(' ', any())
                .build();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

}
