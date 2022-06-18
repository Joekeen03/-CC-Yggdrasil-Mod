package com.joekeen03.yggdrasil.world.structure;

import java.util.Random;

public abstract class StemParams {
    public final int curveRes;
    public final double
            length, lengthVariation, taper,
            segSplits, splitAngle, splitAngleVariation,
            curve, curveBack, curveVariation;

    private double segSplitsError;

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
    }

    public abstract SegSplitError initializeError(Random random);
}
