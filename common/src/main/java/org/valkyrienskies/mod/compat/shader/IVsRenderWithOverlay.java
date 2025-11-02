package org.valkyrienskies.mod.compat.shader;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import org.valkyrienskies.mod.compat.shader.VsShaderChunkOverlay.ShipShaderOverlayData;

public interface IVsRenderWithOverlay {
    void vs2$renderOverlay(ShipShaderOverlayData overlayData,
        ChunkRenderMatrices matrices, CommandList commandList,
        ChunkRenderListIterable renderLists, TerrainRenderPass renderPass,
        CameraTransform camera);
}
