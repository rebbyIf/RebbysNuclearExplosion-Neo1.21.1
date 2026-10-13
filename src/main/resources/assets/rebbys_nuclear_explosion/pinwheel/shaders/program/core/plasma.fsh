#include rebbys_nuclear_explosion:fog

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = vec4(1,1,1,1);//texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor;
    //color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    fragColor = color;//linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}