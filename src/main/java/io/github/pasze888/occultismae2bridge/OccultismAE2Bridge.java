/*
 * SPDX-License-Identifier: MIT
 */
package io.github.pasze888.occultismae2bridge;

import appeng.api.AECapabilities;

import com.klikli_dev.occultism.common.blockentity.StorageControllerBlockEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(OccultismAE2Bridge.MODID)
public class OccultismAE2Bridge {

    public static final String MODID = "occultismae2bridge";

    public OccultismAE2Bridge(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterCapabilities);
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        var storageController = BuiltInRegistries.BLOCK
                .get(ResourceLocation.fromNamespaceAndPath("occultism", "storage_controller"));

        event.registerBlock(AECapabilities.ME_STORAGE,
                (level, pos, state, be, side) -> be instanceof StorageControllerBlockEntity controller
                        ? new OccultismStorageAdapter(controller)
                        : null,
                storageController);
    }
}
