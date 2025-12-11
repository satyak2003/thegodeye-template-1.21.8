package com.satya.godeye.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld; // Required for 1.21.2+
import net.minecraft.world.World;

public class TheEyeEntity extends MobEntity {

    public TheEyeEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setInvulnerable(true); // Vanilla invulnerability
        this.setPersistent();       // Prevents despawning
    }

    public static DefaultAttributeContainer.Builder createEyeAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 100.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    // --- GOD MODE LOGIC ---

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Allow damage ONLY from /kill (Creative) or Void
        if (source.isSourceCreativePlayer() || source.getName().equals("outOfWorld")) {
            return super.damage(world, source, amount);
        }
        return false; // Ignore ALL other damage
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }
}