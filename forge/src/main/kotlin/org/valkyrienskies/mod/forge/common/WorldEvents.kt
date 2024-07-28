package org.valkyrienskies.mod.forge.common

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.valkyrienskies.mod.common.ColliisionPairSavedData
import org.valkyrienskies.mod.common.ValkyrienSkiesMod

@Mod.EventBusSubscriber(
    modid = ValkyrienSkiesMod.MOD_ID
)
class WorldEvents {
    companion object {
        @JvmStatic @SubscribeEvent
        fun onWorldLoad(event: WorldEvent.Load) {
            if (event.phase == EventPriority.NORMAL && event.world is ServerLevel &&
                (event.world as ServerLevel).dimension() == Level.OVERWORLD) {
                ColliisionPairSavedData.load(event.world as ServerLevel)
            }
        }
    }
}
