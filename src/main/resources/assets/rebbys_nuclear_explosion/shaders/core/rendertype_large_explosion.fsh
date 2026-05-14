#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

// Blurring stuff
uniform sampler2D DiffuseSampler;
uniform float Radius;
uniform float RadiusMultiplier;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;

// Blurring stuff
in vec2 sampleStep;

out vec4 fragColor;

void main() {

    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    
    //Blurring stuff
    vec4 blurred = vec4(0.0);
    float actualRadius = round(Radius * RadiusMultiplier);
    for (float a = -actualRadius + 0.5; a <= actualRadius; a += 2.0) {
        blurred += texture(DiffuseSampler, texCoord0 + sampleStep * a);
    }
    blurred += texture(DiffuseSampler, texCoord0 + sampleStep * actualRadius) / 2.0;
    blurred /= (actualRadius + 0.5);

    // Add blur to the final output to make it glow!
    //color += blurred;

    if (color.a < 0.1) {
        discard;
    }

    fragColor = (color + linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor)) / 2;
}
