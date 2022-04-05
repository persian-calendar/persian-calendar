#ifdef GL_ES
precision highp float;
#endif
uniform vec2 u_resolution;
uniform float u_time;
uniform sampler2D u_tex0;

const float PI = 3.1415926;

float rand(vec2 co) {
    return fract(sin(mod(dot(co, vec2(12.9898, 78.233)), 3.14)) * 43758.5453);
}

void main() {
    float R = min(u_resolution.x, u_resolution.y) / 3.0;
    vec2 xy = u_resolution / 2.0 - gl_FragCoord.xy;
    float r = length(xy);
    if (r > R) {
        float b = 4.4 - r / R * 3.7;
        if (rand(vec2(floor(mod(u_time * 10.0 + xy.x / 4.0, 400.0)), floor(xy.y / 4.0))) > 0.997)
            b = 1.0;
        gl_FragColor = vec4(b, b, b, 1.0);
    } else {
        float z = sqrt(R * R - r * r);
        vec2 longLat = vec2((-atan(xy.x, z) / PI - u_time / 5.0) * 0.5, (asin(xy.y / R) / PI + .5));
        gl_FragColor = texture2D(u_tex0, longLat);
    }
}
