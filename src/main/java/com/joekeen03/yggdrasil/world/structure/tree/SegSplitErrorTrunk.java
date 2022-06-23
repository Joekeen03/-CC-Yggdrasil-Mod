package com.joekeen03.yggdrasil.world.structure.tree;

import java.util.Random;

public class SegSplitErrorTrunk extends SegSplitError {

    private final TrunkParams trunkParams;

    public SegSplitErrorTrunk(Random random, TrunkParams trunkParams) {
        super(random);
        this.trunkParams = trunkParams;
    }

    // FIXME Should this be moved to a separate class? I.e. treat this class as level-global constant data, and another
    //  class holds level-global volatile data?
    @Override
    public int getNextEffectiveSplits(int i) {
        if (i == 0) {
            return getNextEffectiveSplits(trunkParams.baseSplits);
        } else {
            return getNextEffectiveSplits(trunkParams.segSplits);
        }
    }
}
