package com.joekeen03.yggdrasil.world.structure;

public class BranchParams {
    public final int branches, curveRes;
    public final double
            downAngle, downAngleVariation,
            rotate, rotateVariation,
            length, lengthVariation, taper,
            segSplits, splitAngle, splitAngleVariation,
            curve, curveBack, curveVariation;

    private double segSplitsError = 0.0;

    public BranchParams(double downAngle, double downAngleVariation,
                        double rotate, double rotateVariation, int branches,
                        double length, double lengthVariation, double taper,
                        double segSplits, double splitAngle, double splitAngleVariation,
                        int curveRes, double curve, double curveBack, double curveVariation) {
        this.downAngle = downAngle;
        this.downAngleVariation = downAngleVariation;
        this.rotate = rotate;
        this.rotateVariation = rotateVariation;
        this.branches = branches;
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

    // FIXME Should this be moved to a separate class? I.e. treat this class as level-global constant data, and another
    //  class holds level-global volatile data?
    public int getNextEffectiveSplits() {
        int segSplitsEffective = (int)Math.round(segSplits + segSplits); // FIXME
        segSplitsError = segSplitsEffective- segSplits;
        return segSplitsEffective;
    }
}
