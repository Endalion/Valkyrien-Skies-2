package org.valkyrienskies.mod.common.item;

import net.minecraft.Util
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
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

class ShipPairNoCollisionItem(properties: Properties) : Item(properties) {
    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level as? ServerLevel ?: return super.useOn(ctx)
        val pos = ctx.clickedPos
        val blockState: BlockState = level.getBlockState(pos)

        if (!level.isClientSide) {
            val ship = ctx.level.getShipManagingPos(pos)
            if (ship != null) {
                val nbt = ctx.itemInHand.orCreateTag
                if (nbt.contains("FirstShip") && nbt.getLong("FirstShip") != ship.id) {
                    val id1 = nbt.getLong("FirstShip")
                    if (ctx.player?.isCrouching == true) {
                        level.shipObjectWorld.enableCollisionBetweenBodies(id1, ship.id)
                        ColliisionPairSavedData.remove(id1, ship.id)
                        ctx.player?.sendMessage(TextComponent("Removed NoCollision"), Util.NIL_UUID)
                    } else {
                        level.shipObjectWorld.disableCollisionBetweenBodies(id1, ship.id)
                        ColliisionPairSavedData.add(id1, ship.id)
                        ctx.player?.sendMessage(TextComponent("Activated NoCollision"), Util.NIL_UUID)
                    }
                    nbt.remove("FirstShip")
                } else {
                    nbt.putLong("FirstShip", ship.id)
                    ctx.player?.sendMessage(TextComponent("Selected First Ship"), Util.NIL_UUID)
                }
            }
        }

        return super.useOn(ctx)
    }

    override fun appendHoverText(itemStack: ItemStack, level: Level?, list: MutableList<Component>,
        tooltipFlag: TooltipFlag) {
        super.appendHoverText(itemStack, level, list, tooltipFlag)
        val nbt = itemStack.orCreateTag
        if (nbt.contains("FirstShip")) list.add(TextComponent("First ID: " + nbt.getLong("FirstShip").toString()))
    }
}
