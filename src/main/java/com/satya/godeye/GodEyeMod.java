package com.satya.godeye;

import com.satya.godeye.command.ModCommands;
import com.satya.godeye.entity.custom.TheEyeEntity;
import com.satya.godeye.registry.ModEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.util.ActionResult;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;

public class GodEyeMod implements ModInitializer {
    public static final String MOD_ID = "godeye";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean isTimeAccelerated = false;

    @Override
    public void onInitialize() {
        // GeckoLib.initialize();

        ModEntities.registerModEntities();
        FabricDefaultAttributeRegistry.register(ModEntities.THE_EYE, TheEyeEntity.createEyeAttributes());
        ModCommands.register();

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity victim) {
                entity.getWorld().getEntitiesByClass(TheEyeEntity.class,
                        entity.getBoundingBox().expand(200),
                        eye -> true
                ).forEach(eye -> {
                    eye.onPlayerKilled(victim, damageSource.getAttacker());
                });
            }
        });

        // 2. TICK EVENT (Smoother Fast Time)
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            if (isTimeAccelerated && world instanceof ServerWorld) {
                long currentTime = world.getTimeOfDay();
                // Add 25 ticks per tick (approx 1 min per day cycle)
                // This is fast enough to see shadows move, but slow enough to look "cinematic"
                ((ServerWorld) world).setTimeOfDay(currentTime + 25);
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClient && hand == Hand.MAIN_HAND && player.getStackInHand(hand).getItem() == Items.FISHING_ROD) {
                if (player.getCommandTags().contains("godeye_actor")) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    Vec3d targetPos = player.getPos();
                    spawnOrbitalStrike(serverWorld, targetPos);
                    player.sendMessage(Text.literal("§c[GodEye]§r ORBITAL STRIKE INITIATED."), true);
                    return ActionResult.PASS;
                }
            }
            return ActionResult.PASS;
        });
    }

    private void spawnOrbitalStrike(ServerWorld world, Vec3d center) {
        double spawnHeight = center.getY() + 80;
        int maxRadius = 35; // Reduced slightly for density

        // Loop rings: Step by 2 blocks (Much tighter rings)
        for (double r = 0; r <= maxRadius; r += 2.0) {

            // Circumference math
            double circumference = 2 * Math.PI * r;

            // Density: 1 TNT every 1.5 blocks along the ring (Very dense)
            int tntCount = (int) (circumference / 1.5);
            if (tntCount < 1) tntCount = 1;

            for (int i = 0; i < tntCount; i++) {
                double angle = (2 * Math.PI / tntCount) * i;

                double xOffset = r * Math.cos(angle);
                double zOffset = r * Math.sin(angle);

                TntEntity tnt = new TntEntity(EntityType.TNT, world);
                tnt.setPosition(center.getX() + xOffset, spawnHeight, center.getZ() + zOffset);
                tnt.setFuse(100);
                tnt.setVelocity(0, -2.0, 0);
                world.spawnEntity(tnt);
            }
        }
    }
}