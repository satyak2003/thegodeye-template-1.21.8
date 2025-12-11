package com.satya.godeye.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.satya.godeye.entity.custom.TheEyeEntity;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import java.util.List;

public class ModCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(ModCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal("godeye")
                // .requires(source -> source.hasPermissionLevel(2)) // Optional: Remove if players should use it without OP

                // Subcommand: Set Actor
                .then(CommandManager.literal("set")
                        .then(CommandManager.literal("actor")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(ModCommands::setActorCommand)
                                )
                        )
                )

                // Subcommand: Revive (New!)
                .then(CommandManager.literal("revive")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ModCommands::reviveCommand)
                        )
                )
        );
    }

    // --- EXISTING SET ACTOR COMMAND ---
    private static int setActorCommand(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity targetActor = EntityArgumentType.getPlayer(context, "target");
            ServerCommandSource source = context.getSource();

            List<TheEyeEntity> entities = source.getWorld().getEntitiesByClass(
                    TheEyeEntity.class,
                    Box.of(source.getPosition(), 50, 50, 50),
                    entity -> true
            );

            if (entities.isEmpty()) {
                source.sendError(Text.literal("No GodEye entity found nearby!"));
                return 0;
            }

            TheEyeEntity eye = entities.get(0);
            eye.setActor(targetActor.getUuid());

            source.sendFeedback(() -> Text.literal("§aSuccess! " + targetActor.getName().getString() + " is now the Actor."), false);
            targetActor.sendMessage(Text.literal("§d[The God Eye]§r You have been chosen."), false);
            return 1;

        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    // --- NEW REVIVE COMMAND ---
    private static int reviveCommand(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
            ServerCommandSource source = context.getSource();

            // 1. Ensure the command sender is a player
            if (!source.isExecutedByPlayer()) {
                source.sendError(Text.literal("Only a player can perform the ritual."));
                return 0;
            }
            ServerPlayerEntity sender = source.getPlayer();

            // 2. Find nearby Eye
            List<TheEyeEntity> entities = source.getWorld().getEntitiesByClass(
                    TheEyeEntity.class,
                    Box.of(source.getPosition(), 50, 50, 50),
                    entity -> true
            );

            if (entities.isEmpty()) {
                source.sendError(Text.literal("The God Eye is not near. You cannot channel its power."));
                return 0;
            }
            TheEyeEntity eye = entities.get(0);

            // 3. Verify Authority (Is sender the Actor?)
            if (eye.getActorUuid() == null || !eye.getActorUuid().equals(sender.getUuid())) {
                source.sendError(Text.literal("§cYou are not the Actor. The Eye ignores your plea."));
                return 0;
            }

            // 4. Perform Resurrection
            eye.resurrectPlayer(targetPlayer);

            source.sendFeedback(() -> Text.literal("§aThe ritual is complete."), false);
            return 1;

        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}