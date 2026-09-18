/*
 * SPDX-License-Identifier: MIT
 */
package io.github.pasze888.occultismae2bridge;

import java.util.Arrays;
import java.util.Objects;

import appeng.api.AECapabilities;

import com.klikli_dev.occultism.common.blockentity.StorageControllerBlockEntity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(OccultismAE2Bridge.MODID)
public class OccultismAE2Bridge {

    public static final String MODID = "occultismae2bridge";

    // StorageControllerBlock shares one block entity, so every variant holds the same map-backed
    // item handler; the base/pedestal blocks have no block entity and are intentionally excluded.
    private static final String[] STORAGE_CONTROLLER_IDS = {
            "storage_controller",
            "storage_controller_stabilized",
            "storage_controller_dark",
            "storage_controller_stabilized_dark",
    };

    public OccultismAE2Bridge(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterCapabilities);
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        Block[] controllers = Arrays.stream(STORAGE_CONTROLLER_IDS)
                .map(id -> BuiltInRegistries.BLOCK
                        .get(ResourceLocation.fromNamespaceAndPath("occultism", id)))
                .filter(Objects::nonNull)
                .toArray(Block[]::new);

        if (controllers.length == 0) {
            return;
        }

        event.registerBlock(AECapabilities.ME_STORAGE,
                (level, pos, state, be, side) -> be instanceof StorageControllerBlockEntity controller
                        ? new OccultismStorageAdapter(controller)
                        : null,
                controllers);
    }
}
