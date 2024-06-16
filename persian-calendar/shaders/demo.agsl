// This is a SkSl/AGSL flavor shader only usable in Android 13, see also:
// * https://shaders.skia.org/?id=de2a4d7d893a7251eb33129ddf9d76ea517901cec960db116a1bbd7832757c1f
// * https://developer.android.com/about/versions/13/features#graphics
// * https://cs.android.com/android/platform/superproject/+/master:external/skia/src/sksl/SkSLMain.cpp;l=275

uniform float iTime;
uniform vec2 iResolution;

// Source: @notargs https://twitter.com/notargs/status/1250468645030858753
half4 main(vec2 fragCoord) {
    vec3 d = .5 - fragCoord.xy1 / iResolution.y, p = vec3(0), o;
    for (int i = 0; i < 32; ++i) {
        o = p;
        o.z -= iTime * 9.;
        float a = o.z * .1;
        o.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        p += (.1 - length(cos(o.xy) + sin(o.yz))) * d;
    }
    return ((sin(p) + vec3(2, 5, 12)) / length(p)).xyz1;
}
