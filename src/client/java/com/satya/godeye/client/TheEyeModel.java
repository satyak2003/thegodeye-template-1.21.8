package com.satya.godeye.client;

import com.satya.godeye.entity.custom.TheEyeEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;
// CORRECT IMPORT
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class TheEyeModel extends GeoModel<TheEyeEntity> {

    // 1. Model Resource: Uses GeoRenderState
    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return Identifier.of("godeye", "geckolib/models/rift_eye.geo.json");
    }

    // 2. Texture Resource: Uses GeoRenderState
    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return Identifier.of("godeye", "textures/entity/rift_eye.png");
    }

    // 3. Animation Resource: Uses TheEyeEntity (T)
    // The error "implement getAnimationResource(T)" requires this specific signature.
    @Override
    public Identifier getAnimationResource(TheEyeEntity animatable) {
        return Identifier.of("godeye", "geckolib/animations/model.animation.json");
    }
}