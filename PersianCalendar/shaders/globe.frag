precision mediump float;
uniform vec2 resolution;
uniform float time;
uniform sampler2D tex0;

const float PI = 3.1415926;

void main() {
    float R = min(resolution.x, resolution.y) / 3.0;
    float x = resolution.x / 2.0 - gl_FragCoord.x;
    float y = resolution.y / 2.0 - gl_FragCoord.y;
    float l = x * x + y * y;
    float r = sqrt(l);
    if (r > R) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    } else {
        float z = sqrt(R * R - l);
        vec2 longLat = vec2((-atan(x, z) / PI + time / 2.0) * 0.5, (asin(y / R) / PI + .5));
        gl_FragColor = texture2D(tex0, longLat);
    }
}
