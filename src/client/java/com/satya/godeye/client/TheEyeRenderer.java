package com.satya.godeye.client;

import com.satya.godeye.entity.custom.TheEyeEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;

public class TheEyeRenderer extends EntityRenderer<TheEyeEntity, EntityRenderState> {

    public TheEyeRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

}