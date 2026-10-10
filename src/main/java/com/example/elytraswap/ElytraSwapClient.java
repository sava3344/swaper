package com.example.elytraswap;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class ElytraSwapClient implements ClientModInitializer {

    // Финальный слот (индекс с 0): 3 = 4-й слот, 2 = 3-й слот
    private static final int FINAL_SLOT = 3;

    private static KeyMapping swapKey;

    private static int step = 0;       // 0 = ждём, 1 = ждём выбора слота, 2 = клик сделан
    private static int targetSlot = -1;

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
            step = 0;
            return;
        }

        // Шаг 2: слот выбран -> имитируем ПКМ
        if (step == 1) {
            if (player.getInventory().getSelectedSlot() == targetSlot) {
                simulate(mc.options.keyUse);
                step = 2;
            } else {
                step = 0; // слот не выбрался, отмена
            }
            return;
        }

        // Шаг 3: имитируем нажатие финального слота
        if (step == 2) {
            simulate(mc.options.keyHotbarSlots[FINAL_SLOT]);
            step = 0;
            return;
        }

        // Шаг 1: по R выбираем нужный предмет
        while (swapKey.consumeClick()) {
            if (mc.screen != null) continue;

            boolean wearingElytra = player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
            int slot = wearingElytra ? findChestplate(player) : findElytra(player);
            if (slot == -1) continue;

            targetSlot = slot;
            simulate(mc.options.keyHotbarSlots[slot]);
            step = 1;
        }
    }

    // Имитация нажатия клавиши с учётом твоей настоящей привязки
    private static void simulate(KeyMapping mapping) {
        KeyMapping.click(InputConstants.getKey(mapping.saveString()));
    }

    private static int findElytra(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(Items.ELYTRA)) return i;
        }
        return -1;
    }

    private static int findChestplate(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.ELYTRA)) continue;
            var eq = stack.get(DataComponents.EQUIPPABLE);
            if (eq != null && eq.slot() == EquipmentSlot.CHEST) return i;
        }
        return -1;
    }
}
