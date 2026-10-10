package com.example.elytraswap;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class ElytraSwapClient implements ClientModInitializer {

    private static final int FINAL_SLOT = 3; // 4-й слот (индекс с 0)

    private static KeyMapping swapKey;
    private static boolean pendingSecondTick = false;

    @Override
    public void onInitializeClient() {
        swapKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.elytraswap.swap",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.gameMode == null) {
            pendingSecondTick = false;
            return;
        }

        // Тик 2: вернуться на 4-й слот
        if (pendingSecondTick) {
            pendingSecondTick = false;
            player.getInventory().setSelectedSlot(FINAL_SLOT);
            return;
        }

        // Тик 1: найти элитры, выбрать слот и надеть через ПКМ
        while (swapKey.consumeClick()) {
            if (mc.screen != null) continue;

            int slot = findElytraInHotbar(player);
            if (slot == -1) continue;

            player.getInventory().setSelectedSlot(slot);
            mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
            player.swing(InteractionHand.MAIN_HAND);
            pendingSecondTick = true;
        }
    }

    private static int findElytraInHotbar(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(Items.ELYTRA)) {
                return i;
            }
        }
        return -1;
    }
}
