package com.joekeen03.yggdrasil.world.structure;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.util.*;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.structure.ICubicStructureGenerator;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TreeMegaStructureGenerator implements ICubicStructureGenerator {
    private static final int TREE_RATE = 5; // On average, 1 out of this many sectors will spawn a tree.
    private static final int treeWidth=512;
    private static final int treeHeight=2048;
    private static final int xzSectorSize = treeWidth/2;
    private static final int ySectorSize = treeHeight/2;
    private static final IBlockState OAK_LOG = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK);
    private static final IBlockState OAK_BARK = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.OAK)
            .withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.NONE);
    private static final Long2ObjectOpenHashMap<IntegerAABBTree> treeCache = new Long2ObjectOpenHashMap<>(10);

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

        if (structureRandom.nextInt(TREE_RATE) != 0) {
            return;
        }

        // FIXME - for proper world gen, this might need to know where the ground is in its "origin" chunk (for
        //  vertical position), even if that chunk is not yet generated.

        if (sectorY != 0) { // Don't generate anywhere except starting at ground level.
           return;
        }
        IntegerAABBTree tree = fetchTree(structureRandom, sectorX, sectorY, sectorZ);
        tree.forEachLeaf(generatedCubePos, feature -> feature.generate(cube, generatedCubePos));
        //ModYggdrasil.info("Tree generation finished for cube at "+generatedCubePos);

        // For each branch:
            // Generate a segment - random length
                // Average length should drop w/ thickness
                // Maybe segments should slowly become thinner over their length
            // Generate a random number of branches leaving the segment, and random angles/base thicknesses for them
            // Call the method recursively on those branches.
            // If the branch reaches a certain minimum thickness (1?), instead generate leaves
                // Maybe reserve this for the actual block placement - instead, terminate the algorithm past a certain
                    // point?
                    // Would need a way to save the generator's state - maybe you generate a seed value which you save,
                    // which is used to seed an rng for generating the leaves/fine branches?
                    // Basically, I only want to save the stuff that a lot of cubes will have to check/process.

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
        //  the trunk, use the noise value at that point to shift the point out/in
        // TODO Better way to generate circles for the trunks (esp. the bark)? For the bark, might be best to just check
        //  if a given block is exposed to air - if so, turn it into bark.
        // TODO How to efficiently check if a cube needs to worry about a branch?
        // TODO Cache the results of this algorithm - otherwise, all 96x96x96 cubes will have to compute this tree
        //      Probably generate the whole tree the first time, then cache the bounding boxes for each branching point?
        //      Storage method:
        // Note - logically, tree growth might be approximated as follows: initial branch angle is fixed, independent
        //  of other branches (where starts). How the branch branches out (length of segments, segment angles), though,
        //  will likely be impacted by the branches above it, but not the other way around.
    }

    public void generateBranch(CubePrimer cube, CubePos generatedCubePos,
                               Random rand, double branchThickness, double branchLength,
                               double[] branchAngles) {
        if (branchThickness < 1 || branchLength < 8) {
            return;
        }
        Math.abs(0.3d);
        if (rand.nextInt(4) < 2) { // Just continue the branch
            generateBranch(cube, generatedCubePos, rand, branchThickness*0.95,
                    branchLength* Helpers.randDoubleRange(rand, 0.4, 0.6), branchAngles);
            return;
        }
        int nBranches = Helpers.randIntRange(rand, 1, Helpers.randIntRange(rand, 2, 3)); // # of branches splitting off
        for (int i = 0; i < nBranches; i++) {
            generateBranch(cube, generatedCubePos, rand, branchThickness* Helpers.randDoubleRange(rand, 0.2, 0.7),
                    branchLength* Helpers.randDoubleRange(rand, 0.4, 0.6),
                    new double[] {branchAngles[0]+ Helpers.randDoubleRange(rand, -Math.PI/18, Math.PI/18),
                            branchAngles[1]+ Helpers.randDoubleRange(rand, -Math.PI/18, Math.PI/18)}); // Really not happy with this
        }
    }

    public static IntegerAABBTree fetchTree(Random structureRandom, int sectorX, int sectorY, int sectorZ) {
        // TODO would it be faster to instead always fetch the tree, and instead have a "null" tree for sectors that
        //  shouldn't have one in them?
        long key = ((long) sectorX << 44) | ((long) sectorY << 22) | sectorZ;
        synchronized (treeCache) {
            // Manual computeIfAbsent, since Long2ObjectHashMap doesn't have one which takes LongFunction.
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

    protected static IntegerAABBTree generateTree(Random structureRandom, int sectorX, int sectorY, int sectorZ) {
        final int trunkXCenter = structureRandom.nextInt(xzSectorSize)+sectorX*xzSectorSize;
        final int trunkYCenter = 48;
        final int trunkZCenter = structureRandom.nextInt(xzSectorSize)+sectorZ*xzSectorSize;
        int trunkHeight = structureRandom.nextInt(512)+512; // Trunk height range: [512, 1024)
        int trunkRadius = structureRandom.nextInt(16)+32; // Trunk's base radius range: [32, 48)
        // - maybe should be influenced by height?

        final int nBranches = Helpers.randIntRange(structureRandom, (int)(trunkHeight/2/20*0.9), (int)(trunkHeight/2/20*1.1));
        double[] branchHeights = new double[nBranches];
        for (int i = 0; i < nBranches; i++) {
            branchHeights[i] = Helpers.randDoubleRange(structureRandom, 0.5, 1.0);
        }
        Arrays.sort(branchHeights); // Sort the branches from bottom to top

        // Cumulative bias of the branches - effectively, an average of the branches' azimuthal angles, and how strongly
        //  they're biased towards that direction.
        Vector2f bias = new Vector2f(0, 0);

        double[][] branchInfo = new double[nBranches][4];
        for (int i = 0; i < nBranches; i++) {
            double[] branch = branchInfo[i];
            // Where the branch is - maybe this should be weighted towards the top?
            branch[0] = branchHeights[i];
            // Thickness relative to the trunk at that point - weighted so branches get thicker relative to the branch
            // as you go up
            branch[1] = Helpers.randDoubleRange(structureRandom, 0.1, branch[0]*1.6-0.6);
            // Angle the branch leaves the tree at, in spherical coords
            branch[2] = Helpers.randDoubleRange(structureRandom, Math.PI*0.3, Math.PI*0.45); // polar

            // How much the next branch's angle will be offset from the bias vector's angle.
            double offset = 0.0;
            // The more biased the branches are to one side, the more biased the next one will be to the opposite side.
            int nRandom = (int) (bias.length()+1);
            for (int j = 0; j < nRandom; j++) {
                offset = Helpers.randDoubleRange(structureRandom, 0, Math.PI*2);
            }
            double azimuthal = (Math.atan2(bias.y, bias.x)+Math.PI+offset/nRandom)%(Math.PI*2) - Math.PI;
            branch[3] = azimuthal; // azimuthal
            // Add the current branch to the bias vector
            bias.translate((float)Math.cos(azimuthal), (float) Math.sin(azimuthal));
        }

        final Cylinder[] cylinders = new Cylinder[nBranches*2+1];
        double currBaseRadius = trunkRadius;
        double lastHeight = 0.0;
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
            -Proper branching
            -Self-intersection avoidance
            -Trunk should handle loss of thickness (due to branching) better - cone shaped? Maybe a bevel at the
                branch's level? Perhaps the next trunk segment is offset, due to the branch splitting from it?
            -Smoothing to connect branches with the trunk.
            -Generation features:
                *Branching branches - start to develop into proper tree
                *Leaf placement.
                *Knots
                *Broken branches
                *Roots - should influence trunk shape?
            -Verify branches are actually being placed at correct heights.
         */
        for (int i = 0; i < nBranches; i++) {
            double[] branch = branchInfo[i];
            // Branch cylinder
            cylinders[i] = new Cylinder(new BlockPos(trunkXCenter, trunkYCenter+trunkHeight*(branch[0]), trunkZCenter),
                    currBaseRadius*branch[1], 300, branch[2], branch[3]);
            // Trunk segment
            cylinders[nBranches+i] = new Cylinder(
                    new BlockPos(trunkXCenter, trunkYCenter+trunkHeight*lastHeight, trunkZCenter),
                    currBaseRadius, trunkHeight*(branch[0]-lastHeight), 0.0, 0.0);
            currBaseRadius *= Math.sqrt(1-branch[1]*branch[1]);
            lastHeight = branch[0];
        }
        cylinders[nBranches*2] = new Cylinder(
                new BlockPos(trunkXCenter, trunkYCenter+trunkHeight*lastHeight, trunkZCenter),
                trunkRadius, trunkHeight*(1.0-lastHeight), 0.0, 0.0);
        ModYggdrasil.info("Tree for sector "+sectorX+","+sectorY+","+sectorZ+" created, with origin at "+cylinders[nBranches].origin);
        return new IntegerAABBTree(cylinders);
    }

    // Using the tree generation model laid out in "Creation and Rendering of Realistic Trees", by Jason Weber and
    //  Joseph Penn.
    // Note that their system uses an axes system where the z axis is the "vertical" axis for the trunk, and the x and
    //  y axes form the horizontal plane. Branch segments (and technically trunk segments, too) each have their own
    //  local coord system, with the z-axis in line with the segment, the x-axis restricted to the horizontal plane,
    //  and the y-axis pointed as close to vertical as it can get, given the segment's orientation.
    // I'm going to say the absolute axes within tree-space (the ones all the other axes are transformations of)
    //  correspond to MC's coord system as follows: x_tree -> z_MC, y_tree -> x_MC, z_tree -> y_MC

    // 0-level (trunk) parameters
    private static final double baseScale = 1.0;
    private static final double baseScaleVariation = 0.1;
    private static final double ratio = 0.1;
    private static final double length_0 = 1500;
    private static final double lengthVariation_0 = 500;
    private static final double curve_0 = Math.toRadians(5);
    private static final double curveVariation_0 = Math.toRadians(5); // Could be varied based on length (*)
    private static final int curveResolution_0 = 10;
    private static final double curveBack_0 = Math.toRadians(0);
    private static final double scale_0 = 1.0;
    private static final double taper_0 = 1.0;
    private static final double segmentSplits_0 = 0.5;
    private static final double baseSplits_0 = 2;
    private static final double splitAngle_0 = Math.toRadians(30);
    private static final double splitAngleVariation_0 = Math.toRadians(20);

    private static double segSplitsError_0 = 0.0;
    // * Would impact other parameters such as splitting rates.

    protected static IntegerAABBTree createTree(Random treeRandom, int sectorX, int sectorY, int sectorZ) {
        // Generate trunk - recursive level 0

        StemVec3d trunkOrigin = new StemVec3d(new Vec3d(treeRandom.nextInt(xzSectorSize)+sectorX*xzSectorSize,
                48, treeRandom.nextInt(xzSectorSize)+sectorZ*xzSectorSize));

        // Trunk's initial segment is aligned with the absolute z-axis. Maybe play with trunks that don't start vertical?
        StemVec3d zUnitOrigin = new StemVec3d(0, 0, 1);
        // Angle the trunk's x-axis is, relative to the absolute axes. Needed, b/c rotations are generally about the x-axis
        double xAngle = Helpers.randDoubleRange(treeRandom, -Math.PI, Math.PI);
        StemVec3d xUnit = new StemVec3d(Math.cos(xAngle), Math.sin(xAngle), 0);

        double treeScale = baseScale + randDoubleVariation(treeRandom, baseScaleVariation);
        double length = (length_0 + randDoubleVariation(treeRandom, lengthVariation_0)) * treeScale;
        double stemRadius = length*scale_0*ratio;

        ArrayList<GenerationFeature> generationFeatures = new ArrayList<>(3000);
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
        double lengthFraction = length/curveResolution_0;
        double unit_taper;
        if (taper_0 < 0) {
            throw new InvalidValueException("taper_0 cannot be negative.");
        } else if (taper_0 <= 1) {
            unit_taper = taper_0;
        } else if (taper_0 > 1) {
            throw new InvalidValueException("Program does not currently handle taper values greater than 1.");
        }

        StemVec3d currZUnit = zUnitOrigin;
        StemVec3d currOrigin = trunkOrigin;
        StemVec3d plane1Unit = zUnitOrigin;
        double prevRadiusZ = stemRadius;
        for (int i = 0; i < curveResolution_0; i++) {
            if (i == 0) {
                // FIXME What does Math.round mean by "ties round to positive infinity"?
                int baseSplitsEffective_0 = (int)Math.round(baseSplits_0 + segSplitsError_0);
                segSplitsError_0 = baseSplitsEffective_0-baseSplits_0;
                double declinationAngle = Math.acos(zUnitOrigin.dotProduct(currZUnit));
                int nBranches = baseSplitsEffective_0 + 1;
                double[] anglesSplit = new double[nBranches];
                for (int j = 0; j < nBranches; j++) {
                    anglesSplit[i] = Math.max(splitAngle_0+randDoubleVariation(treeRandom, splitAngleVariation_0) - declinationAngle, 0);
                }
            }
            double taperZ = stemRadius*(1-unit_taper*((double) i+1)/curveResolution_0);
            double radiusZ = taperZ;
            double theta = curve_0+randDoubleVariation(treeRandom, curveVariation_0);
            // Intersecting plane between this segment and the next. plane2Unit for curr segment is just this scaled by -1
            StemVec3d nextPlane1Unit = xUnit.rotateUnitVector(currZUnit, theta/2);

            DoubleTruncatedCone segment = new DoubleTruncatedCone(
                    currOrigin.toMCVector(), currZUnit.toMCVector(),
                    plane1Unit.toMCVector(), nextPlane1Unit.scale(-1).toMCVector(),
                    prevRadiusZ, radiusZ, lengthFraction);
            generationFeatures.add(segment);

            // Shift the next conical segment's origin down the line of max slope on the plane, to ensure its elliptical
            //  end lines up with the current segment's elliptical end.
            StemVec3d planeVec = xUnit.crossProduct(nextPlane1Unit);
            double coneSlope = lengthFraction/(radiusZ-prevRadiusZ);
            double planeMaxSlope = Math.tan(theta/2);
            double rMin = radiusZ*coneSlope/(coneSlope-planeMaxSlope);
            double rMax = radiusZ*coneSlope/(coneSlope+planeMaxSlope);
            double deltaR = rMax-rMin;
            double deltaH = planeMaxSlope*deltaR;
            double planeShift = Math.sqrt(deltaR*deltaR+deltaH*deltaH);

            currOrigin = currOrigin.add(currZUnit.scale(lengthFraction)).add(planeVec.scale(planeShift));
            prevRadiusZ = radiusZ;
            plane1Unit = nextPlane1Unit;
            currZUnit = xUnit.rotateUnitVector(currZUnit, theta); // Rotate next z-vector
        }
        ModYggdrasil.info("Tree for sector "+sectorX+","+sectorY+","+sectorZ+" created, with origin at "+trunkOrigin.toMCVector());
        return new IntegerAABBTree(generationFeatures.toArray(new GenerationFeature[0]));
    }

    protected static double randDoubleVariation(Random random, double variation) {
        // Is the variation supposed to be any value in the range [-variation, variation], or is it just supposed to be
        //  +/- variation (random sign, fixed magnitude)?
        return Helpers.randDoubleRange(random, -variation, variation);
    }
}
