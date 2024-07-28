package org.valkyrienskies.mod.common.item;

import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.mod.common.ColliisionPairSavedData
import org.valkyrienskies.mod.common.getShipManagingPos
import org.valkyrienskies.mod.common.shipObjectWorld

class ShipGroupNoCollisionItem(properties: Properties) : Item(properties) {
    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level as? ServerLevel ?: return super.useOn(ctx)
        val pos = ctx.clickedPos
        val blockState: BlockState = level.getBlockState(pos)

        if (!level.isClientSide) {
            val ship = ctx.level.getShipManagingPos(pos)
            if (ship != null) {
                val nbt = ctx.itemInHand.orCreateTag
                if (nbt.contains("Ships")) {
                    val ships : LongArray = nbt.getLongArray("Ships")
                    if (ctx.player?.isCrouching == true && ships.contains(ship.id)) {
                        ships.forEach { shipID ->
                            level.shipObjectWorld.enableCollisionBetweenBodies(shipID, ship.id)
                            ColliisionPairSavedData.remove(shipID, ship.id)
                        }
                        nbt.putLongArray("Ships", ships.filter { id -> id != ship.id })
                        ctx.player?.sendSystemMessage(Component.literal("Removed NoCollision"))

                    } else if (ctx.player?.isCrouching == false) {
                        ships.forEach { shipID ->
                            level.shipObjectWorld.disableCollisionBetweenBodies(shipID, ship.id)
                            ColliisionPairSavedData.add(shipID, ship.id)
                        }
                        nbt.putLongArray("Ships", ships + ship.id)
                        ctx.player?.sendSystemMessage(Component.literal("Activated NoCollision"))
                    }
                } else {
                    nbt.putLongArray("Ships", longArrayOf(ship.id))
                    ctx.player?.sendSystemMessage(Component.literal("Selected First Ship"))
                }
            }
        }

        return super.useOn(ctx)
    }

    override fun appendHoverText(itemStack: ItemStack, level: Level?, list: MutableList<Component>,
        tooltipFlag: TooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag)
        val nbt = itemStack.orCreateTag
        if (nbt.contains("Ships")) {
            val ships : LongArray = nbt.getLongArray("Ships")
            var string = ""
            ships.forEach { id ->
                string += ("$id, ")
            }
            list.add(Component.literal("Ships Contained: [ $string ]"))
        }
    }
}
