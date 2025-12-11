package com.satya.godeye;

import com.satya.godeye.entity.custom.TheEyeEntity;
import com.satya.godeye.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry; // <--- MAKE SURE THIS IMPORT IS HERE
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.satya.godeye.command.ModCommands;
import com.satya.godeye.entity.custom.TheEyeEntity;
import com.satya.godeye.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;

public class GodEyeMod implements ModInitializer {
    public static final String MOD_ID = "godeye";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        System.out.println("!!! DEBUG: INITIALIZATION STARTED !!!"); // PROOF 1

        ModEntities.registerModEntities();

        System.out.println("!!! DEBUG: REGISTERING ATTRIBUTES NOW !!!"); // PROOF 2
        FabricDefaultAttributeRegistry.register(ModEntities.THE_EYE, TheEyeEntity.createEyeAttributes());

        System.out.println("!!! DEBUG: INITIALIZATION COMPLETE !!!"); // PROOF 3

        System.out.println("!!!Registering Mod commands");
        ModCommands.register();

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // We only care if a PLAYER died
            if (entity instanceof ServerPlayerEntity victim) {
                // Find all God Eyes in the world and notify them
                // Note: In a real mod, you might want to optimize this search
                entity.getWorld().getEntitiesByClass(TheEyeEntity.class,
                        entity.getBoundingBox().expand(200), // Check within 200 blocks? Or Global?
                        eye -> true
                ).forEach(eye -> {
                    // Pass the victim and the killer (source.getAttacker())
                    eye.onPlayerKilled(victim, damageSource.getAttacker());
                });
            }
        });
    }
}