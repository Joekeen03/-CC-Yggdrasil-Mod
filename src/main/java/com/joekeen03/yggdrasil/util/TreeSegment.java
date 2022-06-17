package com.joekeen03.yggdrasil.util;

// FIXME is there a sane way to implement this?
public abstract class TreeSegment {
    public final StemVec3d origin, zUnit;
    public final double baseRadius;

    public TreeSegment(StemVec3d origin, StemVec3d zUnit, double baseRadius) {
        this.origin = origin;
        this.zUnit = zUnit;
        this.baseRadius = baseRadius;
    }

    static double computeNextRadiusZ(TreeBranch.BranchCreationParams branchCreationParams, double positionFraction) {
        if (branchCreationParams.currBranch.taper < 0) {
            throw new InvalidValueException("taper_0 cannot be negative.");
        } else if (branchCreationParams.currBranch.taper <= 1) {
            // Had issues where it returned a (very slightly) negative radius due to precision limits on
            //  floating-point math, for certain methods of computing the parameters. Should I add a check for that?
            double unit_taper = branchCreationParams.currBranch.taper;
            double taperZ = branchCreationParams.stemRadius*(1-unit_taper*positionFraction);
            return taperZ;
        } else {
            throw new InvalidValueException("Program does not currently handle taper values greater than 1.");
        }
    }

    static boolean isCutoff(TreeModel.TreeCreationParams treeCreationParams, int level, double nextRadius) {
        return (level >= treeCreationParams.firstCutoffLevel && nextRadius <= treeCreationParams.cutoffRadius);
    }

    static TreeSegment createSegment(StemVec3d origin, StemVec3d zUnit, StemVec3d xUnit,
                                     double prevRadiusZ, int i, double nextChildOffset, double lastChildRotateAngle,
                                     double remainingCorrection,
                                     TreeModel.TreeCreationParams treeCreationParams, TreeBranch.BranchCreationParams branchCreationParams) {
        if (isCutoff(treeCreationParams, branchCreationParams.level, computeNextRadiusZ(branchCreationParams,
                ((double) i+1)/branchCreationParams.currBranch.curveRes))) {
            long seed = treeCreationParams.treeRandom.nextLong();
            return new TreeSegmentNode(origin, zUnit, xUnit, prevRadiusZ, i, nextChildOffset, lastChildRotateAngle, remainingCorrection, seed, treeCreationParams, branchCreationParams);
        } else {
            return new TreeSegmentGenerated(origin, zUnit, xUnit,
                    prevRadiusZ, i, nextChildOffset, lastChildRotateAngle, remainingCorrection,
                    treeCreationParams, branchCreationParams);
        }
    }
}
