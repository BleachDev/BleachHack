#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D OutputSampler;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 overlay = texture(DiffuseSampler, texCoord);
    vec4 main = texture(OutputSampler, texCoord);

    fragColor = vec4(main.r + ((overlay.r - main.r) * overlay.a),
                     main.g + ((overlay.g - main.g) * overlay.a),
                     main.b + ((overlay.b - main.b) * overlay.a),
                     min(overlay.a + main.a, 1.0));
}