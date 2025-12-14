package com.satya.godeye.registry;

import com.satya.godeye.GodEyeMod;
import com.satya.godeye.entity.custom.TheEyeEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {

    // 1. Define the Identity Key
    public static final RegistryKey<EntityType<?>> THE_EYE_KEY = RegistryKey.of(
            RegistryKeys.ENTITY_TYPE,
            Identifier.of(GodEyeMod.MOD_ID, "the_eye")
    );

    // 2. Register the Entity using the Key
    public static final EntityType<TheEyeEntity> THE_EYE = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(GodEyeMod.MOD_ID, "the_eye"),
            EntityType.Builder.create(TheEyeEntity::new, SpawnGroup.CREATURE)
                    // CHANGE THIS: Match your visual scale roughly (Width, Height)
                    .dimensions(15.0f, 10.0f)
                    .build(RegistryKey.of(Registries.ENTITY_TYPE.getKey(), Identifier.of(GodEyeMod.MOD_ID, "the_eye")))
    );

    public static void registerModEntities() {
        GodEyeMod.LOGGER.info("Registering Entities for " + GodEyeMod.MOD_ID);
    }
}