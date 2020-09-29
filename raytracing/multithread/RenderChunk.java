package raytracing.multithread;

import processing.core.PApplet;
import raytracing.*;
import raytracing.camera.*;
import raytracing.objet.*;
import raytracing.shader.*;

public class RenderChunk implements Runnable {
  int c;
  int width;
  int height;
  int numFrameLess;
  Scene g_Scene;
  Tracer tracer;

  public RenderChunk(Tracer _tracer, int _c, Scene _g_Scene, int _width, int _height, int _numFrameLess) {
    c = _c;
    width = _width;
    height = _height;
    numFrameLess = _numFrameLess;
    g_Scene = _g_Scene;
    tracer = _tracer;
  }

  public void run() {
    renderPixels(g_Scene.getObjets(), width * height / numFrameLess, c, 1);
  }
  
  public void renderPixels(Obj objectList[], int numSamples, int sampleCount, int numThreads) {
    for (int i = 0; i < numSamples; i++) {
      int samp = (i + numSamples * sampleCount);
      int sampCount = samp % (width * height);
      float x = Sampling.Sobol(samp, 0, 0);
      float y = Sampling.Sobol(samp, 1, 0);

      int ix = (int) (tracer.floor(x * (float) width));
      int iy = height - (int) (tracer.floor(y * (float) height)) - 1;
      int idx = iy * width + ix;

      if (iy < 64 || iy > height - 64)
        continue;
      int sc = (int) tracer.frameWieght[idx];
      int hashA = sampCount | (sampCount << 16);
      int hashB = 0x9e3779b9;
      int hashC = 0x9e3779b9;

      int randX = hash_rand(hashA, hashB, hashC);
      int randY = hash_rand(randX, hashA, hashC);

      QMCSamples sampler = new QMCSamples(randX, randY);
      float mb = sampler.Getf(sc, 0);
      // shutter is open half the frame
      mb = 1.f - mb * .5f;
      Ray ray2 = tracer.g_cam.generateRay(x * (float) width, y * (float) height);
      Ray ray = tracer.g_prevCam.generateRay(x * (float) width, y
          * (float) height);
      ray.Lerp(ray2, mb);

      // more samples in the centre of the screen
      float u = x * 2.f - 1.f;
      float v = y * 2.f - 1.f;
      float vignette = (u * u + v * v);
      vignette = PApplet.constrain(1.f - vignette * 0.5f, 0.f, 1.f);

      TracePoint tr = g_Scene.trace(objectList, ray.origin, ray.direction, sampler, sc, 0, (int) (4. + vignette * 12.), tracer.g_ShowLightingOnly);
      tr.col.mult(vignette); // apply vignetteing

      tracer.AddColor(tr.col, ix, iy);
    }
  }
  
  /* Bob Jenkins invented this great, reasonably fast, very nice has function. Check out his web page: http://ourworld.compuserve.com/homepages/bob_jenkins/blockcip.htm */
  int hash_rand(int a, int b, int c) {
    a -= b;
    a -= c;
    a ^= (c >> 13);
    b -= c;
    b -= a;
    b ^= (a << 8);
    c -= a;
    c -= b;
    c ^= (b >> 13);
    a -= b;
    a -= c;
    a ^= (c >> 12);
    b -= c;
    b -= a;
    b ^= (a << 16);
    c -= a;
    c -= b;
    c ^= (b >> 5);
    a -= b;
    a -= c;
    a ^= (c >> 3);
    b -= c;
    b -= a;
    b ^= (a << 10);
    c -= a;
    c -= b;
    c ^= (b >> 15);
    return c;
  }
  
};
