package com.satya.godeye.client;

import com.satya.godeye.entity.custom.TheEyeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
// 1. ADD THIS IMPORT
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class TheEyeRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<TheEyeEntity, R> {

    public TheEyeRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new TheEyeModel());

        // Scaling code (from previous step)
        this.withScale(4.0f);
        this.shadowRadius = 2.5f;

        // 2. ADD THE GLOW LAYER
        // This tells GeckoLib: "Look for a _glowmask.png file and render it at full brightness"
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}