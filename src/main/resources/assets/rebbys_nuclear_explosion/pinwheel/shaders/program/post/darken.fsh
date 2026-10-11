

uniform sampler2D DiffuseSampler0;
uniform float Factor0;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    fragColor = vec4(baseColor.rgb - Factor0*2, 1.0);
}
