package org.valkyrienskies.mod.mixin.mod_compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import net.minecraft.client.Minecraft;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.compat.shader.IVsRenderWithOverlay;
import org.valkyrienskies.mod.compat.shader.VsChunkShaderInterface;
import org.valkyrienskies.mod.compat.shader.VsShaderChunkOverlay;
import org.valkyrienskies.mod.compat.shader.VsShaderChunkOverlay.ShipShaderOverlayData;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinDefaultChunkRenderer extends ShaderChunkRenderer implements IVsRenderWithOverlay {
    @Shadow
    public abstract void render(ChunkRenderMatrices matrices, CommandList commandList,
        ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera);

    @Unique
    private VsShaderChunkOverlay vs2$shaderOverlay;

    @Unique
    private ShipShaderOverlayData vs2$overlayData;

    public MixinDefaultChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);
    }

    @Inject(
        method = "<init>",
        at = @At("RETURN"),
        remap = false
    )
    private void vs2$init(RenderDevice device, ChunkVertexType vertexType, CallbackInfo ci) {
        this.vs2$shaderOverlay = new VsShaderChunkOverlay(vertexType);
    }

    @Unique
    public void vs2$renderOverlay(ShipShaderOverlayData overlayData, ChunkRenderMatrices matrices, CommandList commandList,
        ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera) {
        this.vs2$overlayData = overlayData;
        this.render(matrices, commandList, renderLists, renderPass, camera);
        this.vs2$overlayData = null;
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ShaderChunkRenderer;begin(Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V"
        ),
        remap = false
    )
    private void vs2$changeShaderBegin(DefaultChunkRenderer instance, TerrainRenderPass pass, Operation<Void> original) {
        if (this.vs2$overlayData == null) {
            original.call(instance, pass);
            return;
        }

        vs2$shaderOverlay.begin(pass);
    }

    @Redirect(
        method = "render",
        at = @At(
            value = "FIELD",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;activeProgram:Lme/jellysquid/mods/sodium/client/gl/shader/GlProgram;",
            opcode = Opcodes.GETFIELD
        ),
        remap = false
    )
    private GlProgram<? extends ChunkShaderInterface> vs2$injectProgram(DefaultChunkRenderer instance) {
        if (this.vs2$overlayData != null) {
            return vs2$shaderOverlay.getProgram();
        }
        return super.activeProgram;
    }

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ShaderChunkRenderer;end(Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;)V"
        ),
        remap = false
    )
    private void vs2$changeShaderEnd(DefaultChunkRenderer instance, TerrainRenderPass pass, Operation<Void> original) {
        if (this.vs2$overlayData == null) {
            original.call(instance, pass);
            return;
        }
        vs2$shaderOverlay.end(pass);
    }

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/shader/ChunkShaderInterface;setModelViewMatrix(Lorg/joml/Matrix4fc;)V"
        ),
        remap = false
    )
    private void vs2$setVsShaderUniforms(ChunkRenderMatrices matrices, CommandList commandList,
            ChunkRenderListIterable renderLists, TerrainRenderPass renderPass, CameraTransform camera, CallbackInfo ci) {
        if (this.vs2$overlayData == null) {
            return;
        }

        ChunkShaderInterface shaderInterface = vs2$shaderOverlay.getProgram().getInterface();
        if (shaderInterface instanceof final VsChunkShaderInterface vsChunkShaderInterface) {
            vsChunkShaderInterface.setUniformColorModulator(this.vs2$overlayData.getColor());
        }
    }

    @Inject(
        method = "delete",
        at = @At("HEAD"),
        remap = false
    )
    private void vs2$delete(CommandList commandList, CallbackInfo ci) {
        vs2$shaderOverlay.delete(commandList);
    }

    @Inject(
        method = "getVisibleFaces",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private static void cancelBlockFaceCulling(final int originX, final int originY, final int originZ, final int chunkX, final int chunkY, final int chunkZ, final CallbackInfoReturnable<Integer> cir) {
        if(VSGameUtilsKt.isChunkInShipyard(Minecraft.getInstance().level, chunkX, chunkZ))
            cir.setReturnValue(ModelQuadFacing.ALL);
    }
}
