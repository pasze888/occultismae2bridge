/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package io.github.pasze888.occultismae2bridge;

import java.util.Map;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.klikli_dev.occultism.common.blockentity.StorageControllerBlockEntity;
import com.klikli_dev.occultism.common.misc.ItemStackKey;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

/**
 * Bridges the Occultism Storage Controller to AE2 as native ME storage. Backed directly by the
 * controller's map-based item handler, so amounts are the true aggregated totals per item type
 * rather than the per-slot stack view the plain {@code IItemHandler} exposes.
 *
 * <p>Structurally adapted from Applied-Mekanistics' {@code QioStorageAdapter}
 * (Copyright ramidzkh, LGPL-3.0-or-later),
 * <a href="https://github.com/ramidzkh/Applied-Mekanistics">ramidzkh/Applied-Mekanistics</a>.
 */
public class OccultismStorageAdapter implements MEStorage {

    // Cache the ItemStack -> AEItemKey conversion so repeated getAvailableStacks polls do not
    // reallocate a key object for every stored type each time.
    private static final Map<ItemStackKey, AEItemKey> KEY_CACHE = new WeakHashMap<>();

    private final StorageControllerBlockEntity controller;

    public OccultismStorageAdapter(StorageControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey) || amount <= 0) {
            return 0;
        }

        ItemStack toInsert = itemKey.toStack((int) Math.min(amount, Integer.MAX_VALUE));
        ItemStack remainder = controller.itemStackHandler.insertItem(toInsert, mode.isSimulate());
        return Math.max(0, amount - remainder.getCount());
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey) || amount <= 0) {
            return 0;
        }

        ItemStack reference = itemKey.toStack();
        int requested = (int) Math.min(amount, Integer.MAX_VALUE);
        ItemStack extracted = controller.itemStackHandler.extractItem(reference, requested, mode.isSimulate());
        return extracted.getCount();
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        for (Object2IntMap.Entry<ItemStackKey> entry : controller.itemStackHandler.keyToCountMap().object2IntEntrySet()) {
            int count = entry.getIntValue();
            if (count <= 0) {
                continue;
            }
            AEItemKey key = KEY_CACHE.computeIfAbsent(entry.getKey(), it -> AEItemKey.of(it.stack()));
            if (key != null) {
                out.add(key, count);
            }
        }
    }

    @Override
    public Component getDescription() {
        return Component.translatable("block.occultism.storage_controller");
    }
}
