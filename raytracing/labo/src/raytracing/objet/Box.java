package raytracing.objet;

import raytracing.geometrie.*;

public class Box implements Primitive {
  PVector bmin;
  PVector bmax;

  public Box(PVector _bmin, PVector _bmax) {
    bmin = _bmin;
    bmax = _bmax;
  }

  public IntersectRes Intersect(PVector ro, PVector rd, float maxT, boolean isShadow) {
    PVector invr_d = new PVector();
    invr_d.set(1.f / rd.x, 1.f / rd.y, 1.f / rd.z);
    return new IntersectRes(Primitives.intersectBox(ro, invr_d, bmin, bmax), this);
  }

  public PVector GetModelSpace(PVector hitPoint) {
    PVector hp = new PVector();
    hp.set(hitPoint);
    PVector c = new PVector();
    c.set(bmin);
    c.add(bmax);
    c.mult(0.5f);
    hp.sub(c);
    return hp;
  }

  public PVector GetNormal(PVector hitPoint) {
    PVector hp = new PVector();
    PVector c = new PVector();
    c.set(bmin);
    c.add(bmax);
    c.mult(0.5f);
    hp.set(hitPoint);
    hp.sub(c);
    PVector p = new PVector(1.f / (bmax.x - bmin.x), 1.f / (bmax.y - bmin.y), 1.f / (bmax.z - bmin.z));
    hp = new PVector(p.x * hp.x, p.y * hp.y, p.z * hp.z);
    PVector d = new PVector(hp.x, 0.f, 0.f);
    if (Math.abs(hp.z) > Math.abs(hp.x)) {
      if (Math.abs(hp.z) > Math.abs(hp.y)) {
        d = new PVector(0.0f, 0.f, hp.z);
      } else {
        d = new PVector(0.f, hp.y, 0.f);
      }
    } else {
      if (Math.abs(hp.y) > Math.abs(hp.x)) {
        d = new PVector(0.f, hp.y, 0.f);
      }
    }
    d.normalize();
    return d;
  }
}
