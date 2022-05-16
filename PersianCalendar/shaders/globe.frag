#ifdef GL_ES
precision highp float;
#endif
uniform vec2 u_resolution;
uniform float u_time;
uniform float u_y;
uniform sampler2D u_tex0;

const float PI = 3.1415926;

float rand(vec2 co) {
    // http://byteblacksmith.com/improvements-to-the-canonical-one-liner-glsl-rand-for-opengl-es-2-0/
    return fract(sin(mod(dot(co, vec2(12.9898, 78.233)), 3.14)) * 43758.5453);
}

void main() {
    float R = min(u_resolution.x, u_resolution.y) / 3.0; // supposed circle/globe radius
    vec2 xy = u_resolution / 2.0 - gl_FragCoord.xy; // screen center
    float r = length(xy); // radius from screen center
    if (r < R) {
        // https://en.wikipedia.org/wiki/Rotation_matrix#General_rotations
        vec3 xyz = mat3(1.0, 0.0, 0.0, 0, cos(-u_y), - sin(-u_y), 0, sin(-u_y), cos(-u_y)) *
            vec3(xy, sqrt(R * R - r * r)); // the third component, z, is height of points over globe surface
        // Converts x/y/z to texture coordinates https://en.wikibooks.org/wiki/GLSL_Programming/GLUT/Textured_Spheres
        vec2 longLat = vec2((-atan(xyz.x, xyz.z) - mod(u_time, PI * 1000.0)) / PI / 2.0, asin(xyz.y / R) / PI + .5);
        gl_FragColor = texture2D(u_tex0, longLat);
    } else {
        float v = 4.4 - r / R * 3.7; // Globe's glow
        // Adds random stars
        if (rand(vec2(floor(mod(u_time * 10.0 + xy.x / 4.0, 1000.0)), floor(xy.y / 4.0 + u_y * 10.0))) > 0.997)
            v = 1.0;
        gl_FragColor = vec4(v, v, v, 1.0);
    }
}
