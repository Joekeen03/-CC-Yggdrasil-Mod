package com.joekeen03.yggdrasil.world.structure;

import com.joekeen03.yggdrasil.util.InvalidValueException;

public class TrunkParams extends StemParams {
    public final double
            scale, scaleVariation,
            baseSplits;

    public TrunkParams(double scale, double scaleVariation,
                       double length, double lengthVariation, double taper,
                       double baseSplits,
                       double segSplits, double splitAngle, double splitAngleVariation,
                       int curveRes, double curve, double curveBack, double curveVariation) {
        super(length, lengthVariation, taper, segSplits, splitAngle, splitAngleVariation, curveRes, curve, curveBack, curveVariation);
        if (lengthVariation >= length) {
            throw new InvalidValueException("TrunkParams received a lengthVariation greater than the length.");
        }
        this.scale = scale;
        this.scaleVariation = scaleVariation;
        this.baseSplits = baseSplits;
    }

    // FIXME Should this be moved to a separate class? I.e. treat this class as level-global constant data, and another
    //  class holds level-global volatile data?
    public int getNextEffectiveSplits(int i) {
        if (i == 0) {
            return getNextEffectiveSplits(baseSplits);
        } else {
            return getNextEffectiveSplits(segSplits);
        }
    }
}
