package com.joekeen03.yggdrasil.world.structure;

import java.util.Random;

public abstract class SegSplitError {
    private double segSplitsError;

    public SegSplitError(Random random) {
        segSplitsError = random.nextDouble()/2;
    }

    protected int getNextEffectiveSplits(double splits) {
        // FIXME What does Math.round mean by "ties round to positive infinity"?
        //  Think it means rounding up - 0.5 -> 1.0, 1.5 -> 2, -1.5 -> -1.0, and so on.
        int segSplitsEffective = (int)Math.round(splits + segSplitsError);
        segSplitsError -= segSplitsEffective-splits;
        return segSplitsEffective;
    }

    public abstract int getNextEffectiveSplits(int i);
}
