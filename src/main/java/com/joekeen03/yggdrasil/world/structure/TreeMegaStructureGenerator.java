package com.joekeen03.yggdrasil.world.structure;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.util.*;
import com.joekeen03.yggdrasil.world.structure.tree.*;
import com.joekeen03.yggdrasil.world.structure.generationFeatures.DoubleTruncatedCone;
import com.joekeen03.yggdrasil.world.structure.generationFeatures.GenerationFeature;
import com.joekeen03.yggdrasil.world.structure.generationFeatures.LeafBranch;
import com.joekeen03.yggdrasil.world.structure.generationFeatures.MNTruncatedCone;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class TreeMegaStructureGenerator implements ICubicStructureGenerator {
    private static final int TREE_RATE = 5; // On average, 1 out of this many sectors will spawn a tree.
    private static final long KEY_MASK = (1<<(22-1))-1;
    private static final int treeWidth=4096;
    private static final int treeHeight=4096;
    private static final int xzSectorSize = treeWidth/2;
    private static final int ySectorSize = treeHeight/2;
    private static final Long2ObjectOpenHashMap<IntegerAABBTree> treeCache = new Long2ObjectOpenHashMap<>(10);
    public static final boolean TREE_DEBUG = true;

    @Override
    public void generate(World world, CubePrimer cube, CubePos cubePos) {

        /*
        TODO Possible ways to make the world-gen fast:
            Segment world into "sections" - areas around the size of a structure instance, which can hold up to one
            "origin" of a structure - then you just check if the chunk is within range of that origin. (?)
            For things like the giga-trees, check y-level before even trying to generate them - check if you're above
            the column's "ground-level" (what if you have trees generate underground?)
         */
        // Prototype megastructure generator
        // Blocks
        // FIXME Maybe an offset so the sectors don't perfectly line up at the origin?
        // What about variable sized features - features that can come in a variety of different sizes?
        final int cubeSectorX = Math.floorDiv(cubePos.getX(), xzSectorSize/ICube.SIZE);
        final int cubeSectorY = Math.floorDiv(cubePos.getY(), ySectorSize/ICube.SIZE);
        final int cubeSectorZ = Math.floorDiv(cubePos.getZ(), xzSectorSize/ICube.SIZE);


        Random rand = new Random(world.getSeed());
        //used to randomize contribution of each coordinate to the cube seed
        //without these swapping x/y/z coordinates would result in the same seed
        //so structures would generate symmetrically
        long randXMul = rand.nextLong();
        long randYMul = rand.nextLong();
        long randZMul = rand.nextLong();

        long randSeed = world.getSeed();
        // Want the generation to always start in sectors with coords divisible by 3
        // So for cubeSectorX = 2, it should do X coords (3, 1, 2), in that order.
        // Determining the offset so that the first coord done is divisible by 3
        final int xOffset = 4-Math.floorMod(cubeSectorX, 3);
        final int yOffset = 4-Math.floorMod(cubeSectorY, 3);
        final int zOffset = 4-Math.floorMod(cubeSectorZ, 3);
        for (int x = xOffset; x < xOffset+3; x++) {
            int sectorX = cubeSectorX+Math.floorMod(x, 3)-1;
            long randX = sectorX * randXMul ^ randSeed;
            for (int y = yOffset; y < yOffset+3; y++) {
                int sectorY = cubeSectorY+Math.floorMod(y, 3)-1;
                long randY = sectorY * randYMul ^ randX;
                for (int z = zOffset; z < zOffset+3; z++) {
                    int sectorZ = cubeSectorZ+Math.floorMod(z, 3)-1;
                    long randZ = sectorZ * randZMul ^ randY;
                    rand.setSeed(randZ);
                    this.generate(world, rand, cube, sectorX, sectorY, sectorZ, cubePos);
                }
            }
        }
    }

    protected void generate(World world, Random structureRandom, CubePrimer cube,
                            int sectorX, int sectorY, int sectorZ,
                            CubePos generatedCubePos) {

        if (!TREE_DEBUG) {
            if (structureRandom.nextInt(TREE_RATE) != 0) {
                return;
            }
        }

        // FIXME - for proper world gen, this might need to know where the ground is in its "origin" chunk (for
        //  vertical position), even if that chunk is not yet generated.

        if (sectorY != 0) { // Don't generate anywhere except starting at ground level.
           return;
        }
        IntegerAABBTree tree = fetchTree(structureRandom, sectorX, sectorY, sectorZ);
        tree.forEachLeaf(generatedCubePos, feature -> feature.generate(cube, generatedCubePos));
        //ModYggdrasil.info("Tree generation finished for cube at "+generatedCubePos);

        // TODO - maybe a simulation type algorithm for generating trees, where it roughly simluates the tree's growth?

        // TODO - Tree varieties?
        //  Just looking in the garden, several different growth patterns. Some trees have branches that just go
        //  straight until they taper off, with stuff branching off of them.
        //  Some have branches that sort of zig-zag - bends might be where it used to go straight and broke off, or
        //  might just be random - maybe following the sunlight?
        //  Some trees, the trunk itself is rather short, and instead splits into several zig-zaggy large branches,
        //  like Toyons.
        //  Some seem to have one very short base, with multiple "trunks" that come out of it, parallel - like the tree
        //  next to the Toyon.
        //  There are also redwoods and the like, which are sort of cone shaped - one main trunk, with branches going
        //  straight out, which grow shorter and shorter (on average) towards the top.
        //  And so on
        // TODO Different tree angles? Could have a mega tree growing out of a mountainside at an angle.

        // TODO Tree roots? Maybe make the trunk thicker/gnarled at the base?
        // TODO Cave system based around the roots - maybe gaps under the bigger roots where soil has sunken in, or
        //  fissures around bigger roots, where they split apart some stone. Maybe the roots reach down into underground
        //  lakes?
        // TODO Knots in the trunk/branches - are they where branches used to be, and fell out? Basic research suggests
        //  they're caused by stress in the tree, and the tree attempting to seal off the stressor (e.g. damage)
        // TODO Trunks and branches that aren't perfectly straight
        // TODO Trunks that aren't perfectly circular? Maybe use some 3D perlin noise, and for each exterior point on
        //  the trunk, use the noise value at that point to shift the point out/in?
        // Note - logically, tree growth might be approximated as follows: initial branch angle is fixed, independent
        //  of other branches (where starts). How the branch branches out (length of segments, segment angles), though,
        //  will likely be impacted by the branches above it, but not the other way around.
    }

    public static IntegerAABBTree fetchTree(Random structureRandom, int sectorX, int sectorY, int sectorZ) {
        // TODO would it be faster to instead always fetch the tree, and instead have a "null" tree for sectors that
        //  shouldn't have one in them?
        long key = ((KEY_MASK & sectorX) << 44) | ((KEY_MASK & sectorY) << 22) | (KEY_MASK & sectorZ);
        synchronized (treeCache) {
            // Manual computeIfAbsent, since Long2ObjectHashMap doesn't have one which takes a LongFunction.
            if (!treeCache.containsKey(key)) {
                ModYggdrasil.info("Creating tree at sector "+sectorX+","+sectorY+","+sectorZ);
                treeCache.put(key, createTree(structureRandom,
                        sectorX, sectorY, sectorZ));
            }
            return treeCache.get(key);
        }
        // TODO Maybe replace this with a more streamlined version - one that only locks the variable if you need to
        //  generate a tree from scratch?
        //  Might be something like: if tree doesn't exist, lock buffer; when entering buffer, check if main cache
        //      now has necessary tree (if another thread created it); if so, return that; otherwise, clone the main
        //      cache into the buffer, create the tree and add it to the buffer, copy the buffer back to the main cache
        //      variable, and return the tree.
        //      Only concern with this is that the other threads might somehow not see the update to the main buffer?
        //      I saw something regarding threads caching the variable, and maybe not updating their copy of it right
        //      away? In which case, I would need to use volatile, to ensure that when a thread enters the sync-block,
        //      it sees the most recent update to the main cache - and knows for sure whether or not the tree's been
        //      created. But, volatile is slow?
        //      Maybe, maintain a third map, hasCreated, which stores whether or not a tree has definitely been created
        //      for a given structure pos, and make this volatile? Then, when entering the sync-block, it checks this
        //      map instead - and if the tree hasn't been created, creates the tree and updates the creation-map.
        //      But, that doesn't help if there's no way to force a cache update...maybe the hasCreated is a volatile
        //      copy of the main cache?
        //      Or, what if you just maintain the buffer as the most up-to-date copy of the main tree? From what I
        //      understand, if you synchronize on a variable, that variable should be updated in main memory after you
        //      exit the synchronized block - and it should read the most recent copy of it from main memory upon
        //      entering the sync block. So, synchronize on the buffer map; upon entering, check if the buffer has the
        //      tree; if it doesn't, create it, and add it to the buffer, and clone the buffer to the main map.
        //      But, is the map guaranteed to be internally updated w/in a thread? Synchronizing on the buffer variable
        //      only guarantees the variable - the object reference - is kept up to date - not the actual object.
        //      Move the buffer to the main map, and clone the buffer back ? Forcing the references to be updated?
        //      I think that would work? This guarantees that when a given thread first looks at the buffer, it will
        //      always have the most up to date version of it? Because it's a freshly-cloned object - it shouldn't exist
        //      in a thread's cache until it first accesses it. Though, could a thread hypothetically load the new
        //      buffer object while it is being created? Then not update it when it should?
    }

        /*
        TODO
            -Rough up trunks (craggly, irregular shapes)
                *Perlin noise to deform trunk - Have the noise be "stretched" in the vertical direction
            -Curved branches (try parabolae, simple curves)
            -Happens-after property for generation (ensure trunk is generated before branches)
                *List of predecessors, and a "proximity" function - one that determines if the predecessor could impact
                    the current cube.
            -Passing a set of flags around between features, then having a final step that transforms those flags into
                block placements.
                * Probably need two bits for direction, one bit for air, one bit for wood.
            -Self-intersection avoidance
            -Trunk should handle loss of thickness (due to branching) better - cone shaped? Maybe a bevel at the
                branch's level? Perhaps the next trunk segment is offset, due to the branch splitting from it?
            -Smoothing to connect branches with the trunk.
            -Generation features:
                *Leaf placement.
                *Knots
                *Broken branches
                *Roots - should influence trunk shape?
                    -Probably several major taproots going roughly downwards, with horizontal-ish roots spreading out
                        at various levels.
                *Tree Throws (depressions in ground)
                *Tree hollows
            -Verify branches are actually being placed at correct heights.
            -Fire-resistant/proof wood.
            -Other decorative features:
                *Miniature lakes and other micro-climates/biomes
                *Hanging vines/other plant life
                *Various animal life
                    +Tree is so big it has micro-biomes w/in it
                *Maybe some kind of light source spread throughout the tree?
                *Moss on older branches
                *Villages
            -Special biome on the ground level, due to the tree's impact on the terrain.
            -What if knots connected to some kind of massive, internal cave system running throughout the trunk?
            -Cave systems connected to/formed by roots
                *Roots maybe connect to massive underground lakes.
            -Giant leaves?
            -Heartwood/sapwood/bark distinction on larger branches & trunk.
         */

    // Using the tree generation model laid out in "Creation and Rendering of Realistic Trees", by Jason Weber and
    //  Joseph Penn.
    // Note that their system uses an axes system where the z axis is the "vertical" axis for the trunk, and the x and
    //  y axes form the horizontal plane. Branch segments (and technically trunk segments, too) each have their own
    //  local coord system, with the z-axis in line with the segment, the x-axis restricted to the horizontal plane,
    //  and the y-axis pointed as close to vertical as it can get, given the segment's orientation.
    // I'm going to say the absolute axes within tree-space (the ones all the other axes are transformations of)
    //  correspond to MC's coord system as follows: x_tree -> z_MC, y_tree -> x_MC, z_tree -> y_MC

    // TODO Implement an nSegsBase, which prevents cloning before a certain point? Or...could just have
    //  baseSplits=segSplits=0, baseLength=0.95, and 0branches=7.
    private static final TreeTypeParams treeParams = PossibleMegaTreeParams.active;

    protected static IntegerAABBTree createTree(Random treeRandom, int sectorX, int sectorY, int sectorZ) {
        // Generate trunk - recursive level 0

        StemVec3d trunkOrigin = new StemVec3d(new Vec3d(treeRandom.nextInt(xzSectorSize)+sectorX*xzSectorSize,
                48, treeRandom.nextInt(xzSectorSize)+sectorZ*xzSectorSize));
        if (TREE_DEBUG) {
            trunkOrigin = new StemVec3d(new Vec3d(sectorX*xzSectorSize+xzSectorSize/2, 48, sectorZ*xzSectorSize+xzSectorSize/2));
        }

        // Trunk's initial segment is aligned with the absolute z-axis. Maybe play with trunks that don't start vertical?
        StemVec3d zUnitOrigin = new StemVec3d(0, 0, 1);

        TreeModel model = new TreeModel(treeRandom, trunkOrigin, zUnitOrigin, treeParams);
        ArrayList<GenerationFeature> newFeatures = createGenerationFeatures(model);
        IntegerAABBTree newTree = new IntegerAABBTree(newFeatures.toArray(new GenerationFeature[0]));
        ModYggdrasil.info("Tree for sector "+sectorX+","+sectorY+","+sectorZ+" created, with origin at "+trunkOrigin.toMCVector());
        return newTree;
    }

    private static ArrayList<GenerationFeature> createGenerationFeatures(TreeModel model) {
        ArrayList<GenerationFeature> generationFeatures = new ArrayList<>(3000);
        // FIXME Verify that the first segment is a TreeSegmentGenerated?
        processBranch(model.trunk, StemVec3d.ZERO, generationFeatures);
        return generationFeatures;
    }

    private static void processBranch(TreeBranch branch, StemVec3d adjustment, ArrayList<GenerationFeature> generationFeatures) {
        processSegment( branch.firstSegment, new StemVec3d[] {branch.firstSegment.zUnit},
                adjustment, generationFeatures);
    }

    private static void processSegment(TreeSegment segment, StemVec3d[] plane1Units, StemVec3d adjustment,
                                       ArrayList<GenerationFeature> generationFeatures) {
        if (segment instanceof TreeSegmentGenerated) {
            TreeSegmentGenerated generatedSegment = (TreeSegmentGenerated) segment;
            for (TreeBranch child : generatedSegment.children) {
                processBranch(child, adjustment, generationFeatures);
            }
            StemVec3d[] nextPlane1Units = new StemVec3d[generatedSegment.nextSegments.length];
            for (int i = 0; i < generatedSegment.nextSegments.length; i++) {
                TreeSegment nextSegment = generatedSegment.nextSegments[i];
                nextPlane1Units[i] = generatedSegment.zUnit.add(nextSegment.zUnit).normalize();
                StemVec3d nextAdjustment = computeNextAdjustment(nextPlane1Units[i], adjustment,
                        generatedSegment.zUnit, nextSegment.zUnit, generatedSegment.length,
                        generatedSegment.baseRadius, generatedSegment.endRadius);
                processSegment(nextSegment, new StemVec3d[] {nextPlane1Units[i]},
                        nextAdjustment, generationFeatures);
            }
            if (nextPlane1Units.length == 0) { // End of the branch
                nextPlane1Units = new StemVec3d[] {generatedSegment.zUnit};
            }
            if (plane1Units.length == 1 && nextPlane1Units.length == 1) {

                DoubleTruncatedCone feature = new DoubleTruncatedCone(
                        (generatedSegment.origin.add(adjustment)).toMCVector(), generatedSegment.zUnit.toMCVector(),
                        plane1Units[0].toMCVector(), nextPlane1Units[0].scale(-1).toMCVector(),
                        generatedSegment.baseRadius, generatedSegment.endRadius, generatedSegment.length);
                generationFeatures.add(feature);
            } else {
                MNTruncatedCone feature = new MNTruncatedCone(
                        (generatedSegment.origin.add(adjustment)).toMCVector(), generatedSegment.zUnit.toMCVector(),
                        stemToMC(plane1Units), stemToMCAndFlip(nextPlane1Units),
                        generatedSegment.baseRadius, generatedSegment.endRadius, generatedSegment.length);
                generationFeatures.add(feature);
            }
        } else if (segment instanceof TreeSegmentNode) {
            LeafBranch feature = new LeafBranch((TreeSegmentNode) segment, adjustment);
            generationFeatures.add(feature);
        }
    }

    /**
     * Computes the next plane's origin. The nextPlaneXUnit should be perpendicular to the line of max slope along the
     * plane, relative to the
     * @param nextPlane1Normal
     * @param currAdjustment
     * @param currZUnit
     * @param nextZUnit
     * @param lengthFraction
     * @param currRadius
     * @param nextRadius
     * @return
     */
    protected static StemVec3d computeNextAdjustment(StemVec3d nextPlane1Normal, StemVec3d currAdjustment,
                                                 StemVec3d currZUnit, StemVec3d nextZUnit,
                                                 double lengthFraction, double currRadius, double nextRadius) {
        // Shift the next conical segment's origin down the line of max slope on the plane, to ensure its elliptical
        //  end lines up with the current segment's elliptical end.
        double normalDot = nextPlane1Normal.dotProduct(nextZUnit); // cos(theta)
        StemVec3d adjustVector = nextPlane1Normal.scale(normalDot).subtract(nextZUnit).normalize(); // n_nextplane*cos(theta)-n_nextcone
        double coneSlope = lengthFraction/(nextRadius-currRadius);
        double planeMaxSlope = Math.tan(Helpers.safeACos(normalDot));
        double rMin = nextRadius*coneSlope/(coneSlope-planeMaxSlope);
        double rMax = nextRadius*coneSlope/(coneSlope+planeMaxSlope);
        double deltaR = rMax-rMin;
        double deltaH = planeMaxSlope*deltaR;
        double planeShift = Math.sqrt(deltaR*deltaR+deltaH*deltaH);

        // P_origin + length*n_nextcone + shift*n_maxSlope
        StemVec3d nextAdjustment = currAdjustment.add(adjustVector.scale(planeShift));
        if (nextAdjustment.hasNaN()) {
            throw new InvalidValueException("computeNextOrigin created new origin with NaNs.");
        }
        return nextAdjustment;
    }

    protected static Vec3d[] stemToMCAndFlip(StemVec3d[] stemVectors) {
        Vec3d[] mcVectors = new Vec3d[stemVectors.length];
        for (int i = 0; i < stemVectors.length; i++) {
            mcVectors[i] = stemVectors[i].toMCVector().scale(-1);
        }
        return mcVectors;
    }

    protected static Vec3d[] stemToMC(StemVec3d[] stemVectors) {
        Vec3d[] mcVectors = new Vec3d[stemVectors.length];
        for (int i = 0; i < stemVectors.length; i++) {
            mcVectors[i] = stemVectors[i].toMCVector();
        }
        return mcVectors;
    }

}
