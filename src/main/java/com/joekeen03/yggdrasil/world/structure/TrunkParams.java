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
    @Override
    public int getNextEffectiveSplits(int i) {
        if (i == 0) {
            return getNextEffectiveSplits(baseSplits);
        } else {
            return getNextEffectiveSplits(segSplits);
        }
    }

    public TrunkParams createVariation(TrunkVaryDoubleEnum param, double val) {
        double scale = this.scale;
        double scaleVariation = this.scaleVariation;
        double length = this.length;
        double lengthVariation = this.lengthVariation;
        double taper = this.taper;
        double baseSplits = this.baseSplits;
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
                lengthVariation=val;
                break;
            case taper:
                taper = val;
                break;
            case segSplits:
                segSplits=val;
                break;
            case splitAngle:
                splitAngle=val;
                break;
            case splitAngleVariation:
                splitAngleVariation=val;
                break;
            case curve:
                curve=val;
                break;
            case curveBack:
                curveBack=val;
                break;
            case curveVariation:
                curveVariation=val;
                break;
            case scale:
                scale=val;
                break;
            case scaleVariation:
                scaleVariation=val;
                break;
            case baseSplits:
                baseSplits=val;
                break;
        }
        return new TrunkParams(scale, scaleVariation,
                length, lengthVariation, taper,
                baseSplits,
                segSplits, splitAngle, splitAngleVariation,
                curveRes, curve, curveBack, curveVariation);
    }

    public TrunkParams createVariation(TrunkVaryIntEnum param, int val) {

        int curveRes = this.curveRes;
        switch(param) {
            case curveRes:
                curveRes=val;
                break;
        }
        return new TrunkParams(scale, scaleVariation,
                length, lengthVariation, taper,
                baseSplits,
                segSplits, splitAngle, splitAngleVariation,
                curveRes, curve, curveBack, curveVariation);
    }

    public enum TrunkVaryIntEnum {
        curveRes;
    }

    public enum TrunkVaryDoubleEnum {
        length, lengthVariation, taper,
        segSplits, splitAngle, splitAngleVariation,
        curve, curveBack, curveVariation,
        scale, scaleVariation,
        baseSplits;
    }
}
