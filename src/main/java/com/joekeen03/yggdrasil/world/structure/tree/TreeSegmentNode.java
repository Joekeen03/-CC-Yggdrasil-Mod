package com.joekeen03.yggdrasil.world.structure.tree;

import com.joekeen03.yggdrasil.util.InvalidValueException;
import com.joekeen03.yggdrasil.util.StemVec3d;

import java.util.Random;

public class TreeSegmentNode extends TreeSegment {
    public final StemVec3d xUnit;
    public final double nextChildOffset, lastChildRotateAngle, remainingCorrection;
    public final int i;
    public final long seed;
    public final TreeModel.TreeCreationParams treeCreationParams;
    public final TreeBranch.BranchCreationParams branchCreationParams;

    public TreeSegmentNode(StemVec3d origin, StemVec3d zUnit, StemVec3d xUnit, double prevRadiusZ, int i,
                           double nextChildOffset, double lastChildRotateAngle, double remainingCorrection,
                           long seed, TreeModel.TreeCreationParams treeCreationParams, TreeBranch.BranchCreationParams branchCreationParams) {
        super(origin, zUnit, prevRadiusZ);
        this.xUnit = xUnit;
        this.nextChildOffset = nextChildOffset;
        this.lastChildRotateAngle = lastChildRotateAngle;
        this.remainingCorrection = remainingCorrection;
        this.i = i;
        this.seed = seed;
        this.treeCreationParams = treeCreationParams;
        this.branchCreationParams = branchCreationParams;
    }

    public TreeSegmentGenerated create() {
        TreeModel.TreeCreationParams thisParams = new TreeModel.TreeCreationParams(new Random(seed), treeCreationParams.treeParams,
                treeCreationParams.xUnitTrunkBase, treeCreationParams.yUnitTrunkBase, treeCreationParams.zUnitTrunkBase,
                treeCreationParams.lengthBase, Integer.MAX_VALUE, Double.NEGATIVE_INFINITY);
        TreeSegment segment = TreeSegment.createSegment(origin, zUnit, xUnit, baseRadius, i, nextChildOffset, lastChildRotateAngle, remainingCorrection, thisParams,
                branchCreationParams);
        if (segment instanceof TreeSegmentGenerated) {
            return (TreeSegmentGenerated) segment;
        } else {
            throw new InvalidValueException("TreeSegment.createSegment returned something other than a "
                    +"TreeSegmentGenerated instance in TreeSegmentNode::create()");
        }
    }
}
