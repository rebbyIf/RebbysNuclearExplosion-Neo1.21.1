

uniform sampler2D DiffuseSampler0;
uniform float Factor0;
uniform float Factor1;

#define COLOR_MODULATION vec4(0,2,0,1)
#define OFFSET vec3(0.5)

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    vec4 modulatedColor = baseColor * COLOR_MODULATION;
    modulatedColor.rgb -= OFFSET;
    modulatedColor = modulatedColor * Factor0 + (1 - Factor0) * baseColor;
    fragColor = Factor1 + (1 - Factor1) * modulatedColor;
}
