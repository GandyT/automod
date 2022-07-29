package com.gandy.automod.events;

import com.gandy.automod.CommandManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
    private BlockState prevBlockState = null;

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
                BlockPos playerSpawn = new BlockPos(foundWood.getX() - 1, foundWood.getY() - 1, foundWood.getZ());

                if (xReq <= 1 && yReq < 6 && zReq <= 1) {
                    p.rotationYaw = -75;
                    p.rotationPitch = (float) (Math.atan((p.getPosY() + 1) - foundWood.getY()) / Math.PI) * 180;

                    // break the wood
                    BlockState blockState = mc.world.getBlockState(foundWood);
                    if (blockState.getMaterial() == Material.AIR) {
                        if (prevBlockState == null) {
                            mc.world.setBlockState(playerSpawn, Blocks.AIR.getDefaultState());
                            // tp player to safest spot
                            BlockPos closestY = closestYBlock(p);
                            teleport(p, closestY.getX(), closestY.getY() + 1, closestY.getZ());
                        } else {
                            mc.world.setBlockState(playerSpawn, prevBlockState);
                        }
                        foundWood = null;
                        prevBlockState = null;
                        return;
                    }
                    mc.playerController.onPlayerDamageBlock(foundWood, Direction.UP);
                } else {
                    System.out.println("teleporting player to wood");
                    // set block under player to glass and save block to revert it later

                    prevBlockState = mc.world.getBlockState(playerSpawn);
                    mc.world.setBlockState(playerSpawn, Blocks.GLASS.getDefaultState());

                    // tp player
                    teleport(p, foundWood.getX() - 1, foundWood.getY(), foundWood.getZ());
                    // p.setPosition(foundWood.getX() - 1, foundWood.getY(), foundWood.getZ());
                }

            }
        } else {
            foundWood = null;
            prevBlockState = null;
        }

        if (CommandManager.getInstance().autoMineCommand.active) {
            ClientPlayerEntity p = mc.player;
            p.rotationPitch = 45;
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKey(), true);
            // press left click
            Vector3d bv = mc.objectMouseOver.getHitVec();
            BlockPos bp = new BlockPos(bv.getX(), bv.getY(), bv.getZ());

            if (bv.getY() <= p.getPosY()) {
                p.rotationPitch = 45;
                return;
            }

            BlockState bs = mc.world.getBlockState(bp);
            if(bs.getMaterial() == Material.AIR) {
                if (p.rotationPitch < 70) {
                    p.rotationPitch = 70;
                } else {
                    p.rotationPitch = 45;
                }

                return;
            }

            mc.playerController.onPlayerDamageBlock(bp, Direction.UP);
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

    private BlockPos closestYBlock (ClientPlayerEntity player) {
        BlockPos safe = new BlockPos((int) player.getPosX(), (int) player.getPosY(), (int) player.getPosZ());
        BlockState safeState = mc.world.getBlockState(safe);

        while (safeState.getMaterial() == Material.AIR) {
            safe = new BlockPos(safe.getX(), safe.getY() - 1, safe.getZ());
            safeState = mc.world.getBlockState(safe);
        }

        return safe;
    }

    private void teleport(ClientPlayerEntity player, int x, int y, int z) {
        MinecraftServer s = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
        s.getCommandManager().handleCommand(s.getCommandSource(), "tp " + player.getName().getString() + " " + x + " " + y + " " + z);
    }
}
