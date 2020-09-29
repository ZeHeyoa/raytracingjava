package raytracing.objet;

import processing.core.PVector;
import raytracing.geometrie.IntersectRes;

public interface Primitive {
  IntersectRes Intersect(PVector ro, PVector rd, float maxT, boolean isShadow);

  PVector GetNormal(PVector hitPoint);

  PVector GetModelSpace(PVector hitPoint);
}
