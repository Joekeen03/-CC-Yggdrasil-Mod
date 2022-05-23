package com.joekeen03.yggdrasil.util;

import io.github.opencubicchunks.cubicchunks.api.util.CubePos;

public class IntegerAABB{
    public final int minX, maxX, minY, maxY, minZ, maxZ, lengthX, lengthY, lengthZ;

    public IntegerAABB(int x1, int y1, int z1, int x2, int y2, int z2) {
        // What exactly should the min/max coords indicate? Should they indicate start/stop coords like the arraySection
        // methods? Where the minX is the x-coord where the bounding box starts, and maxX is where the bounding box stops?
        // I.e. it includes coordinates in the range, [minX, maxX)
        // Or should maxX be the last x-coord in the bounding box, given this is using integers?
        // Currently assuming the former.
        minX = Math.min(x1, x2);
        maxX = Math.max(x1, x2);
        minY = Math.min(y1, y2);
        maxY = Math.max(y1, y2);
        minZ = Math.min(z1, z2);
        maxZ = Math.max(z1, z2);
        lengthX = maxX-minX;
        lengthY = maxY-minY;
        lengthZ = maxZ-minZ;
        /*
        Since the whole point of this bounding box and the tree it's part of is to be able to quickly exclude, should
        this also hold the shape it's supposed to wrap? Or should it be something the shape stores, given it's the shape
        that would generate it? The latter would lead to a case where you call shape.getminbounding, and then create a
        IntegerAABB that holds it. Maybe the shape.getminbounding would return a bounding box that wraps the shape?
        I think it makes logical sense for the minboundingbox to wrap the shape, given that you check the minboundingbox
        before checking the shape - but at the same time, it's the shape that "knows" how to generate the minboundingbox
        for itself, and the minboundingbox could be considered a property of the shape?
        Gonna try just holding the shape in the box - the box is specifically meant for a shape, in my use case, so I
        think that makes sense.

        Also, I could technically incorporate this into the IntegerAABBTreeNode class, but that would unnecessarily
        complicate things, I think.
         */
    }

    /**
     * Returns whether the specified point is in the bounding box.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public boolean isInBoundingBox (CubePos pos) {
        return ((pos.getX() >= minX) && (pos.getX() < maxX)
                && (pos.getY() >= minY) && (pos.getY() < maxY)
                && (pos.getZ() >= minZ) && (pos.getZ() < maxZ));
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMaxZ() {
        return maxZ;
    }

    public int getMinX() {
        return minX;
    }

    public int getMinY() {
        return minY;
    }

    public int getMinZ() {
        return minZ;
    }
}
