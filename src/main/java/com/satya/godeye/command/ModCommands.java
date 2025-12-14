package com.satya.godeye.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.satya.godeye.GodEyeMod;
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
                .then(CommandManager.literal("set")
                        .then(CommandManager.literal("actor")
                                .then(CommandManager.argument("target", EntityArgumentType.player())
                                        .executes(ModCommands::setActorCommand)
                                )
                        )
                )
                .then(CommandManager.literal("revive")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ModCommands::reviveCommand)
                        )
                )
                .then(CommandManager.literal("ignore")
                        .then(CommandManager.argument("target", EntityArgumentType.player())
                                .executes(ModCommands::ignoreCommand)
                        )
                )
                // START FAST TIME
                .then(CommandManager.literal("fast")
                        .executes(context -> {
                            GodEyeMod.isTimeAccelerated = true;
                            context.getSource().sendFeedback(() -> Text.literal("§e[GodEye]§r Time accelerates... (1 minute days)"), true);
                            return 1;
                        })
                )
                // STOP FAST TIME (Added 'stop' alias)
                .then(CommandManager.literal("stop")
                        .executes(ModCommands::stopTimeCommand)
                )
                .then(CommandManager.literal("normal")
                        .executes(ModCommands::stopTimeCommand)
                )
        );
    }

    // Extracted logic for stopping time so both /stop and /normal use it
    private static int stopTimeCommand(CommandContext<ServerCommandSource> context) {
        GodEyeMod.isTimeAccelerated = false;
        context.getSource().sendFeedback(() -> Text.literal("§e[GodEye]§r Time flows normally."), true);
        return 1;
    }

    private static int setActorCommand(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity targetActor = EntityArgumentType.getPlayer(context, "target");
            ServerCommandSource source = context.getSource();

            List<TheEyeEntity> entities = source.getWorld().getEntitiesByClass(
                    TheEyeEntity.class, Box.of(source.getPosition(), 50, 50, 50), entity -> true
            );

            if (entities.isEmpty()) {
                source.sendError(Text.literal("No GodEye entity found nearby!"));
                return 0;
            }

            TheEyeEntity eye = entities.get(0);
            eye.setActor(targetActor.getUuid());

            for (ServerPlayerEntity p : source.getServer().getPlayerManager().getPlayerList()) {
                p.removeCommandTag("godeye_actor");
            }
            targetActor.addCommandTag("godeye_actor");

            source.sendFeedback(() -> Text.literal("§aSuccess! " + targetActor.getName().getString() + " is now the Actor."), false);
            return 1;

        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int reviveCommand(CommandContext<ServerCommandSource> context) { return 1; }
    private static int ignoreCommand(CommandContext<ServerCommandSource> context) { return 1; }
}