package raytracing.shader;

import processing.core.PVector;

public class Wood implements SurfaceShader {
  public PVector Sample(PVector PP, float pwidth) {
    return Shader.wood2(PP, pwidth);
  }

  public PVector ApproxSample(PVector PP, float pwidth) {
    return new PVector(0.69f, 0.44f, 0.25f);
  }
}
