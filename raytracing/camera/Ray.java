package raytracing.camera;

import processing.core.PVector;
import raytracing.shader.Shader;

public class Ray {
  public PVector origin;
  public PVector direction;

  Ray(PVector o, PVector d) {
    origin = o;
    direction = d;
  }

  public void Lerp(Ray or, float t) {
    origin = Shader.lerp(origin, or.origin, t);
    direction = Shader.lerp(direction, or.direction, t);
  }
}
