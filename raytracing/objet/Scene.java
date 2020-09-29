package raytracing.objet;

import processing.core.PVector;
import raytracing.camera.*;
import raytracing.geometrie.IntersectRes;
import raytracing.objet.Obj;
import raytracing.shader.*;

public class Scene {
  Obj[] m_obj;
  PolarCamera m_cam;
  boolean useHemi;
  boolean useDirect;
  boolean useReflect;
  boolean useBounce;

  PVector g_LightPos = new PVector(0.f, 4.f, -1.f);
  PVector g_LightTan = new PVector(2.5f, 0.f, 0.f);
  PVector g_LightBi = new PVector(0.f, 0.f, 2.5f);
  PVector g_SkyColor = new PVector(1.9f * .25f, 0.9f * .25f, 1.2f * .25f);
  
  public boolean isUseHemi() {
    return useHemi;
  }

  public void setUseHemi(boolean useHemi) {
    this.useHemi = useHemi;
  }

  public boolean isUseDirect() {
    return useDirect;
  }

  public void setUseDirect(boolean useDirect) {
    this.useDirect = useDirect;
  }

  public boolean isUseReflect() {
    return useReflect;
  }

  public void setUseReflect(boolean useReflect) {
    this.useReflect = useReflect;
  }

  public boolean isUseBounce() {
    return useBounce;
  }

  public void setUseBounce(boolean useBounce) {
    this.useBounce = useBounce;
  }

  public Scene(Obj[] _obj, PolarCamera _cam, boolean _amb, boolean _dir, boolean _ref, boolean _ub) {
    m_obj = _obj;
    m_cam = _cam;
    useHemi = _amb;
    useDirect = _dir;
    useReflect = _ref;
    useBounce = _ub;
  }

  public void Dump() {
    /* PrintWriter out = createWriter("positions.txt"); out.println( "new Scene(ObjList," + m_cam.Settings() + ", " + useHemi + ", "+ useDirect + ", " + useReflect + ", " + useBounce + ") " );
     * out.flush(); out.close(); */
  }

  public PolarCamera getPolarCamera() {
    return m_cam;
  }

  public void setPolarCamera(PolarCamera m_cam) {
    this.m_cam = m_cam;
  }

  public Obj[] getObjets() {
    return m_obj;
  }

  public void setObjets(Obj[] m_obj) {
    this.m_obj = m_obj;
  }

  public PolarCamera getCamera() {
    return m_cam;
  }

  public void setCamera(PolarCamera m_cam) {
    this.m_cam = m_cam;
  }


  // TODO - separate direct lighting from bounce EBT on direct , always leave bounce
  PVector calculateLight(Obj objectList[], PVector p, PVector nrm, PVector I, PVector diff, QMCSamples sampler, int sc, int numSamples, int depth, boolean g_ShowLightingOnly) {

    PVector lightPosX = new PVector();
    PVector lightPosY = new PVector();
    PVector scol = new PVector(0, 0, 0);

    if (numSamples == 0) {
      return scol;
    }
    int numDirSamples = isUseDirect() ? numSamples : 0;
    // numDirSamples = depth ==0 ? 0 : numDirSamples;

    for (int i = 0; i < numDirSamples; i++) {
      float[] lgtSample = sampler.Get(sc * numSamples + i, 1 + depth * 2);
      lightPosX.set(g_LightTan);
      lightPosX.mult(lgtSample[0]);

      lightPosY.set(g_LightBi);
      lightPosY.mult(lgtSample[1]);
      PVector lightPos = new PVector();
      lightPos.set(lightPosX);
      lightPos.add(lightPosY);
      lightPos.add(g_LightPos);

      PVector lDir = new PVector();
      lDir.set(lightPos);
      lDir.sub(p);
      float len = lDir.mag();
      lDir.mult(1.f / len);

      float ndotl = Math.max(nrm.dot(lDir), 0.f);

      if (ndotl > 0.) {
        float shadow = ShadowTrace(objectList, p, lDir, len);
        // specular
        PVector hangle = new PVector();
        hangle.set(I);
        hangle.mult(-1);
        hangle.add(lDir);
        hangle.normalize();

        float spec = pow16(Math.max(hangle.dot(nrm), 0)) * shadow;
        PVector speccol = new PVector();
        speccol.set(1.f, 1.f, 1.f);
        speccol.mult(spec * 1.f / numSamples);
        scol.add(speccol);

        ndotl *= shadow * 2.;
        PVector cl = new PVector();
        cl.set(diff);
        cl.mult(ndotl * 1.f / numSamples);

        scol.add(cl);
      }
    }

    if (depth > 3)
      return scol;

    // calculate hemi -maybe do four?
    if (isUseHemi() || isUseBounce()) {
      PVector tg = new PVector();
      if (Math.abs(nrm.y) < 0.99f) {
        tg = new PVector(0.f, 1.f, 0.f);
      } else {
        tg = new PVector(1.f, 0.f, 0.f);
      }
      PVector bi = tg.cross(nrm);
      bi.normalize();
      tg = nrm.cross(bi);

      float amb = 0.f;
      int numSubSamples = numSamples;
      PVector hdir = new PVector();

      float wieght = 1.f / numSubSamples;
      for (int i = 0; i < numSubSamples; i++) {
        float[] hemiSample = sampler.Get(sc * numSubSamples + i,
            3 + depth * 2);
        PVector hemi = Sampling.to_unit_disk(hemiSample[0],
            hemiSample[1]);

        hdir.x = tg.x * hemi.x + nrm.x * hemi.y + bi.x * hemi.z;
        hdir.y = tg.y * hemi.x + nrm.y * hemi.y + bi.y * hemi.z;
        hdir.z = tg.z * hemi.x + nrm.z * hemi.y + bi.z * hemi.z;
        if (isUseBounce()) {
          TracePoint trr = trace(objectList, p, hdir, sampler, sc, depth + 1, 1, g_ShowLightingOnly);
          trr.col.mult(wieght);
          // trr.col = PVector.mult(diff,0,trr.col);
          trr.col = new PVector(diff.x * trr.col.x, diff.y
              * trr.col.y, diff.z * trr.col.z);
          scol.add(trr.col);
        } else {
          amb += ShadowTrace(objectList, p, hdir, 1000.f);
        }
      }
      if (!isUseBounce()) {
        assert (amb >= 0.);
        amb *= 1. / numSubSamples;
        assert (numSubSamples > 0.);
        assert (amb >= 0.);
        scol.add(diff.x * amb * g_SkyColor.x, diff.y * amb
            * g_SkyColor.y, diff.z * amb * g_SkyColor.z);
      }
    }
    return scol;
  }
  
  float ShadowTrace(Obj objectList[], PVector ro, PVector rd, float maxT) {
    float t = maxT;
    for (int i = 0; i < objectList.length; i++) {
      IntersectRes ht = objectList[i].getPrim().Intersect(ro, rd, t, true);
      if (ht.t < t && ht.t >= 0) {
        return 0.f;
      }
    }
    return 1.f;
  }

  float Fresnel(PVector nrm, PVector I) {
    float f = 1.f - Math.abs(nrm.dot(I));
    float f2 = f * f;
    f2 = f2 * f2 * f;
    return 0.05f + 0.95f * f2;
  }

  PVector Reflect(PVector nrm, PVector I) {
    float f = -2.f * nrm.dot(I);
    PVector r = new PVector();
    r.set(nrm);
    r.mult(f);
    r.add(I);
    return r;
  }

  public TracePoint trace(Obj objectList[], PVector ro, PVector rd, QMCSamples sampler, int sc, int depth, int numSamples, boolean g_ShowLightingOnly) {
    Obj hitRes = null;
    float t = 100000.f;
    TracePoint tr = new TracePoint();
    tr.w = 1.f;
    Primitive rhobj = null;
    for (int i = 0; i < objectList.length; i++) {
      IntersectRes ht = objectList[i].getPrim().Intersect(ro, rd, t, false);
      if (ht.t < t && ht.t > 0.) {
        hitRes = objectList[i];
        rhobj = ht.hobj;
        t = ht.t;
      }
    }
    if (hitRes != null) {
      PVector hp = new PVector();
      hp.set(rd);
      hp.mult(t);
      hp.add(ro);
      PVector nrm = rhobj.GetNormal(hp);
      PVector off = new PVector();
      off.set(nrm);
      off.mult(0.00001f);
      hp.add(off);

      // cache four hit points??
      PVector diff;
      if (!g_ShowLightingOnly) {
        if (depth > 0)
          diff = hitRes.getShader().ApproxSample(rhobj.GetModelSpace(hp),
              t * 0.002f);
        else
          diff = hitRes.getShader().Sample(rhobj.GetModelSpace(hp),
              t * 0.002f);
      } else {
        diff = new PVector(0.7f, 0.7f, 0.7f);
      }

      boolean useReflect = hitRes.getReflect() > 0 && (depth == 0)
          && isUseReflect(); // fully
      // reflective

      float bl = Fresnel(nrm, rd) * hitRes.getReflect();
      float calcRefAmt = hitRes.getMetallic() > 0.f ? hitRes.getMetallic() : bl;
      calcRefAmt *= useReflect ? 1. : 0.;

      int numSamplesRef = (int) (Math.ceil(numSamples * calcRefAmt));
      int numSamplesDir = numSamples - numSamplesRef;

      tr.col = calculateLight(objectList, hp, nrm, rd, diff, sampler, sc, numSamplesDir, depth, g_ShowLightingOnly);
      if (useReflect && numSamplesRef > 0) // fully reflective
      {
        if (bl > 0.01 || hitRes.getMetallic() > 0.f) {
          PVector rnrm = Reflect(nrm, rd);
          TracePoint trr = trace(objectList, hp, rnrm, sampler, sc, depth + 1, numSamplesRef, g_ShowLightingOnly);

          if (hitRes.getMetallic() > 0.f) {
            trr.col.mult(diff.x);
            bl = hitRes.getMetallic();
          }
          tr.col = Shader.lerp(tr.col, trr.col, bl);
        }
      }
      return tr;
    }
    tr.col = new PVector();
    tr.col.set(g_SkyColor); // sky;
    return tr;
  }  
  

  public float pow16(float v) {
    v = v * v;
    v = v * v;
    v = v * v;
    v = v * v;
    v = v * v;
    return v * v;
  }
  
};
