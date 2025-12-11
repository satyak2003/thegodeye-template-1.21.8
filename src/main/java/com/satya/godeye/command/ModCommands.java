package com.satya.godeye.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                .requires(source -> source.hasPermissionLevel(2)) // Ops only
                .then(CommandManager.literal("set")
                        .then(CommandManager.literal("actor")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(ModCommands::setActorCommand)
                                )
                        )
                )
        );
    }

    private static int setActorCommand(CommandContext<ServerCommandSource> context) {
        try {
            // 1. Get the player we want to be the actor
            ServerPlayerEntity targetActor = EntityArgumentType.getPlayer(context, "target");
            ServerCommandSource source = context.getSource();

            // 2. Find the nearest GodEye entity to the command sender
            // We search in a 50-block radius around the person typing the command
            List<TheEyeEntity> entities = source.getWorld().getEntitiesByClass(
                    TheEyeEntity.class,
                    Box.of(source.getPosition(), 50, 50, 50),
                    entity -> true
            );

            if (entities.isEmpty()) {
                source.sendError(Text.literal("No GodEye entity found nearby! Summon one first."));
                return 0;
            }

            // 3. Link them
            TheEyeEntity eye = entities.get(0); // Get the closest one
            eye.setActor(targetActor.getUuid());

            source.sendFeedback(() -> Text.literal("§aSuccess! " + targetActor.getName().getString() + " is now the Actor."), false);
            targetActor.sendMessage(Text.literal("§d[The God Eye]§r You have been chosen."), false);

            return 1;

        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error setting actor: " + e.getMessage()));
            return 0;
        }
    }
}