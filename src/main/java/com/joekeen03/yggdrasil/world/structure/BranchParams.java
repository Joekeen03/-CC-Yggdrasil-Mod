package com.joekeen03.yggdrasil.world.structure;

import java.util.Random;

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

    @Override
    public SegSplitError initializeError(Random random) {
        return new SegSplitErrorBranch(random, this);
    }

    public BranchParams createVariation(BranchVaryDoubleEnum param, double val) {

        double downAngle = this.downAngle;
        double downAngleVariation = this.downAngleVariation;
        double rotate = this.rotate;
        double rotateVariation = this.rotateVariation;
        double length = this.length;
        double lengthVariation = this.lengthVariation;
        double taper = this.taper;
        double segSplits = this.segSplits;
        double splitAngle = this.splitAngle;
        double splitAngleVariation = this.splitAngleVariation;
        double curve = this.curve;
        double curveBack = this.curveBack;
        double curveVariation = this.curveVariation;
        switch (param) {
            case length:
                length = val;
                break;
            case lengthVariation:
                lengthVariation = val;
                break;
            case taper:
                taper = val;
                break;
            case segSplits:
                segSplits = val;
                break;
            case splitAngle:
                splitAngle = val;
                break;
            case splitAngleVariation:
                splitAngleVariation = val;
                break;
            case curve:
                curve = val;
                break;
            case curveBack:
                curveBack=val;
                break;
            case curveVariation:
                curveVariation=val;
                break;
            case downAngle:
                downAngle = val;
                break;
            case downAngleVariation:
                downAngleVariation=val;
                break;
            case rotate:
                rotate=val;
                break;
            case rotateVariation:
                rotateVariation=val;
                break;
        }
        return new BranchParams(downAngle, downAngleVariation,
                rotate, rotateVariation, this.branches,
                length, lengthVariation, taper,
                segSplits, splitAngle, splitAngleVariation,
                this.curveRes, curve, curveBack, curveVariation);
    }

    public BranchParams createVariation(BranchVaryIntEnum param, int val) {

        int branches = this.branches;
        int curveRes = this.curveRes;
        switch (param) {
            case curveRes:
                curveRes = val;
                break;
            case branches:
                branches = val;
                break;
        }
        return new BranchParams(this.downAngle, this.downAngleVariation,
                this.rotate, this.rotateVariation, branches,
                this.length, this.lengthVariation, this.taper,
                this.segSplits, this.splitAngle, this.splitAngleVariation,
                curveRes, this.curve, this.curveBack, this.curveVariation);
    }

    public enum BranchVaryIntEnum {
        curveRes, branches;
    }

    public enum BranchVaryDoubleEnum {
        length, lengthVariation, taper,
        segSplits, splitAngle, splitAngleVariation,
        curve, curveBack, curveVariation,
        downAngle, downAngleVariation,
        rotate, rotateVariation;
    }
}
