package raytracing.shader;

import processing.core.PVector;

public interface SurfaceShader {
	  PVector Sample( PVector PP, float pwidth); 
	  PVector ApproxSample(PVector PP, float pwidth);
}
