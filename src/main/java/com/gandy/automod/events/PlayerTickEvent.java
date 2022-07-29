package com.gandy.automod.events;

import com.gandy.automod.CommandManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.loading.FMLCommonLaunchHandler;

import java.util.ArrayList;

public class PlayerTickEvent {

    private Minecraft mc;
    private BlockPos foundWood = null;

    public PlayerTickEvent () {
        mc = Minecraft.getInstance();
    }

    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event) {

        if (CommandManager.getInstance().autoWoodCommand.active) {

            ClientPlayerEntity p = mc.player;

            if (foundWood == null) {
                // scan blocks to find logs
                BlockPos matchPos = scan(p, 20, Material.WOOD);
                if (matchPos == null) {
                    // keep walking straight
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKey(), true);
                    p.rotationPitch = 0;
                    return;
                } else {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKey(), false);
                    System.out.println("Found wood block at: x:" + matchPos.getX() + " y:" + matchPos.getY() + " z:" + matchPos.getZ());
                    foundWood = matchPos;
                }
            } else {
                // walk to coordinates - find closest wood to player
                int xReq = (int) Math.abs(foundWood.getX() - p.getPosX());
                int yReq = (int) Math.abs(foundWood.getY() - p.getPosY());
                int zReq = (int) Math.abs(foundWood.getZ() - p.getPosZ());

                if (xReq <= 1 && yReq < 6 && zReq <= 1) {
                    p.rotationYaw = -75;
                    p.rotationPitch = (float) (Math.atan((p.getPosY() + 1) - foundWood.getY()) / Math.PI) * 180;

                    // break the wood
                    BlockState blockState = mc.world.getBlockState(foundWood);
                    if (blockState.getMaterial() == Material.AIR) {
                        foundWood = null;
                        return;
                    }
                    mc.playerController.onPlayerDamageBlock(foundWood, Direction.UP);
                } else {
                    System.out.println("teleporting player to wood");
                    MinecraftServer s = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
                    s.getCommandManager().handleCommand(s.getCommandSource(), "tp " + p.getName().getString() + " " + (foundWood.getX() - 1) + " " + foundWood.getY() + " " + foundWood.getZ());
                    // p.setPosition(foundWood.getX() - 1, foundWood.getY(), foundWood.getZ());
                }

            }
        } else {
            foundWood = null;
        }
    }

    private BlockPos scan(ClientPlayerEntity player, int radius, Material search) {
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        BlockPos closest = null;
        int px = (int) player.getPosX();
        int py = (int) player.getPosY();
        int pz = (int) player.getPosZ();

        for (int x = px - radius; x < px + radius; ++x) {
            for (int y = py - radius; y < py + radius; ++y) {
                for (int z = pz - radius; z < pz + radius; ++z) {
                    BlockState blockState = mc.world.getBlockState(blockPos.setPos(x, y, z));

                    if (blockState.getMaterial() == search) {
                        // check if its the closest
                        if (closest == null) {
                            // create a new BlockPos to remove reference from blockPos
                            closest = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        } else {

                            int cDis = Math.abs(closest.getX() - px) + Math.abs(closest.getY() - py) + Math.abs(closest.getZ() - pz);
                            int nDis = Math.abs(x - px) + Math.abs(y - py) + Math.abs(z - pz);

                            if (nDis < cDis) closest = new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        }
                    }
                }
            }
        }

        return closest;
    }
}
