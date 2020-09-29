package raytracing;

import processing.core.*;
import raytracing.camera.*;
import raytracing.geometrie.IntersectRes;
import raytracing.multithread.*;
import raytracing.objet.*;
import raytracing.shader.*;

public class Tracer extends PApplet {
  private static Tracer instance = new Tracer();

  public static Tracer getInstance() {
    return instance;
  }

  WorkQueue g_renderQueue;

  public void settings() {
    size(200, 200);
  }

  public void setup() {
    g_renderQueue = new WorkQueue(8);
    rendered = createImage(width, height, RGB);
    frameColor = new PVector[width * height];
    for (int i = 0; i < width * height; i++)
      frameColor[i] = new PVector();
    frameWieght = new float[width * height];

    g_Reset = true;
    frameRate(60);
  }

  public void draw() {
    if (rendered == null)
      rendered = createImage(width, height, RGB);
    background(rendered);
    if (rotationAngle > 0.) {
      if (!g_renderQueue.IsFinished() && g_SaveAnim) {
        return;
      }
      if (rotationAngle > 2. * PI) {
        rotationAngle = 0.f;
        g_Drag = false;
      } else {
        rotationAngle += PI / (128);
        g_Scene.getPolarCamera().rotate(rotationAngle);
        if (rotateAnim == -1)
          updateCamera();
        if (g_SaveAnim)
          g_Reset = true;
        else
          g_Drag = true;
      }
      if (rotateAnim >= 0 && g_SaveAnim)
        save("frame" + rotateAnim + ".tga");
      rotateAnim++;
    }

    updateCamera();

    if (g_Reset) {
      g_renderQueue.clear();
    }
    if (g_Reset || g_Drag) {
      g_sampleCount = 0;
      for (int i = 0; i < width * height; i++) {
        frameColor[i].set(0.f, 0.f, 0.f);
        frameWieght[i] = 0.f;
      }
      if (!g_renderQueue.IsFinished())
        return;
      g_StartTime = GetTime();

      g_Reset = false;
    }
    if (g_StartTime < 0) {
      fill(~0);
      text("Time  " + (-g_StartTime), 10, 40);
    }
    if (g_renderQueue.IsFinished()) {
      println("Update " + g_sampleCount);
      if (g_sampleCount == numFrameLess * 4)
        g_StartTime = -(GetTime() - g_StartTime);

      for (int i = 0; i < numFrameLess * 4; i++)
        g_renderQueue.execute(new RenderChunk(this, g_sampleCount++, g_Scene, width, height, numFrameLess));
    }

  }

  public static void main(String args[]) {
    String[] a = { "Tracer" };
    PApplet.runSketch(a, new Tracer());
  }
 
  synchronized int doColor(float x, float y, float z) {
    return color(x, y, z);
  }

  public synchronized void AddColor(PVector col, int ix, int iy) {
    PVector c = new PVector();

    c.set(col);
    int idx = ix + iy * width;
    frameColor[idx].add(c);
    frameWieght[idx] += 1.;

    c.set(frameColor[idx]);
    float w = 1.f / frameWieght[idx];
    c.x = sqrt(c.x * w);
    c.y = sqrt(c.y * w);
    c.z = sqrt(c.z * w);
    rendered.pixels[idx] = doColor(c.x * 255.f, c.y * 255.f, c.z * 255.f);
    if (frameWieght[idx] == 1.) {
      // now fill any empty ones
      int sx = max(ix - 1, 0);
      int sy = max(iy - 1, 0);
      int ex = min(ix + 1, width);
      int ey = min(iy + 1, height);
      for (int y = sy; y < ey; y++)
        for (int x = sx; x < ex; x++) {
          idx = x + y * width;
          if (frameWieght[idx] == 0.)
            rendered.pixels[idx] = doColor(c.x * 255.f,
                c.y * 255.f, c.z * 255.f);
        }
    }
  }

  PImage rendered = null;

  public Camera g_cam = null;
  public Camera g_prevCam = null;

  Obj Obj_Materials[] = new Obj[] { new Obj(new Box(new PVector(-.5f, -.01f, -5f), new PVector(2.0f, 0.01f, 2f)), 0.1f, 0.f, new ParquetPlank()) };

  Scene g_Scenes[] = new Scene[] { new Scene(Obj_Materials, new PolarCamera(new PVector(1.8213837f, 3.0841362f, -3.4671848f), -0.5062503f, -0.5520832f), false, true, false, true) };

  int g_CurrentScene = 0;
  volatile Scene g_Scene = g_Scenes[0];

  PVector[] frameColor;
  public float[] frameWieght;

  volatile int g_sampleCount = 0;
  final int numFrameLess = 32;
  boolean g_Reset = true;
  boolean g_Drag = false;
  float rotationAngle = 0.f;
  int rotateAnim = 0;
  boolean g_SaveAnim = false;
  public boolean g_ShowLightingOnly = false;

  public void mouseDragged() {
    float dx = (float) (mouseX - pmouseX) / width;
    float dy = (float) (mouseY - pmouseY) / height;
    if (keyPressed && keyCode == SHIFT) {
      g_Scene.getPolarCamera().forward(dy * 2);
      g_Scene.getPolarCamera().side(dx * 2);
    } else
      g_Scene.getPolarCamera().update(dx, dy);
    g_Drag = true;
  }

  public void mouseReleased() {
    g_Reset = true;
    g_Drag = false;
  }

  public void keyPressed() {
    if (key == 'h' || key == 'H')
      g_Scene.setUseHemi(!g_Scene.isUseHemi());
    if (key == 'd' || key == 'D')
      g_Scene.setUseDirect(!g_Scene.isUseDirect());
    if (key == 'r' || key == 'R')
      g_Scene.setUseReflect(!g_Scene.isUseReflect());
    if (key == 'b' || key == 'B')
      g_Scene.setUseBounce(!g_Scene.isUseBounce());
    if (key == 'l' || key == 'L')
      g_ShowLightingOnly = !g_ShowLightingOnly;

    if (key == 'x' || key == 'X')
      g_SaveAnim = !g_SaveAnim;
    if (key == 'a' || key == 'A') {
      if (rotationAngle == 0.) {

        rotationAngle = 0.00001f;
        rotateAnim = -1;
      } else
        rotationAngle = 0.0f;
    }

    if (key == 's' || key == 'S')
      g_Scene.Dump();

    if (key == '+' || key == '=') {
      g_CurrentScene++;
      g_Scene = g_Scenes[g_CurrentScene % g_Scenes.length];
    } else if (key == '-' || key == '_') {
      g_CurrentScene--;
      g_Scene = g_Scenes[g_CurrentScene % g_Scenes.length];
    }
    g_Reset = true;
  }

  float g_StartTime;

  float GetTime() {
    return (float) millis() / 1000.f;
  }

  int g_drawCount = 0;

  void updateCamera() {
    g_prevCam = g_cam;
    g_cam = new Camera(g_Scene.getCamera().getPositionCamera(), new PVector(0.0f, 1.0f, 0.0f), g_Scene.getCamera().getDirectionCamera(), width, height, width);
    if (g_prevCam == null)
      g_prevCam = g_cam;
  }

}
