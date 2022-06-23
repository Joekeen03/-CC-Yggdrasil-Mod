package com.joekeen03.yggdrasil.world.structure.tree;

import com.joekeen03.yggdrasil.util.StemVec3d;

public class TreeBranch {
    public final TreeSegment firstSegment;

    public TreeBranch(StemVec3d origin, StemVec3d zUnit, StemVec3d xUnit,
                      double length, double baseRadius, int nChildren,
                      int level, TreeModel.TreeCreationParams treeCreationParams) {
        double branchDistance = length/nChildren;
        double firstChildOffset = branchDistance/2;
        if (level == 0) {
            branchDistance = (length-treeCreationParams.lengthBase)/(nChildren-1)*.999; // So the last branch ends up at the trunk's end
            // FIXME ensuring the last branch ends up at the end of the trunk isn't always applicable
            firstChildOffset = treeCreationParams.lengthBase;
        }
        StemParams branch = treeCreationParams.treeParams.fetchParams(level);
        firstSegment = TreeSegment.createSegment(origin, zUnit, xUnit,
                baseRadius, 0, firstChildOffset, 0,
                0,
                treeCreationParams,
                new BranchCreationParams(baseRadius, length/branch.curveRes, length,
                        branchDistance, level, treeCreationParams));
    }

    public static class BranchCreationParams {
        public final double stemRadius;
        public final double lengthFraction;
        public final double branchLength;
        public final double branchDistance;
        public final int level;
        public final StemParams currBranch;
        public final SegSplitError segSplitError;

        public BranchCreationParams(double stemRadius, double lengthFraction, double branchLength,
                                    double branchDistance, int level, TreeModel.TreeCreationParams treeCreationParams) {
            this.stemRadius = stemRadius;
            this.lengthFraction = lengthFraction;
            this.branchLength = branchLength;
            this.branchDistance = branchDistance;
            this.level = level;
            this.currBranch = treeCreationParams.treeParams.fetchParams(level);
            this.segSplitError = treeCreationParams.fetchSegError(level);
        }
    }
}
