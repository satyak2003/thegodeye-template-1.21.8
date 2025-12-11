package com.satya.godeye.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import java.util.UUID;

// --- FIX: NEW STORAGE IMPORTS ---
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
// -------------------------------

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TheEyeEntity extends MobEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // Actor UUID Variable
    private UUID actorUuid;

    public TheEyeEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setPersistent();
    }

    public static DefaultAttributeContainer.Builder createEyeAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 100.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    // --- GECKOLIB LOGIC ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TheEyeEntity>(
                "controller", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("pupil.rotate"))
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // --- ACTOR LOGIC ---

    public void setActor(UUID uuid) {
        this.actorUuid = uuid;
    }

    public UUID getActorUuid() {
        return this.actorUuid;
    }

    // --- FIX: NEW PERSISTENCE METHODS ---

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view); // Always call super!
        if (this.actorUuid != null) {
            // Save UUID as a String
            view.putString("ActorUuid", this.actorUuid.toString());
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view); // Always call super!

        // Read string with a default empty value if missing
        String uuidStr = view.getString("ActorUuid", "");

        if (!uuidStr.isEmpty()) {
            try {
                this.actorUuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                this.actorUuid = null; // Ignore invalid data
            }
        }
    }

    // ------------------------------------

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient && hand == Hand.MAIN_HAND) {
            if (this.actorUuid != null && this.actorUuid.equals(player.getUuid())) {
                player.sendMessage(Text.literal("§d[The God Eye]§r I am watching you, my Vessel."), false);
            } else {
                player.sendMessage(Text.literal("§d[The God Eye]§r You are not my chosen vessel."), true);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    // --- GOD MODE LOGIC ---
    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.isSourceCreativePlayer() || source.getName().equals("outOfWorld")) {
            return super.damage(world, source, amount);
        }
        return false;
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) { return false; }
}