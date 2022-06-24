package com.joekeen03.yggdrasil.world.structure.tree;

import com.joekeen03.yggdrasil.util.Helpers;
import com.joekeen03.yggdrasil.util.StemVec3d;

import java.util.Random;

public class TreeModel {
    public final TreeBranch trunk;
    private final TreeTypeParams treeType;
    public static final int nLeafLevels = 3; // FIXME should be in TreeTypeParams
    public static final double leafBranchRadius = 0.5/2;

    public TreeModel(Random treeRandom, StemVec3d origin, StemVec3d trunkBaseZUnit, TreeTypeParams treeTypeParams) {
        this.treeType = treeTypeParams;
        // Generate trunk - recursive level 0

        // Angle the trunk's x-axis is, relative to the absolute axes. Needed, b/c rotations are generally about the x-axis
        double xAngle = Helpers.randDoubleRange(treeRandom, -Math.PI, Math.PI);
        StemVec3d xUnit = new StemVec3d(Math.cos(xAngle), Math.sin(xAngle), 0);

        double treeScale = treeTypeParams.scale + randDoubleVariation(treeRandom, treeTypeParams.scaleVariation);
        double length = (treeTypeParams.trunkParams.length + randDoubleVariation(treeRandom, treeTypeParams.trunkParams.lengthVariation)) * treeScale;
        double stemRadius = length * treeTypeParams.trunkParams.scale * treeTypeParams.ratio;
        // Paper defines it as scale*baseSize, but that would give a fractional length - which doesn't make sense for
        //  how it's used
        double lengthBase = length*treeTypeParams.baseSize;

        // FIXME - Reference stores segments as nearly-circular circles between each segment, which it then connects
        //  to create an intermediate mesh; can this be adapted to work with my tapered cylinder segments?
        //  Each cylinder segment would also need to store the cut angles for the ends.
        //  I mean, I can "easily" represent the segments as tapered cylinders, so it's really a question of, is there
        //  any benefit to storing the segments as the start/stop circles, instead?
        //  Maybe not so easy to represent the segments as tapered cyilinders...there does seem to be a reason they
        //  store segments as the cross-sections
        //  Options:
        //      -Store circular cross-sections, figure out a way to stretch between them
        //          How to do the "stretching"?
        //          Not doing this
        //      -Store tapered cylinders
        //          Simple, probably use this to get the algorithm working.
        //      -Store cones with the ends sliced off by flat planes
        //          Cylinders will join seamlessly, a bit more involved than the tapered cylinders
        //      -Store as curved, tapered cylinders.
        //          Fairly seamless, but no idea how to handle these. Also, might look too smooth?
        //          Probably go with this ultimately.
        //  And then how would I handle branches?
        //      The way the paper seems to handle them is by just placing the first cross-section for each branch where
        //      it should be (?), then stretching a mess from each branch's cross-section to the base; i.e., each branch
        //      is just my truncated cones idea, and they just join in the middle. Just do that, or would I want some
        //      way to handle this with my curved cylinders?

        int nChildren = 0;
        if (treeTypeParams.levels > 0) {
            nChildren = ((BranchParams) treeTypeParams.fetchParams(1)).branches;
        }
        int firstCutoffLevel = Integer.MAX_VALUE;
        double cutoffRadius = Double.POSITIVE_INFINITY;
        if (treeTypeParams.leafParams.leaves > 0) {
            firstCutoffLevel = treeTypeParams.levels-nLeafLevels;
            cutoffRadius = leafBranchRadius;
        }

        this.trunk = new TreeBranch(origin, trunkBaseZUnit, xUnit,
                length, stemRadius, nChildren,
                0, new TreeCreationParams(treeRandom, treeTypeParams, xUnit,
                trunkBaseZUnit.crossProduct(xUnit).normalize(), trunkBaseZUnit, lengthBase,
                firstCutoffLevel, cutoffRadius));
    }

    /**
     * Returns a random number in the range [-variation, variation) (verify?).
     * @param random
     * @param variation
     * @return
     */
    public static double randDoubleVariation(Random random, double variation) {
        // Is the variation supposed to be any value in the range [-variation, variation], or is it just supposed to be
        //  +/- variation (random sign, fixed magnitude)?
        return Helpers.randDoubleRange(random, -variation, variation);
    }

    public static class TreeCreationParams {
        public final Random treeRandom;
        public final TreeTypeParams treeParams;
        public final StemVec3d xUnitTrunkBase, yUnitTrunkBase, zUnitTrunkBase;
        public final double lengthBase, cutoffRadius;
        public final int firstCutoffLevel;
        public final SegSplitError[] splitErrors;

        public TreeCreationParams(Random treeRandom, TreeTypeParams treeParams, StemVec3d xUnitTrunkBase,
                                  StemVec3d yUnitTrunkBase, StemVec3d zUnitTrunkBase, double lengthBase,
                                  int firstCutoffLevel, double cutoffRadius) {
            this.treeRandom = treeRandom;
            this.treeParams = treeParams;
            this.xUnitTrunkBase = xUnitTrunkBase;
            this.yUnitTrunkBase = yUnitTrunkBase;
            this.zUnitTrunkBase = zUnitTrunkBase;
            this.lengthBase = lengthBase;
            this.firstCutoffLevel = firstCutoffLevel;
            this.cutoffRadius = cutoffRadius;
            int nBranchLevels = treeParams.stemLevels;
            this.splitErrors = new SegSplitError[nBranchLevels];
            for (int i = 0; i < nBranchLevels; i++) {
                // Works, because the treeTypeParams guarantees that there are at least as many StemParams as there
                // are levels - unlike the paper, this doesn't reuse the last level of specified params for all further levels.
                this.splitErrors[i] = treeParams.fetchParams(i).initializeError(treeRandom);
            }
        }

        public SegSplitError fetchSegError(int level) {
            return splitErrors[level];
        }
    }
}
