package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;

public class LeafBranch implements GenerationFeature {
    private final TreeSegmentNode branchNode;
    private final Vec3d adjustment;
    private TreeSegmentGenerated generatedSectionCache;

    public LeafBranch(TreeSegmentNode branchNode, Vec3d adjustment) {
        this.branchNode = branchNode;
        this.adjustment = adjustment;
    }

    /*
    Implementations:
    -This handles only the last branch level - the one with the leaves coming off of it - and just places leaf blocks
        along its length. Simple, but not that great? Leaves will come off of very thin branches, which in turn come off
        slightly less thing branches, and so on - how would those slightly less thin branches be handled, if they're
        not closer to a block in diameter?
        I guess, the question is
    -Above, but handles the last few levels - all below a certain radius?
        That might work alright.
     */

    @Override
    @Nonnull
    public IntegerMinimumAABB getMinimumBoundingBox() {
        throw new NotImplementedException("LeafBranch hasn't yet implemented getMinimumBoundingBox.");
//        return null;
    }

    @Override
    public boolean intersectsCube(CubePos pos) {
        return true;
    }

    @Override
    public void generate(CubePrimer cubePrimer, CubePos pos) {
        int bufferSize = ICube.SIZE+2;
        byte[][][] buffer = new byte[bufferSize][bufferSize][bufferSize];
        /*
        Logic:
            Traverse the tree, placing wood blocks at all branch positions; then, go back, and place leaf blocks at all leaf positions.
            ...What if there's only one leaf in a cube? What if the majority of a cube should be a wood block (could that happen?)?
            Maybe just place leaf blocks at every leafy branch's position (instead of computing each and every leaf's position).
            More sophisticated algorithm:
                -For each woody branch going through a block, add the volume of it contained w/in that block to a "woodiness"
                    counter for the block.
                -Likewise for the leafy branches, just for a "leafyness" counter
                -After all branches have been checked, check their leafyness and woodyness - if at least one of them is
                    above a specified fill threshold, then set the block to the block type corresponding to the higher
                    of those two.
         */
    }
}
