package net.rebby.rebbys_nuclear_explosion.client.rendertype;

import foundry.veil.api.client.render.VeilShaderBufferLayout;
import org.joml.Vector4f;

public class RadiationUniforms {
    private final Vector4f colorModulation;
    private final float factor;

    public RadiationUniforms() {
        this.colorModulation = new Vector4f(0,2,0,1);
        this.factor = 0;
    }

    public static VeilShaderBufferLayout<RadiationUniforms> createLayout() {
        return VeilShaderBufferLayout.<RadiationUniforms>builder()
                .vec4("ColorModulation", radiationUniforms -> radiationUniforms.colorModulation)
                .f32("Factor", radiationUniforms -> radiationUniforms.factor)
                .build();
    }
}
