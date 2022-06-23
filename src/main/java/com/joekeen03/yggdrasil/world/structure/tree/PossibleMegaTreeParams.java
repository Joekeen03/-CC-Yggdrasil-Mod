package com.joekeen03.yggdrasil.world.structure.tree;

import static com.joekeen03.yggdrasil.world.structure.tree.TreeTypeParams.TreeTypeVaryIntEnum.levels;

public class PossibleMegaTreeParams {
    private static final double TEMP = 0;
    public static TreeTypeParams ModerateLevel1Splitting() {
        TrunkParams trunkParams = new TrunkParams(
                1.0, 0.1,
                300, 75, 0.5,
                0,
                0, Math.toRadians(0), Math.toRadians(0),
                10, Math.toRadians(0), Math.toRadians(0), Math.toRadians(0));

        BranchParams branch_1 = new BranchParams(
                Math.toRadians(50), Math.toRadians(-40),
                Math.toRadians(100), Math.toRadians(15), 10,
                0.7, 0.05, 0.95,
                0.14, Math.toRadians(20), Math.toRadians(20),
                10, Math.toRadians(-10), Math.toRadians(0), Math.toRadians(5));

        BranchParams branch_2 = new BranchParams(
                Math.toRadians(50), Math.toRadians(20),
                Math.toRadians(110), Math.toRadians(60), 10,
                0.9, 0.1, 1.0,
                0.0, Math.toRadians(40), Math.toRadians(10),
                10, Math.toRadians(10), Math.toRadians(0), Math.toRadians(3));

        BranchParams branch_3 = new BranchParams(
                Math.toRadians(50), Math.toRadians(20),
                Math.toRadians(110), Math.toRadians(60), 10,
                0.9, 0.1, 1.0,
                0.1, Math.toRadians(40), Math.toRadians(10),
                10, Math.toRadians(7), Math.toRadians(0), Math.toRadians(3));

        BranchParams[] branchParams = new BranchParams[] {branch_1, branch_2, branch_2};

        LeafParams leafParams = new LeafParams(50, 0, 0.17, 1);

        return new TreeTypeParams(
                "Testing",
                TreeTypeParams.TreeShape.MegaTree,
                0.66,
                1.0, 0.1, TEMP, TEMP,
                3,
                0.06, 0.1,
                (int)TEMP, TEMP,
                TEMP,
                0.5,
                trunkParams,
                branchParams,
                leafParams);
        // So-so. Definitely looked cluttered, might be due to excess branches, might be from the angle of the branches.
    }

    public static TreeTypeParams ModestLevel2Branching() {
        TrunkParams trunkParams = new TrunkParams(
                1.0, 0.1,
                300, 75, 0.5,
                0,
                0, Math.toRadians(0), Math.toRadians(0),
                10, Math.toRadians(0), Math.toRadians(0), Math.toRadians(0));

        BranchParams branch_1 = new BranchParams(
                Math.toRadians(50), Math.toRadians(-40),
                Math.toRadians(100), Math.toRadians(15), 10,
                0.7, 0.05, 0.95,
                0.14, Math.toRadians(20), Math.toRadians(20),
                10, Math.toRadians(-10), Math.toRadians(0), Math.toRadians(5));

        BranchParams branch_2 = new BranchParams(
                Math.toRadians(30), Math.toRadians(10),
                Math.toRadians(110), Math.toRadians(60), 6,
                0.9, 0.1, 1.0,
                0.0, Math.toRadians(40), Math.toRadians(10),
                10, Math.toRadians(10), Math.toRadians(0), Math.toRadians(3));

        BranchParams branch_3 = new BranchParams(
                Math.toRadians(30), Math.toRadians(5),
                Math.toRadians(70), Math.toRadians(20), 4,
                0.9, 0.1, 1.0,
                0.0, Math.toRadians(40), Math.toRadians(10),
                4, Math.toRadians(5), Math.toRadians(0), Math.toRadians(1));

        BranchParams branch_4 = new BranchParams(
                Math.toRadians(20), Math.toRadians(5),
                Math.toRadians(110), Math.toRadians(20), 4,
                0.4, 0.05, 1.0,
                0.0, Math.toRadians(40), Math.toRadians(10),
                4, Math.toRadians(5), Math.toRadians(0), Math.toRadians(1));

        BranchParams branch_5 = new BranchParams(
                Math.toRadians(20), Math.toRadians(5),
                Math.toRadians(70), Math.toRadians(20), 4,
                0.3, 0.03, 1.0,
                0.0, Math.toRadians(40), Math.toRadians(10),
                4, Math.toRadians(5), Math.toRadians(0), Math.toRadians(1));

        BranchParams branch_6 = new BranchParams(
                Math.toRadians(20), Math.toRadians(5),
                Math.toRadians(110), Math.toRadians(20), 4,
                0.3, 0.03, 1.0,
                0.0, Math.toRadians(40), Math.toRadians(10),
                4, Math.toRadians(5), Math.toRadians(0), Math.toRadians(1));

        BranchParams branch_7 = new BranchParams(
                Math.toRadians(20), Math.toRadians(5),
                Math.toRadians(110), Math.toRadians(20), 4,
                0.5, 0.03, 1.0,
                0.1, Math.toRadians(40), Math.toRadians(10),
                15, Math.toRadians(0), Math.toRadians(0), Math.toRadians(18));

        BranchParams[] branchParams = new BranchParams[] {
                branch_1, branch_2, branch_3,
                branch_4, branch_5, branch_6,
                branch_7};

        LeafParams leafParams = new LeafParams(50, 0, 0.17, 1);

        return new TreeTypeParams(
                "Testing Modest Level 2 Branching",
                TreeTypeParams.TreeShape.MegaTree,
                0.66,
                1.0, 0.1, TEMP, TEMP,
                3,
                0.06, 0.1,
                (int)TEMP, TEMP,
                TEMP,
                0.5,
                trunkParams,
                branchParams,
                leafParams);
        // I think the density is pretty close to where I want it, maybe just a bit lower.
        // Possible up the nCurveVary, to make the branches seem more gnarled? Seems really smooth right now.
        // Need to start thinking about the leaves...
        //  If the
    }

    public static TreeTypeParams SlightLevel2Branching() {
        return ModestLevel2Branching()
                .createBranchVariation("Testing Slight Level 2 Branching", 2,
                        BranchParams.BranchVaryIntEnum.branches, 5)
                .createBranchVariation("Testing Moderate Level 1 Curve Variation", 1,
                        BranchParams.BranchVaryDoubleEnum.curveVariation, Math.toRadians(100))
                .createBranchVariation("Testing Moderate Level 1 Curve Variation", 1,
                        BranchParams.BranchVaryDoubleEnum.curveVariation, Math.toRadians(130))
                .createBranchVariation("Testing High Level 2 Curve Variation", 2,
                        BranchParams.BranchVaryDoubleEnum.curveVariation, Math.toRadians(100))
                .createTreeTypeVariation("Testing High Level 2 Curve Variation", levels, 3);
        // Level 1 looks fine, but level 2 seems...cluttered?
        //  Might be spreading too much from their parents
        //  Too wavy - looks a little too similar to level 1?
    }

    public static TreeTypeParams Level2Tweaks() {
        return SlightLevel2Branching()
                .createBranchVariation("Testing Slight Level 2 Branching", 2,
                        BranchParams.BranchVaryIntEnum.branches, 4)
                .createBranchVariation("Testing Moderate Level 2 Curve Variation", 2,
                        BranchParams.BranchVaryDoubleEnum.curveVariation, Math.toRadians(60))
                .createBranchVariation("Testing Level 2 Planar Distribution", 2,
                        BranchParams.BranchVaryDoubleEnum.rotate, Math.toRadians(-80))
                .createBranchVariation("Testing Level 2 Planar Distribution", 2,
                        BranchParams.BranchVaryDoubleEnum.rotateVariation, Math.toRadians(50))
                .createBranchVariation("Testing Level 2 Planar Distribution", 2,
                        BranchParams.BranchVaryDoubleEnum.rotateVariation, Math.toRadians(70))
                .createBranchVariation("Testing Slight Level 2 Splitting", 2,
                        BranchParams.BranchVaryDoubleEnum.segSplits, 0.1);
        // Sorta okay, has trouble filling in the spaces between main branches
    }

    public static TreeTypeParams Level2FurtherTweaks() {
        return SlightLevel2Branching()
                .createBranchVariation("Testing Slight Level 2 Branching", 2,
                        BranchParams.BranchVaryIntEnum.branches, 4)
                .createBranchVariation("Testing Moderate Level 2 Down Angle", 2,
                        BranchParams.BranchVaryDoubleEnum.downAngle, Math.toRadians(24))
                .createBranchVariation("Testing Slight Level 2 Splitting", 2,
                        BranchParams.BranchVaryDoubleEnum.segSplits, 0.1)
                .createBranchVariation("Testing Modest Level 2 Splitting", 2,
                        BranchParams.BranchVaryDoubleEnum.segSplits, 0.2)
                .createBranchVariation("Testing Moderate Level 2 Curve Variation", 2,
                        BranchParams.BranchVaryDoubleEnum.curveVariation, Math.toRadians(60))
                .createTreeTypeVariation("Testing Lower Up-Attraction",
                        TreeTypeParams.TreeTypeVaryDoubleEnum.attractionUp, 0.2);
    }

    public static TreeTypeParams Level2AdditionalTweaks() {
        return Level2FurtherTweaks()
                .createBranchVariation("Testing Moderate Level 2 Down Angle", 2,
                        BranchParams.BranchVaryDoubleEnum.downAngle, Math.toRadians(35))
                .createBranchVariation("Testing Moderate Level 2 Split Angle", 2,
                        BranchParams.BranchVaryDoubleEnum.splitAngle, Math.toRadians(55))
                .createTreeTypeVariation("Testing Lower Up-Attraction",
                        TreeTypeParams.TreeTypeVaryDoubleEnum.attractionUp, 0.2); // FIXME Ambivalent about this - could work w/ or w/o this.
        // I think we have a winner - seems to fill the space pretty well.
        //  Does seem a bit chaotic, and could maybe use a slightly higher 2CurveVary, but I've spent long enough on this for now.
    }

    public static TreeTypeParams Level3Initial1() {
        return Level2AdditionalTweaks()
//                .createBranchVariation("Testing Moderate Level 3 Down Angle", 3,
//                        BranchParams.BranchVaryDoubleEnum.downAngle, Math.toRadians(35))
//                .createBranchVariation("Testing Moderate Level 3 Curve", 3,
//                        BranchParams.BranchVaryDoubleEnum.curve, Math.toRadians(-30))
                .createBranchVariation("Testing Negative Level 3 Rotate Angle", 3,
                        BranchParams.BranchVaryDoubleEnum.rotate, Math.toRadians(-50))
                .createBranchVariation("Testing Negative Level 3 Rotate Angle", 3,
                        BranchParams.BranchVaryDoubleEnum.rotateVariation, Math.toRadians(50))
                .createTreeTypeVariation("Testing Level 3", levels, 3);
        // So, seems to fill the space pretty well, but a lot of the branches dip down below the trunk's bottom, and I'm not sure
        //  how to remedy that - rotate=-50 and rotateV=50? downAngle=15? curve=-30? Set level 1 branches to start at
        //  a higher angle (smaller 1downAngle & 1downAngleV)?
    }

    public static TreeTypeParams Level3Initial2() {
        return Level2AdditionalTweaks()
//                .createBranchVariation("Testing Moderate Level 3 Down Angle", 3,
//                        BranchParams.BranchVaryDoubleEnum.downAngle, Math.toRadians(35))
//                .createBranchVariation("Testing Moderate Level 3 Curve", 3,
//                        BranchParams.BranchVaryDoubleEnum.curve, Math.toRadians(-30))
//                .createBranchVariation("Testing Negative Level 3 Rotate Angle", 3,
//                        BranchParams.BranchVaryDoubleEnum.rotate, Math.toRadians(-50))
//                .createBranchVariation("Testing Negative Level 3 Rotate Angle", 3,
//                        BranchParams.BranchVaryDoubleEnum.rotateVariation, Math.toRadians(50))
                .createTreeTypeVariation("Testing Higher Up-Attraction",
                        TreeTypeParams.TreeTypeVaryDoubleEnum.attractionUp, 0.5)
//                .createBranchVariation("Testing Smaller Level 1 DownAngle", 1,
//                        BranchParams.BranchVaryDoubleEnum.downAngle, Math.toRadians(40))
//                .createBranchVariation("Testing Smaller Level 1 DownAngle Variation", 1,
//                        BranchParams.BranchVaryDoubleEnum.downAngleVariation, Math.toRadians(-30))
                .createTreeTypeVariation("Testing Level 3", levels, 4);
        // So, seems to fill the space pretty well, but a lot of the branches dip down below the trunk's bottom, and I'm not sure
        //  how to remedy that - rotate=-50 and rotateV=50? downAngle=15? curve=-30? Set level 1 branches to start at
        //  a higher angle (smaller 1downAngle & 1downAngleV)?
        //  So, I think I see a few possible solutions:
        //      -Level 1 branches start at higher angles - seems to make the top very crowded - maybe combine with a lower branch count?
        //      -"Chop off" the lowest branch - set the down angles, branch count and bare area such that it skips that first problematic one
        //          (what if other branches prove problematic?). Seems identical to the first solution?
        //      -Increase the attraction up, so that the level 2 & 3 branches seem to start at higher angles.
        //      -Set the Level 3 branches to use a coplanar distribution - seemed to leave bare regions.
        //      -Use the HemiSpherical TreeShape (instead of my custom one), and set the level 1 branches to start at higher
        //          branch angles (downAngle = 40, downAngleV = -30)
        // Gonna use the higher attractionUp for now.
    }

    public static TreeTypeParams FullScaleTest() {
        return Level3Initial2()
                .createTrunkVariation("Testing Full Scale",
                        TrunkParams.TrunkVaryDoubleEnum.length, 1500)
                .createTrunkVariation("Testing Full Scale",
                        TrunkParams.TrunkVaryDoubleEnum.lengthVariation, 400)
                ;
    }

    public static final TreeTypeParams active = FullScaleTest();
}
