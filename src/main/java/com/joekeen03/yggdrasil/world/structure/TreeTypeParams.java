package com.joekeen03.yggdrasil.world.structure;

import com.joekeen03.yggdrasil.ModYggdrasil;
import com.joekeen03.yggdrasil.util.InvalidValueException;
import org.apache.commons.lang3.NotImplementedException;

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
            flare;
    public final int levels, lobes;
    public final TrunkParams trunkParams;
    public final BranchParams[] branchParams;

    public TreeTypeParams(String name,
                          TreeShape shape,
                          double baseSize,
                          double scale, double scaleVariation, double zScale, double zScaleVariation,
                          int levels,
                          double ratio, double ratioPower,
                          int lobes, double lobeDepth,
                          double flare,
                          TrunkParams trunkParams,
                          BranchParams[] branchParams) {
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
        this.trunkParams = trunkParams;
        this.branchParams = branchParams;
    }

    public enum TreeShape {
        Conical(ratio -> 0.2+0.8*ratio),
        Spherical(ratio -> 0.2+0.8*Math.sin(Math.PI*ratio)),
        Hemispherical(ratio -> 0.2+0.8*Math.sin(Math.PI/2*ratio)),
        Cylindrical(ratio -> 1.0),
        TaperedCylindrical(ratio -> 0.5+0.5*ratio),
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
}
