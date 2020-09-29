package raytracing.objet;

import raytracing.geometrie.IntersectRes;
import raytracing.geometrie.PVector;

public interface Primitive {
  IntersectRes Intersect(PVector ro, PVector rd, float maxT, boolean isShadow);

  PVector GetNormal(PVector hitPoint);

  PVector GetModelSpace(PVector hitPoint);
}
