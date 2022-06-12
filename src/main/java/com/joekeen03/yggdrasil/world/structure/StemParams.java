package com.joekeen03.yggdrasil.world.structure;

import java.util.Random;

public abstract class StemParams {
    public final int curveRes;
    public final double
            length, lengthVariation, taper,
            segSplits, splitAngle, splitAngleVariation,
            curve, curveBack, curveVariation;

    private double segSplitsError; // TODO Maybe initialize this to a random value in the range (-0.5, 0.5]

    public StemParams(double length, double lengthVariation, double taper,
                      double segSplits, double splitAngle, double splitAngleVariation,
                      int curveRes, double curve, double curveBack, double curveVariation) {
        this.length = length;
        this.lengthVariation = lengthVariation;
        this.taper = taper;
        this.segSplits = segSplits;
        this.splitAngle = splitAngle;
        this.splitAngleVariation = splitAngleVariation;
        this.curveRes = curveRes;
        this.curve = curve;
        this.curveBack = curveBack;
        this.curveVariation = curveVariation;
        resetSplitError();
    }

    public void resetSplitError() {
        segSplitsError = 0.0;
    }

    public void resetSplitError(Random random) {
//        segSplitsError = random.nextDouble()-0.5;
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
