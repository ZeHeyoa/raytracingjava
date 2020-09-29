package raytracing.shader;

import processing.core.PVector;

public class HexFloor implements SurfaceShader {
	public PVector Sample(PVector PP, float pwidth) {
		return Shader.HexTile(PP.x, PP.z, pwidth);
	}

	public PVector ApproxSample(PVector PP, float pwidth) {
		return new PVector(.55f, 0.f, 0.f);
	}
}
