precision mediump float;
uniform float time;
uniform vec2 resolution;
void main() {
    // https://twitter.com/notargs/status/1250468645030858753
    vec3 d = .5 - vec3(gl_FragCoord.xy, 1.0) / resolution.y, p, o;
    for (int i = 0; i < 32; ++i) {
        o = p;
        o.z -= time * 9.;
        float a = o.z * .1;
        o.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        p += (.1 - length(cos(o.xy) + sin(o.yz))) * d;
    }
    gl_FragColor = vec4((sin(p) + vec3(2, 5, 9)) / length(p) * vec3(1), 1);
}

