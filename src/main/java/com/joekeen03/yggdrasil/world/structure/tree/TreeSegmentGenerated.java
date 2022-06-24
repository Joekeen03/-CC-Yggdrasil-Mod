package com.joekeen03.yggdrasil.world.structure.tree;

import com.joekeen03.yggdrasil.util.Helpers;
import com.joekeen03.yggdrasil.util.InvalidValueException;
import com.joekeen03.yggdrasil.util.StemVec3d;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;

public class TreeSegmentGenerated extends TreeSegment {
    public final TreeSegment[] nextSegments;
    public final TreeBranch[] children;
    public final double endRadius, length;
    public final int level;

    TreeSegmentGenerated(StemVec3d origin, StemVec3d zUnit, StemVec3d xUnit,
                         double prevRadiusZ, int i, double nextChildOffset, double lastChildRotateAngle,
                         double remainingCorrection,
                         TreeModel.TreeCreationParams treeCreationParams, TreeBranch.BranchCreationParams branchCreationParams) {
        super(origin, zUnit, prevRadiusZ);
        double radiusZ = computeNextRadiusZ(branchCreationParams, ((double) i+1)/ branchCreationParams.currBranch.curveRes);
        this.endRadius = radiusZ;
        this.length = branchCreationParams.lengthFraction;
        this.level = branchCreationParams.level;
        if (this.endRadius < 0) {
            throw new InvalidValueException("TreeSegment computed a negative end radius!");
        }

        // Form children
        double endLength = branchCreationParams.lengthFraction *(i+1);
        double offset = nextChildOffset;
        double lastChildAngle = lastChildRotateAngle;
        final int nextLevel = branchCreationParams.level+1;
        TreeBranch[] children = new TreeBranch[0];
        if (nextLevel < treeCreationParams.treeParams.stemLevels) {
            ArrayList<TreeBranch> branches = new ArrayList<>(10);
            while (offset < endLength) {
                BranchParams nextBranch = (BranchParams)treeCreationParams.treeParams.fetchParams(nextLevel);
                double lengthChildMax = nextBranch.length+TreeModel.randDoubleVariation(treeCreationParams.treeRandom, nextBranch.lengthVariation);
                double lengthChild = lengthChildMax*(branchCreationParams.branchLength-0.6*offset);
                if (nextLevel == 1) {
                    lengthChild = lengthChildMax*branchCreationParams.branchLength
                            *getShapeRatio(treeCreationParams.treeParams.shape,
                            (branchCreationParams.branchLength-offset)/(branchCreationParams.branchLength-treeCreationParams.lengthBase));
                }
                double spawnLocationRadius = computeNextRadiusZ(branchCreationParams, offset/branchCreationParams.branchLength);
                double radiusChild = spawnLocationRadius*Math.pow(lengthChild/branchCreationParams.branchLength, treeCreationParams.treeParams.ratioPower);
                double downAngleChild;
                if (nextBranch.downAngleVariation >= 0) {
                    downAngleChild = nextBranch.downAngle+TreeModel.randDoubleVariation(treeCreationParams.treeRandom, nextBranch.downAngleVariation);
                } else {
                    // FIXME is using lengthBase correct?
                    // FIXME Might need a divide-by-zero check?
//                    // Using random variation of [downAngleVariation, 0) instead of [downAngleVariation, downAngleVariation),
//                    // as the point of this is that the branches generally point more and more upwards as you go along the branch.
//                    downAngleChild = nextBranch.downAngle+(Helpers.randDoubleRange(treeCreationParams.treeRandom, nextBranch.downAngleVariation, 0)
//                            *(1-2*TreeTypeParams.TreeShape.Conical.getRatio.applyAsDouble(
//                                    (branchCreationParams.branchLength-offset)/(branchCreationParams.branchLength-treeCreationParams.lengthBase))) );
                    // Using the downangle formula as given in the paper, where downAngleVariation is scaled by the
                    //  offset formula (1-2*...) and added to downAngle, instead of scaling a random value in the range
                    //  [downAngleVariation, 0) and scaling that.
                    downAngleChild = nextBranch.downAngle+(nextBranch.downAngleVariation
                            *(1-2* TreeTypeParams.TreeShape.Conical.getRatio.applyAsDouble(
                            (branchCreationParams.branchLength-offset)/(branchCreationParams.branchLength-treeCreationParams.lengthBase))) );
                }
                StemVec3d childZUnit = xUnit.rotateUnitVector(zUnit, downAngleChild);
                StemVec3d childXUnit = xUnit;

                if (nextBranch.rotate >= 0) {
                    lastChildAngle += nextBranch.rotate+TreeModel.randDoubleVariation(treeCreationParams.treeRandom, nextBranch.rotateVariation);
                    childZUnit = zUnit.rotateAbout(childZUnit, lastChildAngle);
                    childXUnit = StemVec3d.ZUnit.crossProduct(childZUnit).normalize();
                    if (childXUnit == StemVec3d.ZERO) {
                        childXUnit = xUnit;
                    }
                } else {
                    double angle = Math.PI+nextBranch.rotate+TreeModel.randDoubleVariation(treeCreationParams.treeRandom, nextBranch.rotateVariation);
                    if (lastChildAngle > 0) { // Alternate sides
                        angle *= -1;
                    }
                    childZUnit = zUnit.rotateAbout(childZUnit, angle);
                    lastChildAngle = angle;
                    //                 Or should it be:
                    //                 StemVec3d yUnit = zUnit.crossProduct(xUnit);
                    //                 childZUnit = yUnit.rotateAbout(childZUnit, angle);
                    childXUnit = StemVec3d.ZUnit.crossProduct(childZUnit).normalize();
                    if (childXUnit == StemVec3d.ZERO) {
                        childXUnit = xUnit;
                    }
                }

                double localOffset = offset-branchCreationParams.lengthFraction*i;
                StemVec3d childOrigin = origin.add(zUnit.scale(localOffset));
                int nStems = (int) Math.round(nextBranch.branches*(1.0-0.5*(offset/branchCreationParams.branchLength)));
                if (nextLevel == 1) {
                    nStems = (int) Math.round(nextBranch.branches*(0.2+0.8*(lengthChild/branchCreationParams.branchLength)/lengthChildMax));
                }
                // TODO Spawn child
                branches.add(new TreeBranch(childOrigin, childZUnit, childXUnit,
                        lengthChild, radiusChild, nStems,
                        nextLevel, treeCreationParams));

                offset += branchCreationParams.branchDistance;
            }
            children = branches.toArray(new TreeBranch[0]);
        }
        this.children = children;

        int segSplitsEffective = branchCreationParams.segSplitError.getNextEffectiveSplits(i);

        if (i < (branchCreationParams.currBranch.curveRes-1)) { // Not the last segment in the branch/clone.
            if (segSplitsEffective == 0) { // No branching
                this.nextSegments = createNextSingleSegment(origin, zUnit, xUnit,
                        i, remainingCorrection, lastChildAngle,
                        radiusZ, offset,
                        treeCreationParams, branchCreationParams
                );
            } else { // Branch
                this.nextSegments = createNextMultipleSegments(origin, zUnit, xUnit,
                        i, remainingCorrection, lastChildAngle,
                        radiusZ, offset, segSplitsEffective,
                        treeCreationParams, branchCreationParams
                );
            }
        } else { // Last segment in the branch/clone
            this.nextSegments = new TreeSegmentGenerated[0];
        }
    }

    private TreeSegment[] createNextSingleSegment(StemVec3d origin, StemVec3d zUnit, StemVec3d xUnit,
                                                           int i, double remainingCorrection, double lastChildAngle,
                                                           double radiusZ, double offset,
                                                           TreeModel.TreeCreationParams treeCreationParams, TreeBranch.BranchCreationParams branchCreationParams) {
        double correction = remainingCorrection/(branchCreationParams.currBranch.curveRes-i);
        double theta = (branchCreationParams.currBranch.curve+TreeModel.randDoubleVariation(treeCreationParams.treeRandom, branchCreationParams.currBranch.curveVariation))/branchCreationParams.currBranch.curveRes-correction;
        StemVec3d nextZUnit = xUnit.rotateUnitVector(zUnit, theta); // Rotate next z-vector
        // Vertical attraction
        // FIXME not sure if this is meant to transform a stem based on its own current orientation due to curvature,
        //  or its predecessor's orientation due to curvature?
        if (branchCreationParams.level > 1){
            double declination = Helpers.safeACos(nextZUnit.z);
            double orientation = Helpers.safeACos(nextZUnit.crossProduct(xUnit).z);
            double curveUp = treeCreationParams.treeParams.attractionUp * declination * Math.cos(orientation) / branchCreationParams.currBranch.curveRes;
            nextZUnit = xUnit.rotateUnitVector(nextZUnit, -curveUp);
        }

        StemVec3d nextOrigin = origin.add(zUnit.scale(branchCreationParams.lengthFraction));

        return new TreeSegment[] { TreeSegment.createSegment(nextOrigin, nextZUnit, xUnit,
                radiusZ, i+1, offset, lastChildAngle, remainingCorrection-correction,
                treeCreationParams, branchCreationParams) };
    }

    private TreeSegment[] createNextMultipleSegments(StemVec3d origin, StemVec3d zUnit, StemVec3d xUnit,
                                                              int i, double remainingCorrection, double lastChildAngle,
                                                              double radiusZ, double offset, int effectiveSplits,
                                                              TreeModel.TreeCreationParams treeCreationParams, TreeBranch.BranchCreationParams branchCreationParams) {
        int nBranches = effectiveSplits + 1;
        double declinationAngle = Helpers.safeACos(StemVec3d.ZUnit.dotProduct(zUnit));
        double[] splitAngles = new double[nBranches];
        double[] rotateAngles = new double[nBranches];
        StemVec3d[] nextZUnits = new StemVec3d[nBranches];
        double lastSign = Helpers.randDoubleSign(treeCreationParams.treeRandom);

        for (int j = 0; j < nBranches; j++) {
            splitAngles[j] = Math.max(0, (branchCreationParams.currBranch.splitAngle + TreeModel.randDoubleVariation(treeCreationParams.treeRandom, branchCreationParams.currBranch.splitAngleVariation)) - declinationAngle);
            // From https://sourceforge.net/p/arbaro/code/HEAD/tree/trunk/arbaro/src/net/sourceforge/arbaro/tree/impl/StemImpl.java#l1121
            // Makes the base split more natural - tree isn't all towards one side.
            if (i == 0 && branchCreationParams.level==0 && ((TrunkParams)branchCreationParams.currBranch).baseSplits > 0) {
                rotateAngles[j] = (2*Math.PI)*((double) j)/((double)nBranches)+TreeModel.randDoubleVariation(treeCreationParams.treeRandom, branchCreationParams.currBranch.splitAngleVariation);
            } else {
                double factor = treeCreationParams.treeRandom.nextDouble();
                rotateAngles[j] = lastSign*(Math.toRadians(20) + 0.75*(Math.toRadians(30) + Math.abs(declinationAngle-Math.PI/2))*factor*factor);
                // Heavily bias it to alternate the directions of rotation. Not specified in the paper, but it seems to make the trees look better.
                lastSign *= (treeCreationParams.treeRandom.nextDouble() > 0.95) ? 1 : -1;
                // Could just do:
                //  lastSign *= -1;
            }
            nextZUnits[j] = StemVec3d.ZUnit.rotateAbout(xUnit.rotateUnitVector(zUnit, splitAngles[j]), rotateAngles[j]);
        }

        StemVec3d nextOrigin = origin.add(zUnit.scale(branchCreationParams.lengthFraction));
        TreeSegment[] nextSegments = new TreeSegment[nBranches];
        for (int j = 0; j < nBranches; j++) {
            StemVec3d nextXUnit = StemVec3d.ZUnit.crossProduct(nextZUnits[j]).normalize();
            if (nextXUnit == StemVec3d.ZERO) { // zUnit and zUnitOrigin are parallel
                nextXUnit = StemVec3d.ZUnit.rotateUnitVector(xUnit, rotateAngles[j]);
            }

            // FIXME Proper joining between branches.
            nextSegments[j] = TreeSegment.createSegment(nextOrigin, nextZUnits[j], nextXUnit,
                    radiusZ, i+1, offset, lastChildAngle,
                    remainingCorrection + splitAngles[j],
                    treeCreationParams, branchCreationParams);
        }
        return nextSegments;
    }

    protected double getShapeRatio(TreeTypeParams.TreeShape shape, double ratio) {
        switch (shape) {
            case PruningEnvelope:
                throw new NotImplementedException("Pruning envelope has not yet been implemented.");
            default:
                return shape.getRatio.applyAsDouble(ratio);
        }
    }

}
