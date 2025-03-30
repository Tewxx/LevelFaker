package com.github.tewxx.levelfaker;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod(modid = "levelcommand", version = "1.0", clientSideOnly = true)
public class XP {
    private static int savedLevel = -1;
    private static boolean shouldApplyLevel = false;
    private static int tickCounter = 0;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new LevelCommand());

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && shouldApplyLevel) {
            tickCounter++;

            if (tickCounter >= 10) {
                applyLevelIfPossible();
                tickCounter = 0;
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity == Minecraft.getMinecraft().thePlayer && savedLevel != -1) {
            shouldApplyLevel = true;
            tickCounter = 0;
        }
    }

    @SubscribeEvent
    public void onExperienceChange(TickEvent.PlayerTickEvent event) {
        if (event.player != null && event.player.equals(Minecraft.getMinecraft().thePlayer)
                && savedLevel != -1 && event.player.experienceLevel != savedLevel) {
            event.player.experienceLevel = savedLevel;
        }
    }

    private void applyLevelIfPossible() {
        if (Minecraft.getMinecraft().thePlayer != null && savedLevel != -1) {
            Minecraft.getMinecraft().thePlayer.experienceLevel = savedLevel;
            shouldApplyLevel = false;
        }
    }

    public static class LevelCommand extends CommandBase {
        @Override
        public String getCommandName() {
            return "level";
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/level <number>";
        }

        @Override
        public boolean canCommandSenderUseCommand(ICommandSender sender) {
            return true;
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) throws CommandException {
            if (args.length != 1) {
                sender.addChatMessage(new ChatComponentText("§cUsage: /level <number>"));
                return;
            }

            try {
                int level = Integer.parseInt(args[0]);

                if (level < 0) {
                    sender.addChatMessage(new ChatComponentText("§cLevel cannot be negative!"));
                    return;
                }

                savedLevel = level;

                if (Minecraft.getMinecraft().thePlayer != null) {
                    Minecraft.getMinecraft().thePlayer.experienceLevel = level;
                }

                sender.addChatMessage(new ChatComponentText("§aSet your level to " + level + ""));

                if (level == 0) {
                    savedLevel = -1;
                    sender.addChatMessage(new ChatComponentText("§aDisabled persistent level"));
                }
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText("§cInvalid number format!"));
            }
        }
        @Override
        public int getRequiredPermissionLevel() {
            return 0;
        }
    }
}
