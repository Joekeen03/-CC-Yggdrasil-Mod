package com.joekeen03.yggdrasil.world.structure.tree;

import java.util.Random;

public class SegSplitErrorBranch extends SegSplitError {
    private final BranchParams branchParams;

    public SegSplitErrorBranch(Random random, BranchParams branchParams) {
        super(random);
        this.branchParams = branchParams;
    }

    @Override
    public int getNextEffectiveSplits(int i) {
        return getNextEffectiveSplits(branchParams.segSplits);
    }
}
