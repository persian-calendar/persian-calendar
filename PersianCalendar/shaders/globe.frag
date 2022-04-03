#ifdef GL_ES
precision mediump float;
#endif
uniform vec2 u_resolution;
uniform float u_time;
uniform sampler2D u_tex0;

const float PI = 3.1415926;

void main() {
    float R = min(u_resolution.x, u_resolution.y) / 3.0;
    float x = u_resolution.x / 2.0 - gl_FragCoord.x;
    float y = u_resolution.y / 2.0 - gl_FragCoord.y;
    float l = x * x + y * y;
    float r = sqrt(l);
    if (r > R) {
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    } else {
        float z = sqrt(R * R - l);
        vec2 longLat = vec2((-atan(x, z) / PI + u_time / 2.0) * 0.5, (asin(y / R) / PI + .5));
        gl_FragColor = texture2D(u_tex0, longLat);
    }
}
