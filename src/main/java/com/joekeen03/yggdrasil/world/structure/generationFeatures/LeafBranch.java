package com.joekeen03.yggdrasil.world.structure.generationFeatures;

import com.joekeen03.yggdrasil.util.*;
import com.joekeen03.yggdrasil.world.structure.tree.TreeBranch;
import com.joekeen03.yggdrasil.world.structure.tree.TreeSegment;
import com.joekeen03.yggdrasil.world.structure.tree.TreeSegmentGenerated;
import com.joekeen03.yggdrasil.world.structure.tree.TreeSegmentNode;
import com.joekeen03.yggdrasil.world.structure.tree.StemParams;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nonnull;

public class LeafBranch implements GenerationFeature {
    private final TreeSegmentNode branchNode;
    private final StemVec3d adjustment;
    private TreeSegmentGenerated generatedSectionCache;

    private static final byte AIR = 0;
    private static final byte LEAF = 1;
    private static final byte WOODX = 2;
    private static final byte WOODY = 3;
    private static final byte WOODZ = 4;
    private static final byte WOODNONE = 5;
    private static final IBlockState[] blockArray = new IBlockState[] {null, Blocks.LEAVES.getDefaultState(),
            BlockHelpers.blockOakX, BlockHelpers.blockOakY, BlockHelpers.blockOakZ, BlockHelpers.blockOakNone};

    public LeafBranch(TreeSegmentNode branchNode, StemVec3d adjustment) {
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
        // Max possible length - at each level, it's the current level's length + the max length for the child if
        //  the child's max length is at the tip, or the max length of the child if the child's max length is at the base.
        double branchLength = branchNode.branchCreationParams.branchLength;
        double offset = branchLength-branchNode.branchCreationParams.lengthFraction
                *(branchNode.branchCreationParams.currBranch.curveRes-branchNode.i);
        double maxTotalLength = computeMaxLength(branchLength, offset, branchNode.branchCreationParams.level);
        double maxLengthCubeScale = maxTotalLength/16;
        Vec3d origin = branchNode.origin.toMCVector();
        return new IntegerMinimumAABB(this,
                (int) Math.floor(origin.x-maxLengthCubeScale), (int) Math.floor(origin.y-maxLengthCubeScale),
                (int) Math.floor(origin.z-maxLengthCubeScale), (int) Math.ceil(origin.x+maxLengthCubeScale),
                (int) Math.ceil(origin.y+maxLengthCubeScale), (int) Math.ceil(origin.z+maxLengthCubeScale));
    }

    private double computeMaxLength(double maxLength, double offset, int level) {
        if (level+1 >= branchNode.treeCreationParams.treeParams.stemLevels) { // Next level is the leaf level
            return maxLength;
        }
        StemParams childParams = branchNode.treeCreationParams.treeParams.fetchParams(level+1);
        double maxChildRelativeLength = childParams.length+childParams.lengthVariation;
        // This assumes the max length only occurs at the base or tip. Which is guaranteed true for any level past the
        //  trunk. Not sure about the trunk, though.
        double maxChildLengthBase = computeChildLength(maxLength, maxChildRelativeLength, offset, level);
        double maxChildLengthTip = computeChildLength(maxLength, maxChildRelativeLength, maxLength, level);
        double childMaxLength = computeMaxLength(Math.max(maxChildLengthBase, maxChildLengthTip), 0, level+1);
        double maxTotalLength = childMaxLength;
        if (maxChildLengthTip > maxChildLengthBase) {
            maxTotalLength = childMaxLength + (maxLength-offset);
        }
        return maxTotalLength;
    }

    private double computeChildLength(double lengthParent, double lengthChildMax, double offsetChild, int parentLevel) {
        if (parentLevel == 0) {
            double lengthChild = lengthParent*lengthChildMax
                    *branchNode.treeCreationParams.treeParams.shape.getRatio.applyAsDouble(
                            (lengthParent-offsetChild)/(lengthParent-branchNode.treeCreationParams.lengthBase));
            return lengthChild;
        } else {
            double lengthChild = lengthChildMax*(lengthParent-0.6*offsetChild);
            return lengthChild;
        }
    }

    @Override
    public boolean intersectsCube(CubePos pos) {
        return true;
    }

    @Override
    public void generate(CubePrimer cubePrimer, CubePos pos) {
        int bufferSize = ICube.SIZE+2;
        Vec3i bufferOrigin = new Vec3i(pos.getMinBlockX()-1, pos.getMinBlockY()-1, pos.getMinBlockZ()-1);
        byte[][][] buffer = new byte[bufferSize][bufferSize][bufferSize];
        if (generatedSectionCache == null) {
            generatedSectionCache = branchNode.create();
        }
        generateSegment(buffer, bufferSize, bufferOrigin, generatedSectionCache);

        for (int x = 0; x < ICube.SIZE; x++) {
            for (int y = 0; y < ICube.SIZE; y++) {
                for (int z = 0; z < ICube.SIZE; z++) {
                    int material = buffer[x+1][y+1][z+1];
                    switch (material) {
                        case WOODX:
                            if (buffer[x][y+1][z+1] == AIR || buffer[x+2][y+1][z+1] == AIR) {
                                material = WOODNONE;
                            }
                            break;
                        case WOODY:
                            if (buffer[x+1][y][z+1] == AIR || buffer[x+1][y+2][z+1] == AIR) {
                                material = WOODNONE;
                            }
                            break;
                        case WOODZ:
                            if (buffer[x+1][y+1][z] == AIR || buffer[x+1][y+1][z+2] == AIR) {
                                material = WOODNONE;
                            }
                            break;
                    }
                    if (material > AIR) {
                        if (cubePrimer.getBlockState(x, y, z) == Blocks.AIR.getDefaultState()) {
                            cubePrimer.setBlockState(x, y, z, blockArray[material]);
                        }
                    }
                }
            }
        }
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

    private void generateSegment(byte[][][] buffer, int bufferSize, Vec3i bufferOrigin, TreeSegmentGenerated segment) {
        // Generate all segments at this level first.
        byte fillValue = LEAF;
        if (segment.level < branchNode.treeCreationParams.treeParams.stemLevels
                || branchNode.treeCreationParams.treeParams.leafParams.leaves == 0) {
            fillValue = AIR;
        }
        lineRasterization(buffer, bufferSize, bufferOrigin, segment, fillValue);
        for (TreeSegment nextSegment : segment.nextSegments) {
            generateSegment(buffer, bufferSize, bufferOrigin, (TreeSegmentGenerated) nextSegment);
        }
        for (TreeBranch child : segment.children) {
            generateSegment(buffer, bufferSize, bufferOrigin, (TreeSegmentGenerated) child.firstSegment);
        }
    }

    private void lineRasterization(byte[][][] buffer, int bufferSize, Vec3i bufferOrigin, TreeSegmentGenerated segment,
                                   byte fillValue) {
        // FIXME - doesn't handle  other primary axes, or the primary axis needing to be swapped (zUnit points in the
        //  wrong direction).
        //      I.e. it assumes stopZ > startZ
        // Bresenham's line algorithm - treats the center of pixels (blocks, in this case), as having integer coords
        //  So the center of the block at (0, 0, 0) will have coords (0, 0, 0), while the MC-origin of that block
        //  (the -x, -y, -z corner) would have coords (-0.5, -0.5, -0.5).
        Vec3d activeZUnit = segment.zUnit.toMCVector();
        Vec3d origin = segment.origin.add(adjustment).toMCVector();
        if (Math.abs(activeZUnit.z) > Math.abs(activeZUnit.y)
                && Math.abs(activeZUnit.z) > Math.abs(activeZUnit.x)) { // Primary axis is Z
            if (activeZUnit.z < 0) { // Need to flip the segment so we work from its other end.
                origin = origin.add(activeZUnit.scale(segment.length));
                activeZUnit = activeZUnit.scale(-1);
            }
            if (fillValue == AIR) {
                fillValue = WOODZ;
            }
            double slopeXZ = activeZUnit.x/ activeZUnit.z;
            double slopeYZ = activeZUnit.y/ activeZUnit.z;
            double segmentStartZLocal = origin.z-bufferOrigin.getZ();
            double segmentStopZLocal = segmentStartZLocal+ activeZUnit.z*segment.length;
            // Math.floor(segmentStartZLocal); z-coord of the block containing the segment's start point.
            //  This is equivalent to Math.round(segmentStartZLocal-0.5), where z-0.5 is the transformation from
            //  MC-space to the integer-center space used for Bresenham's.
            int startLocalZ = Math.max((int) Math.floor(segmentStartZLocal), 0);
            int stopLocalZ = Math.min((int) Math.floor(segmentStopZLocal), bufferSize-1);
            int currLocalZ = Math.max(startLocalZ, bufferOrigin.getZ()); // Start within the cube's z-bounds.
            // Distance between the segment's actual start z-coord, and the center z-coord of the block we're starting
            //  the rasterization in.
            double zOffset = startLocalZ+0.5-segmentStartZLocal;
            // Integer-center X & Y coords of the line for the integer-center startLocalZ.
            double currLocalX = (origin.x-bufferOrigin.getX())
                    +slopeXZ*(zOffset)-0.5;
            double currLocalY = (origin.y-bufferOrigin.getY())
                    +slopeYZ*(zOffset)-0.5;
            while (currLocalZ <= stopLocalZ) {
                int x = (int)Math.round(currLocalX);
                int y = (int)Math.round(currLocalY);
                if (x < bufferSize && y < bufferSize && x >= 0 && y >= 0) {
                    buffer[x][y][currLocalZ] |= fillValue;
                    // Technically, not subtracting 0.5 from the currLocalX & Y and using Math.floor instead of Math.round
                    // would be faster (I think?), but I do it this way for clarity.
                }
                currLocalZ += 1;
                currLocalX += slopeXZ;
                currLocalY += slopeYZ;
            }
        } else if (Math.abs(activeZUnit.x) > Math.abs(activeZUnit.y)) { // Primary axis is X
            if (activeZUnit.x < 0) { // Need to flip the segment so we work from its other end.
                origin = origin.add(activeZUnit.scale(segment.length));
                activeZUnit = activeZUnit.scale(-1);
            }
            if (fillValue == AIR) {
                fillValue = WOODX;
            }
            double slopeZX = activeZUnit.z/ activeZUnit.x;
            double slopeYX = activeZUnit.y/ activeZUnit.x;
            double segmentStartXLocal = origin.x-bufferOrigin.getX();
            double segmentStopXLocal = segmentStartXLocal+ activeZUnit.x*segment.length;

            int startLocalX = Math.max((int) Math.floor(segmentStartXLocal), 0);
            int stopLocalX = Math.min((int) Math.floor(segmentStopXLocal), bufferSize-1);
            int currLocalX = Math.max(startLocalX, bufferOrigin.getX());

            double xOffset = startLocalX+0.5-segmentStartXLocal;
            double currLocalY = (origin.y-bufferOrigin.getY())
                    +slopeYX*(xOffset)-0.5;
            double currLocalZ = (origin.z-bufferOrigin.getZ())
                    +slopeZX*(xOffset)-0.5;
            while (currLocalX <= stopLocalX) {
                int z = (int)Math.round(currLocalZ);
                int y = (int)Math.round(currLocalY);
                if (z < bufferSize && y < bufferSize && z >= 0 && y >= 0) {
                    buffer[currLocalX][y][z] |= fillValue;
                }
                currLocalX++;
                currLocalY += slopeYX;
                currLocalZ += slopeZX;
            }
        } else { // Primary axis is Y
            if (activeZUnit.y < 0) { // Need to flip the segment so we work from its other end.
                origin = origin.add(activeZUnit.scale(segment.length));
                activeZUnit = activeZUnit.scale(-1);
            }
            if (fillValue == AIR) {
                fillValue = WOODY;
            }
            double slopeXY = activeZUnit.x/ activeZUnit.y;
            double slopeZY = activeZUnit.z/ activeZUnit.y;
            double segmentStartYLocal = origin.y-bufferOrigin.getY();
            double segmentStopYLocal = segmentStartYLocal+ activeZUnit.y*segment.length;

            int startLocalY = Math.max((int) Math.floor(segmentStartYLocal), 0);
            int stopLocalY = Math.min((int) Math.floor(segmentStopYLocal), bufferSize-1);
            int currLocalY = Math.max(startLocalY, bufferOrigin.getY());

            double yOffset = startLocalY+0.5-segmentStartYLocal;
            double currLocalX = (origin.x-bufferOrigin.getX())
                    +slopeXY*(yOffset)-0.5;
            double currLocalZ = (origin.z-bufferOrigin.getZ())
                    +slopeZY*(yOffset)-0.5;
            while (currLocalY <= stopLocalY) {
                int z = (int)Math.round(currLocalZ);
                int x = (int)Math.round(currLocalX);
                if (z < bufferSize && x < bufferSize && z >= 0 && x >= 0) {
                    buffer[x][currLocalY][z] |= fillValue;
                }
                currLocalY++;
                currLocalX += slopeXY;
                currLocalZ += slopeZY;
            }
        }
    }
}
