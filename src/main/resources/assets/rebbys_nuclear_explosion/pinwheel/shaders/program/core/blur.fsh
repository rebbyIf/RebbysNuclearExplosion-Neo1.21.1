uniform sampler2D DiffuseSampler0;
uniform vec2 OutSize;

in vec2 texCoord;

out vec4 fragColor;

const float offset = 3;

void main() {
    vec2 halfpixel = 0.5 / OutSize;

    vec4 sum = texture(DiffuseSampler0, texCoord) * 4.0;
    sum += texture(DiffuseSampler0, texCoord - halfpixel.xy * offset);
    sum += texture(DiffuseSampler0, texCoord + halfpixel.xy * offset);
    sum += texture(DiffuseSampler0, texCoord + vec2(halfpixel.x, -halfpixel.y) * offset);
    sum += texture(DiffuseSampler0, texCoord - vec2(halfpixel.x, -halfpixel.y) * offset);
    sum = sum / 4.0;
    sum.w = min(1,sum.w);
    fragColor = sum;
}