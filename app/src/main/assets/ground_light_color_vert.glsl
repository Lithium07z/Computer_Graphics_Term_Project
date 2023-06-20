#version 300 es

uniform mat4 uMVPMatrix, worldMat;
uniform vec3 eyePos;

layout(location = 0) in vec4 vPosition;
layout(location = 1) in vec3 vNormal;

out vec3 fNormal, fPosition;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    fNormal = normalize(transpose(inverse(mat3(worldMat))) * vNormal);
    fView = (worldMat * vPosition).xyz;
}