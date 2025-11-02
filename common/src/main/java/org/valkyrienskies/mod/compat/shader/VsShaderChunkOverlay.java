package org.valkyrienskies.mod.compat.shader;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.gl.shader.GlShader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderConstants;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderType;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkFogMode;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.minecraft.resources.ResourceLocation;

/**
 * Adapted from `me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer`
 */
public class VsShaderChunkOverlay {
    public record ShipShaderOverlayData(@Nonnull float[] color) {
        public float[] getColor() {
            return color;
        }
    }
    private final Map<ChunkShaderOptions, GlProgram<VsChunkShaderInterface>> programs = new Object2ObjectOpenHashMap<>();

    protected final ChunkVertexType vertexType;
    protected GlProgram<VsChunkShaderInterface> activeProgram;

    public VsShaderChunkOverlay(final ChunkVertexType vertexType) {
        this.vertexType = vertexType;
    }

    public  GlProgram<VsChunkShaderInterface> compileProgram(ChunkShaderOptions options) {
        GlProgram<VsChunkShaderInterface> program = this.programs.get(options);

        if (program == null) {
            this.programs.put(options, program = createShader("blocks/block_layer_opaque", options));
        }

        return program;
    }

    public GlProgram<VsChunkShaderInterface> createShader(String path, ChunkShaderOptions options) {
        ShaderConstants constants = options.constants();

        GlShader vertShader = ShaderLoader.loadShader(ShaderType.VERTEX,
            new ResourceLocation("valkyrienskies", path + ".vsh"), constants);
        GlShader fragShader = ShaderLoader.loadShader(ShaderType.FRAGMENT,
            new ResourceLocation("valkyrienskies", path + ".fsh"), constants);

        try {
            return GlProgram.builder(new ResourceLocation("valkyrienskies", "ship_shader"))
                .attachShader(vertShader)
                .attachShader(fragShader)
                .bindAttribute("a_PosId", VsChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID)
                .bindAttribute("a_Color", VsChunkShaderBindingPoints.ATTRIBUTE_COLOR)
                .bindAttribute("a_TexCoord", VsChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE)
                .bindAttribute("a_LightCoord", VsChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE)
                .bindFragmentData("fragColor", VsChunkShaderBindingPoints.FRAG_COLOR)
                .link((shader) -> new VsChunkShaderInterface(shader, options));
        } finally {
            vertShader.delete();
            fragShader.delete();
        }
    }

    public void begin(TerrainRenderPass pass) {
        pass.startDrawing();

        ChunkShaderOptions options = new ChunkShaderOptions(ChunkFogMode.SMOOTH, pass, this.vertexType);

        this.activeProgram = this.compileProgram(options);
        this.activeProgram.bind();
        this.activeProgram.getInterface()
            .setupState();
    }

    public void end(TerrainRenderPass pass) {
        this.activeProgram.unbind();
        this.activeProgram = null;

        pass.endDrawing();
    }

    public void delete(CommandList commandList) {
        this.programs.values().forEach(GlProgram::delete);
    }

    public GlProgram<VsChunkShaderInterface> getProgram() {
        return activeProgram;
    }

    public ChunkVertexType getVertexType() {
        return this.vertexType;
    }
}
