package com.satya.godeye.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.GameMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;

import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;

import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.particle.ParticleTypes;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.registry.Registries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.List;
import java.util.UUID;
import java.util.Random;
import java.util.Set;
import java.util.HashSet; // Import HashSet
import java.util.Collections;
import java.util.stream.Collectors; // Import Collectors

import net.minecraft.world.TeleportTarget;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TheEyeEntity extends MobEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID actorUuid;

    // NEW: Ignore List
    private final Set<UUID> ignoredPlayers = new HashSet<>();

    public enum QuestType { NONE, KILL, FETCH }
    private QuestType currentQuest = QuestType.NONE;
    private UUID targetPlayerUuid;
    private String targetPlayerName = "";
    private Item targetItem = Items.AIR;
    private boolean isQuestCompleted = false;
    private long lastInteractTime = 0;

    private static final Identifier REWARD_LOOT_TABLE = Identifier.of("godeye", "rewards/mission_reward");

    public TheEyeEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.setPersistent();
        this.moveControl = new FlightMoveControl(this, 20, true);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation navigation = new BirdNavigation(this, world);
        navigation.setCanSwim(true);
        return navigation;
    }

    public static DefaultAttributeContainer.Builder createEyeAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 100.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.FLYING_SPEED, 0.6)
                .add(EntityAttributes.GRAVITY, 0.0);
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TheEyeEntity>(
                "controller", 0,
                state -> state.setAndContinue(RawAnimation.begin().thenLoop("pupil.rotate"))
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getWorld().isClient) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 1.5;
            double y = this.getY() + 1.0 + (this.random.nextDouble() - 0.5) * 1.5;
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 1.5;
            this.getWorld().addParticleClient(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, x, y, z, 0.0, 0.0, 0.0);
        }
    }

    // --- ACTOR & QUEST LOGIC ---

    public void setActor(UUID uuid) {
        this.actorUuid = uuid;
        this.ignoredPlayers.clear(); // Clear ignore list on new actor? (Optional)
        this.currentQuest = QuestType.NONE;
        this.targetPlayerUuid = null;
        this.targetPlayerName = "";
        this.targetItem = Items.AIR;
        this.isQuestCompleted = false;
    }

    public UUID getActorUuid() { return this.actorUuid; }

    // NEW: Method to Add Ignored Player
    public boolean addIgnoredPlayer(UUID uuid) {
        return this.ignoredPlayers.add(uuid);
    }

    // NEW: Check if ignored
    public boolean isIgnored(UUID uuid) {
        return this.ignoredPlayers.contains(uuid);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getWorld().isClient && hand == Hand.MAIN_HAND) {
            long currentTime = this.getWorld().getTime();
            if (currentTime - this.lastInteractTime < 20) return ActionResult.SUCCESS;
            this.lastInteractTime = currentTime;

            if (this.actorUuid == null || !this.actorUuid.equals(player.getUuid())) {
                player.sendMessage(Text.literal("§c[The God Eye]§r You are unworthy!"), true);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
                this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.HOSTILE, 2.0f, 1.0f);
                if (this.getWorld() instanceof ServerWorld serverWorld) {
                    Vec3d dir = player.getPos().subtract(this.getPos()).normalize();
                    serverWorld.spawnParticles(ParticleTypes.SONIC_BOOM,
                            this.getX() + dir.x, this.getEyeY() + dir.y, this.getZ() + dir.z,
                            1, 0.0, 0.0, 0.0, 0.0);
                }
                return ActionResult.SUCCESS;
            }

            if (this.currentQuest == QuestType.FETCH && !this.isQuestCompleted) {
                if (player.getMainHandStack().getItem() == this.targetItem) {
                    completeQuest(player);
                    player.getMainHandStack().decrement(1);
                    return ActionResult.SUCCESS;
                }
            }

            if (this.currentQuest == QuestType.KILL && this.isQuestCompleted) {
                completeQuest(player);
                return ActionResult.SUCCESS;
            }

            if (this.currentQuest != QuestType.NONE) {
                String taskDesc = (this.currentQuest == QuestType.KILL)
                        ? "Kill §c" + this.targetPlayerName
                        : "Bring §b" + this.targetItem.getName().getString();

                player.sendMessage(Text.literal("§6[The God Eye]§r Finish your task first! (" + taskDesc + "§r)"), true);
                Vec3d direction = player.getPos().subtract(this.getPos()).normalize();
                player.setVelocity(direction.x * 3.0, 0.5, direction.z * 3.0);
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
                    serverPlayer.velocityModified = true;
                }
                player.damage((ServerWorld) this.getWorld(), this.getDamageSources().magic(), 10.0f);
                return ActionResult.SUCCESS;
            }

            generateNewQuest(player);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    private void generateNewQuest(PlayerEntity player) {
        Random random = new Random();
        if (random.nextBoolean()) {
            List<ServerPlayerEntity> players = this.getServer().getPlayerManager().getPlayerList();

            // UPDATED: Filter out Actor AND Ignored Players
            List<ServerPlayerEntity> targets = players.stream()
                    .filter(p -> !p.getUuid().equals(this.actorUuid)) // Not the actor
                    .filter(p -> !this.ignoredPlayers.contains(p.getUuid())) // Not ignored
                    .toList();

            if (targets.isEmpty()) {
                player.sendMessage(Text.literal("§d[The God Eye]§r No suitable targets found... I will wait."), false);
                return;
            }

            ServerPlayerEntity target = targets.get(random.nextInt(targets.size()));
            this.currentQuest = QuestType.KILL;
            this.targetPlayerUuid = target.getUuid();
            this.targetPlayerName = target.getName().getString();
            this.isQuestCompleted = false;
            player.sendMessage(Text.literal("§4[The God Eye]§r QUEST: Kill §c" + this.targetPlayerName + "§r."), false);
        } else {
            Item[] items = {Items.DIAMOND_SWORD, Items.GOLDEN_APPLE, Items.ENDER_EYE, Items.NETHERITE_INGOT};
            this.targetItem = items[random.nextInt(items.length)];
            this.currentQuest = QuestType.FETCH;
            this.isQuestCompleted = false;
            player.sendMessage(Text.literal("§d[The God Eye]§r QUEST: Bring me a §b" + this.targetItem.getName().getString() + "§r."), false);
        }
    }

    private void completeQuest(PlayerEntity player) {
        if (this.getWorld() instanceof ServerWorld serverWorld) {
            RegistryKey<LootTable> tableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, REWARD_LOOT_TABLE);
            LootTable lootTable = serverWorld.getServer().getReloadableRegistries().getLootTable(tableKey);

            if (lootTable == LootTable.EMPTY) {
                player.sendMessage(Text.literal("§c[Debug] Error: Reward file missing!"), false);
                return;
            }

            player.sendMessage(Text.literal("§a[The God Eye]§r Excellent. Here is your reward."), false);

            LootWorldContext.Builder builder = new LootWorldContext.Builder(serverWorld)
                    .add(LootContextParameters.THIS_ENTITY, this)
                    .add(LootContextParameters.ORIGIN, this.getPos());

            List<ItemStack> stacks = lootTable.generateLoot(builder.build(LootContextTypes.CHEST));
            for (ItemStack stack : stacks) {
                player.giveItemStack(stack);
            }
        }

        this.currentQuest = QuestType.NONE;
        this.targetPlayerUuid = null;
        this.targetPlayerName = "";
        this.targetItem = Items.AIR;
        this.isQuestCompleted = false;
    }

    public void onPlayerKilled(ServerPlayerEntity victim, Entity attacker) {
        if (this.currentQuest == QuestType.KILL && !this.isQuestCompleted) {
            if (victim.getUuid().equals(this.targetPlayerUuid) && attacker.getUuid().equals(this.actorUuid)) {
                this.isQuestCompleted = true;
                ServerPlayerEntity actor = this.getServer().getPlayerManager().getPlayer(this.actorUuid);
                if (actor != null) {
                    actor.sendMessage(Text.literal("§a[The God Eye]§r Target eliminated. Return to me."), false);
                }
            }
        }
    }

    public boolean resurrectPlayer(ServerPlayerEntity target) {
        if (target == null) return false;
        target.changeGameMode(GameMode.SURVIVAL);
        target.setHealth(target.getMaxHealth());
        target.getHungerManager().setFoodLevel(20);
        target.clearStatusEffects();
        target.extinguish();

        ServerWorld world = (ServerWorld) this.getWorld();
        TeleportTarget teleportTarget = new TeleportTarget(
                world,
                this.getPos(),
                Vec3d.ZERO,
                this.getYaw(),
                this.getPitch(),
                Set.of(),
                TeleportTarget.NO_OP
        );
        target.teleportTo(teleportTarget);

        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENTITY_WARDEN_EMERGE, SoundCategory.MASTER, 1.0f, 0.5f);

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    this.getX(), this.getY() + 1, this.getZ(),
                    50, 0.5, 0.5, 0.5, 0.5);
        }

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 600, 1));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200, 0));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
        target.sendMessage(Text.literal("§d[The God Eye]§r Death is not your end yet."), true);
        return true;
    }

    // --- DATA SAVING (Including Ignore List) ---

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (this.actorUuid != null) view.putString("ActorUuid", this.actorUuid.toString());
        view.putString("QuestType", this.currentQuest.name());
        view.putBoolean("QuestCompleted", this.isQuestCompleted);

        // Save Ignored Players as a comma-separated string
        if (!this.ignoredPlayers.isEmpty()) {
            String ignoredStr = this.ignoredPlayers.stream()
                    .map(UUID::toString)
                    .collect(Collectors.joining(","));
            view.putString("IgnoredPlayers", ignoredStr);
        }

        if (this.currentQuest == QuestType.KILL) {
            if (this.targetPlayerUuid != null) view.putString("TargetUuid", this.targetPlayerUuid.toString());
            view.putString("TargetName", this.targetPlayerName);
        } else if (this.currentQuest == QuestType.FETCH) {
            view.putString("TargetItem", Registries.ITEM.getId(this.targetItem).toString());
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view); // Always call super

        // 1. Load Actor UUID
        // view.getString now requires a default value (empty string)
        String actorStr = view.getString("ActorUuid", "");
        if (!actorStr.isEmpty()) {
            try {
                this.actorUuid = UUID.fromString(actorStr);
            } catch (Exception e) {
                this.actorUuid = null;
            }
        }

        // 2. Load Quest Type
        String questStr = view.getString("QuestType", "NONE");
        try {
            this.currentQuest = QuestType.valueOf(questStr);
        } catch (Exception e) {
            this.currentQuest = QuestType.NONE;
        }

        // 3. Load Quest Completion Status
        this.isQuestCompleted = view.getBoolean("QuestCompleted", false);

        // 4. Load Ignored Players List
        this.ignoredPlayers.clear();
        String ignoredStr = view.getString("IgnoredPlayers", "");

        if (!ignoredStr.isEmpty()) {
            // Now 'ignoredStr' is definitely a String, so .split() works
            String[] uuids = ignoredStr.split(",");
            for (String uuidStr : uuids) {
                try {
                    if (!uuidStr.isBlank()) {
                        this.ignoredPlayers.add(UUID.fromString(uuidStr.trim()));
                    }
                } catch (Exception e) {
                    // Ignore malformed UUIDs
                }
            }
        }
    }
    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (source.isSourceCreativePlayer() || source.getName().equals("outOfWorld")) return super.damage(world, source, amount);
        return false;
    }
    @Override
    public boolean isPushable() { return false; }
    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) { return false; }
}