#version 300 es
precision mediump float;

uniform sampler2D sTexture;
uniform vec3 lightDir, lightAmbi, lightDiff, lightSpec;
uniform vec3 matAmbi, matSpec;
uniform float matSh; // shininess

uniform vec3 eyePos;
uniform float fogStart, fogEnd;
uniform vec3 fogColor;

in vec2 fTexCoord;
in vec3 fNormal, fPosition;

out vec4 fragColor;

void main() {
    // normalization
    vec3 normal = normalize(fNormal);
    vec3 light = normalize(lightDir);
    vec3 view = normalize(eyePos - fPosition);

    // ambient term
    vec3 ambi = lightAmbi * matAmbi;

    vec3 diff = vec3(0.0, 0.0, 0.0);
    vec3 spec = vec3(0.0, 0.0, 0.0);

    // diffuse term
    vec3 matDiff = texture(sTexture, fTexCoord).rgb;
    float dotNL = dot(normal, light);
    if (dotNL > 0.0) {
        diff = dotNL * lightDiff * matDiff;

        // specular term
        vec3 refl = 2.0 * normal * dot(normal, light) - light;
        spec = pow(max(dot(refl, view), 0.0), matSh) * lightSpec * matSpec;
    }

    float fogDepth = length(eyePos - fPosition);
    float fogFactor = smoothstep(fogStart, fogEnd, fogDepth);

    //fragColor = texture(sTexture, fTexCoord);
    //fragColor = vec4(ambi + diff + spec, 1.0);
    fragColor = vec4(mix(ambi + diff + spec, fogColor, fogFactor), 1.0);
}