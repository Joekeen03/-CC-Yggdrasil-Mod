package com.joekeen03.yggdrasil.world.structure;

public class TrunkParams {
    public final int curveRes;
    public final double
            scale, scaleVariation,
            length, lengthVariation, taper,
            baseSplits,
            segSplits, splitAngle, splitAngleVariation,
            curve, curveBack, curveVariation;
    private double segSplitsError = 0.0; // TODO Maybe initialize this to a random value in the range (-0.5, 0.5]

    public TrunkParams(double scale, double scaleVariation,
                       double length, double lengthVariation, double taper,
                       double baseSplits,
                       double segSplits, double splitAngle, double splitAngleVariation,
                       int curveRes, double curve, double curveBack, double curveVariation) {
        this.scale = scale;
        this.scaleVariation = scaleVariation;
        this.length = length;
        this.lengthVariation = lengthVariation;
        this.taper = taper;
        this.baseSplits = baseSplits;
        this.segSplits = segSplits;
        this.splitAngle = splitAngle;
        this.splitAngleVariation = splitAngleVariation;
        this.curveRes = curveRes;
        this.curve = curve;
        this.curveBack = curveBack;
        this.curveVariation = curveVariation;
    }

    // FIXME Should this be moved to a separate class? I.e. treat this class as level-global constant data, and another
    //  class holds level-global volatile data?
    public int getNextEffectiveSplits(int i) {
        int segSplitsEffective = 0;
        if (i == 0) {
            // FIXME What does Math.round mean by "ties round to positive infinity"?
            segSplitsEffective = (int)Math.round(baseSplits + segSplitsError);
            segSplitsError = segSplitsEffective- baseSplits;
        } else {
            segSplitsEffective = (int)Math.round(segSplits + segSplits); // FIXME
            segSplitsError = segSplitsEffective- segSplits;
        }
        return segSplitsEffective;
    }
}
