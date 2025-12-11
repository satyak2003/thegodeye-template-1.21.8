package com.satya.godeye;

import com.satya.godeye.entity.custom.TheEyeEntity;
import com.satya.godeye.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry; // <--- MAKE SURE THIS IMPORT IS HERE
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
    }
}