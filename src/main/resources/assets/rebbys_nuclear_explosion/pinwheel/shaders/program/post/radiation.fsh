

uniform sampler2D DiffuseSampler0;
uniform float Factor0;
uniform float Factor1;

#define LUM vec3(0.299, 0.587, 0.114)
#define OFFSET vec3(2)

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(DiffuseSampler0, texCoord);
    vec3 gray = vec3(max(baseColor.x, max(baseColor.y, baseColor.z)));
    gray = gray * OFFSET - OFFSET / 2;
    vec4 mix = mix(baseColor, vec4(gray,1.0), min(Factor0*2.0,1.0));
    fragColor = Factor1 + (1 - Factor1) * mix;
}
