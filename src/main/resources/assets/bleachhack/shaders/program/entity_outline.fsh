#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 left   = texture(DiffuseSampler, texCoord - vec2(oneTexel.x, 0.0));
    vec4 right  = texture(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 up     = texture(DiffuseSampler, texCoord - vec2(0.0, oneTexel.y));
    vec4 down   = texture(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));

    float leftDiff  = abs(center.a - left.a);
    float rightDiff = abs(center.a - right.a);
    float upDiff    = abs(center.a - up.a);
    float downDiff  = abs(center.a - down.a);
 
    float alpha = leftDiff + rightDiff + upDiff + downDiff > 0.0 ? 1.0 : center.a;
    vec3 rgb = max(max(max(max(center.rgb, left.rgb), right.rgb), up.rgb), down.rgb);

    fragColor = vec4(rgb, alpha);
}