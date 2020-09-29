package raytracing.shader;

import processing.core.PVector;

public class ParquetPlank implements SurfaceShader {
	public PVector Sample(PVector PP, float pwidth) {
		return Shader.LGParquetPlank(PP.x, PP.z, pwidth);
	}

	public PVector ApproxSample(PVector PP, float pwidth) {
		return new PVector(0.57f, 0.292f, 0.125f);
	}
}
