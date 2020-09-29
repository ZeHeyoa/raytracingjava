package raytracing.geometrie;

import raytracing.objet.Primitive;

public class IntersectRes {
  public float t;
  public Primitive hobj;

  public IntersectRes(float _t, Primitive _hobj) {
    t = _t;
    hobj = _hobj;
  }
}
