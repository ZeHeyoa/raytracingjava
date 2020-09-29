package raytracing.shader;

import raytracing.geometrie.PVector;

public interface SurfaceShader {
	  PVector Sample( PVector PP, float pwidth); 
	  PVector ApproxSample(PVector PP, float pwidth);
}
