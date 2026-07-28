

uniform sampler2D DiffuseSampler0;
uniform vec4 ColorModulation;
uniform float Factor;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    vec4 modulatedColor = baseColor * ColorModulation;
    modulatedColor.rgb -= vec3(0.5);
    fragColor = modulatedColor * Factor + (1 - Factor) * baseColor;
}
