package org.valkyrienskies.mod.common

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import kotlin.math.max
import kotlin.math.min

class ColliisionPairSavedData : SavedData() {
    private val pairs : MutableSet<Pair<Long, Long>> = HashSet()

    override fun save(compoundTag: CompoundTag): CompoundTag {
        val firstArr = ArrayList<Long>()
        val secondArr = ArrayList<Long>()
        pairs.forEach { pair: Pair<Long, Long> ->
            firstArr.add(pair.first)
            secondArr.add(pair.second)
        }
        compoundTag.putLongArray("FirstArray", firstArr)
        compoundTag.putLongArray("SecondArray", secondArr)
        return compoundTag
    }

    companion object {
        private var instance : ColliisionPairSavedData? = null

        fun add(id1: Long, id2: Long) {
            instance?.pairs?.add(orderedPair(id1, id2))
            instance?.setDirty()
        }

        fun remove(id1: Long, id2: Long) {
            instance?.pairs?.remove(orderedPair(id1, id2))
            instance?.setDirty()
        }

        private fun orderedPair(id1: Long, id2: Long) : Pair<Long,Long> {
            return Pair(min(id1, id2), max(id1, id2))
        }

        private fun create() : ColliisionPairSavedData {
            return ColliisionPairSavedData()
        }

        private fun load(tag: CompoundTag) : ColliisionPairSavedData  {
            val data = this.create()
            // Load saved data
            val firstArr = tag.getLongArray("FirstArray")
            val secondArr = tag.getLongArray("SecondArray")
            for (i in firstArr.indices) {
                data.pairs.add(Pair(firstArr[i], secondArr[i]))
            }
            return data
        }

        fun load(level: ServerLevel) : ColliisionPairSavedData {
            val data = level.dataStorage.computeIfAbsent(this::load, this::create, "collisionpairs")
            data.pairs.forEach { pair: Pair<Long, Long> ->
                level.shipObjectWorld.disableCollisionBetweenBodies(
                    pair.first, pair.second
                )
            }
            this.instance = data
            return data
        }
    }
}
