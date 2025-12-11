package com.satya.godeye.client;

import com.satya.godeye.registry.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class GodEyeClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Link the Entity Logic to the Visual Renderer
		EntityRendererRegistry.register(ModEntities.THE_EYE, (context) -> new TheEyeRenderer(context));
	}
}