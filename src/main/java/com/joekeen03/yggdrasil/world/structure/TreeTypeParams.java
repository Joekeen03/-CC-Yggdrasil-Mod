package com.joekeen03.yggdrasil.world.structure;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.util.InvalidValueException;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;

public class TreeTypeParams {
    public static final int LEAF_LEVELS = 1;
    public static final int TRUNK_LEVELS = 1;

    public final String name;
    public final TreeShape shape;
    public final double
            baseSize,
            scale, scaleVariation, zScale, zScaleVariation,
            ratio, ratioPower,
            lobeDepth,
            flare,
            attractionUp;
    public final int levels, lobes;
    public final TrunkParams trunkParams;
    public final BranchParams[] branchParams;
    public final LeafParams leafParams;

    public TreeTypeParams(String name,
                          TreeShape shape,
                          double baseSize,
                          double scale, double scaleVariation, double zScale, double zScaleVariation,
                          int levels,
                          double ratio, double ratioPower,
                          int lobes, double lobeDepth,
                          double flare,
                          double attractionUp,
                          TrunkParams trunkParams,
                          BranchParams[] branchParams,
                          LeafParams leafParams) {
        int branchLevels = levels-(LEAF_LEVELS+TRUNK_LEVELS);
        if (branchParams.length < branchLevels) {
            throw new InvalidValueException("BranchParams array must be at least as long as levels-2.");
        } else {
            for (int i = 0; i < branchLevels; i++) {
                if (branchParams[i] == null) {
                    throw new InvalidValueException("BranchParams array had a null value for an active branch level.");
                }
            }
        }
        if (branchParams.length > branchLevels) {
            ModYggdrasil.warn("BranchParams array is longer than necessary (length-2).");
        }
        this.name = name;
        this.shape = shape;
        this.baseSize = baseSize;
        this.scale = scale;
        this.scaleVariation = scaleVariation;
        this.zScale = zScale;
        this.zScaleVariation = zScaleVariation;
        this.levels = levels;
        this.ratio = ratio;
        this.ratioPower = ratioPower;
        this.lobes = lobes;
        this.lobeDepth = lobeDepth;
        this.flare = flare;
        this.attractionUp = attractionUp;
        this.trunkParams = trunkParams;
        this.branchParams = branchParams;
        this.leafParams = leafParams;
    }

    public StemParams fetchParams(int level) {
        if (level == 0) {
            return trunkParams;
        } else {
            return branchParams[level-1];
        }
    }

    public TreeTypeParams createBranchVariation(String newName, int branch, BranchParams.BranchVaryDoubleEnum param, double val) {
        BranchParams[] newBranchParams = Arrays.copyOf(branchParams, branchParams.length);
        newBranchParams[branch-1] = branchParams[branch-1].createVariation(param, val);
        return new TreeTypeParams(newName,
                shape,
                baseSize,
                scale, scaleVariation, zScale, zScaleVariation,
                levels,
                ratio, ratioPower,
                lobes, lobeDepth,
                flare,
                attractionUp,
                trunkParams,
                newBranchParams,
                leafParams);
    }

    public TreeTypeParams createBranchVariation(String newName, int branch, BranchParams.BranchVaryIntEnum param, int val) {
        BranchParams[] newParams = Arrays.copyOf(branchParams, branchParams.length);
        newParams[branch-1] = branchParams[branch-1].createVariation(param, val);
        return new TreeTypeParams(newName,
                shape,
                baseSize,
                scale, scaleVariation, zScale, zScaleVariation,
                levels,
                ratio, ratioPower,
                lobes, lobeDepth,
                flare,
                attractionUp,
                trunkParams,
                newParams,
                leafParams);
    }

    public TreeTypeParams createTrunkVariation(String newName, TrunkParams.TrunkVaryIntEnum param, int val) {
        TrunkParams newTrunkParams = trunkParams.createVariation(param, val);
        return new TreeTypeParams(newName,
                shape,
                baseSize,
                scale, scaleVariation, zScale, zScaleVariation,
                levels,
                ratio, ratioPower,
                lobes, lobeDepth,
                flare,
                attractionUp,
                newTrunkParams,
                branchParams,
                leafParams);
    }

    public TreeTypeParams createTrunkVariation(String newName, TrunkParams.TrunkVaryDoubleEnum param, double val) {
        TrunkParams newTrunkParams = trunkParams.createVariation(param, val);
        return new TreeTypeParams(newName,
                shape,
                baseSize,
                scale, scaleVariation, zScale, zScaleVariation,
                levels,
                ratio, ratioPower,
                lobes, lobeDepth,
                flare,
                attractionUp,
                newTrunkParams,
                branchParams,
                leafParams);
    }

    public TreeTypeParams createTreeTypeVariation(String newName, TreeTypeVaryDoubleEnum param, double val) {
        double baseSize = this.baseSize;
        double scale = this.scale;
        double scaleVariation = this.scaleVariation;
        double zScale = this.zScale;
        double zScaleVariation = this.zScaleVariation;
        double ratio = this.ratio;
        double ratioPower = this.ratioPower;
        double lobeDepth = this.lobeDepth;
        double flare = this.flare;
        double attractionUp = this.attractionUp;
        switch (param) {

            case baseSize:
                baseSize = val;
                break;
            case scale:
                scale = val;
                break;
            case scaleVariation:
                scaleVariation = val;
                break;
            case zScale:
                zScale = val;
                break;
            case zScaleVariation:
                zScaleVariation=val;
                break;
            case ratio:
                ratio=val;
                break;
            case ratioPower:
                ratioPower=val;
                break;
            case lobeDepth:
                lobeDepth=val;
                break;
            case flare:
                flare=val;
                break;
            case attractionUp:
                attractionUp=val;
                break;
        }
        return new TreeTypeParams(newName,
                this.shape,
                baseSize,
                scale, scaleVariation, zScale, zScaleVariation,
                this.levels,
                ratio, ratioPower,
                this.lobes, lobeDepth,
                flare,
                attractionUp,
                trunkParams,
                branchParams,
                leafParams);
    }

    public TreeTypeParams createTreeTypeVariation(String newName, TreeTypeVaryIntEnum param, int val) {
        int levels = this.levels;
        int lobes = this.lobes;
        switch (param) {
            case levels:
                levels = val;
                break;
            case lobes:
                lobes=val;
                break;
        }
        return new TreeTypeParams(newName,
                this.shape,
                this.baseSize,
                this.scale, this.scaleVariation, this.zScale, this.zScaleVariation,
                levels,
                this.ratio, this.ratioPower,
                lobes, this.lobeDepth,
                this.flare,
                this.attractionUp,
                trunkParams,
                branchParams,
                leafParams);
    }

    public TreeTypeParams createTreeTypeVariation(String newName, TreeShape newShape) {
        return new TreeTypeParams(newName,
                newShape,
                this.baseSize,
                this.scale, this.scaleVariation, this.zScale, this.zScaleVariation,
                this.levels,
                this.ratio, this.ratioPower,
                this.lobes, this.lobeDepth,
                this.flare,
                this.attractionUp,
                trunkParams,
                branchParams,
                leafParams);
    }

    public enum TreeShape {
        Conical(ratio -> 0.2+0.8*ratio),
        Spherical(ratio -> 0.2+0.8*Math.sin(Math.PI*ratio)),
        Hemispherical(ratio -> 0.2+0.8*Math.sin(Math.PI/2*ratio)),
        Cylindrical(ratio -> 1.0),
        TaperedCylindrical(ratio -> 0.5+0.5*ratio),
        MegaTree(ratio -> 0.6+0.4*Math.sin(Math.PI/2*ratio)), // Custom shape for my mega trees - might be better to use a pruning envelope.
        Flame(ratio -> {
            throw new NotImplementedException("Flame shape not yet implemented");
        }),
        InverseConical(ratio -> {
            throw new NotImplementedException("Inverse Conical shape not yet implemented");
        }),
        TendFlame(ratio -> {
            throw new NotImplementedException("Tend Flame shape not yet implemented");
        }),
        PruningEnvelope(ratio -> {
            throw new NotImplementedException("Prune shape does not have a ratio function.");
        });
        public final DoubleUnaryOperator getRatio;
        private TreeShape(DoubleUnaryOperator ratioFunction) {
            getRatio = ratioFunction;
        }
    }

    public enum TreeTypeVaryDoubleEnum {
        baseSize,
        scale, scaleVariation, zScale, zScaleVariation,
        ratio, ratioPower,
        lobeDepth,
        flare,
        attractionUp;
    }

    public enum TreeTypeVaryIntEnum {
        levels, lobes;
    }
}
