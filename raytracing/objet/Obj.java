package raytracing.objet;

import raytracing.shader.SurfaceShader;

public class Obj {
  Primitive prim;
  float reflect;
  SurfaceShader shader;
  float metallic;

  public Obj(Primitive _prim, float _reflect, float _metal, SurfaceShader _shader) {
    prim = _prim;
    reflect = _reflect;
    shader = _shader;
    metallic = _metal;
  }

  public Primitive getPrim() {
    return prim;
  }

  public void setPrim(Primitive prim) {
    this.prim = prim;
  }

  public float getReflect() {
    return reflect;
  }

  public void setReflect(float reflect) {
    this.reflect = reflect;
  }

  public SurfaceShader getShader() {
    return shader;
  }

  public void setShader(SurfaceShader shader) {
    this.shader = shader;
  }

  public float getMetallic() {
    return metallic;
  }

  public void setMetallic(float metallic) {
    this.metallic = metallic;
  }
}
