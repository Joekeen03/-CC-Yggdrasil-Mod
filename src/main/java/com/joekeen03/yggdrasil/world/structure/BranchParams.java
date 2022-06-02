package com.joekeen03.yggdrasil.world.structure;

public class BranchParams extends StemParams {
    public final int branches;
    public final double
            downAngle, downAngleVariation,
            rotate, rotateVariation;

    public BranchParams(double downAngle, double downAngleVariation,
                        double rotate, double rotateVariation, int branches,
                        double length, double lengthVariation, double taper,
                        double segSplits, double splitAngle, double splitAngleVariation,
                        int curveRes, double curve, double curveBack, double curveVariation) {
        super(length, lengthVariation, taper, segSplits, splitAngle, splitAngleVariation, curveRes, curve, curveBack, curveVariation);
        this.downAngle = downAngle;
        this.downAngleVariation = downAngleVariation;
        this.rotate = rotate;
        this.rotateVariation = rotateVariation;
        this.branches = branches;
    }

    // FIXME Should this be moved to a separate class? I.e. treat this class as level-global constant data, and another
    //  class holds level-global volatile data?
    @Override
    public int getNextEffectiveSplits(int i) {
        return getNextEffectiveSplits(segSplits);
    }
    @Deprecated
    public int getNextEffectiveSplits() {
        return getNextEffectiveSplits(segSplits);
    }
}
