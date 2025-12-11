package com.satya.godeye.client;

import com.satya.godeye.entity.custom.TheEyeEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

// DO NOT CHANGE THIS LINE.
// It defines "R" as a special type that is BOTH a LivingEntityRenderState AND a GeoRenderState.
public class TheEyeRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<TheEyeEntity, R> {

    public TheEyeRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new TheEyeModel());
    }
}