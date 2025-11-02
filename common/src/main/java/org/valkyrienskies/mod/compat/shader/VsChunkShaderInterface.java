package org.valkyrienskies.mod.compat.shader;

import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat4v;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderOptions;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ShaderBindingContext;

public class VsChunkShaderInterface extends ChunkShaderInterface {
    private final GlUniformFloat4v uniformColorModulator;

    public VsChunkShaderInterface(ShaderBindingContext context, ChunkShaderOptions options) {
        super(context, options);

        this.uniformColorModulator = context.bindUniform("u_ColorModulator", GlUniformFloat4v::new);
    }

    public void setUniformColorModulator(final float[] rgba) {
        this.uniformColorModulator.set(rgba);
    }
}
