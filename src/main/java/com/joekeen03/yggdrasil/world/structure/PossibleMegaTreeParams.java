package com.joekeen03.yggdrasil.world.structure;

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
                branchParams);
        // So-so. Definitely looked cluttered, might be due to excess branches, might be from the angle of the branches.
    }

    public static TreeTypeParams SlightLevel1Splitting() {
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
                Math.toRadians(50), Math.toRadians(20),
                Math.toRadians(110), Math.toRadians(60), 10,
                0.9, 0.1, 1.0,
                0.1, Math.toRadians(40), Math.toRadians(10),
                10, Math.toRadians(7), Math.toRadians(0), Math.toRadians(3));

        BranchParams[] branchParams = new BranchParams[] {branch_1, branch_2, branch_2};
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
                branchParams);
        // I think the density is pretty close to where I want it, maybe just a bit lower.
        // Possible up the nCurveVary, to make the branches seem more gnarled? Seems really smooth right now.
        // Need to start thinking about the leaves...
        //  If the
    }
}
