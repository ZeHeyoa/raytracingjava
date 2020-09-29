package raytracing.camera;

import processing.core.PVector;

public class PolarCamera {
  PVector m_camPos = new PVector(1.3f, 2.7f, -3.0f);
  float m_CamAngleX = -0.4f;
  float m_CamAngleY = -0.5f;
  PVector m_camDir;

  public PVector getPositionCamera() {
    return m_camPos;
  }

  public void setPositionCamera(PVector m_camPos) {
    this.m_camPos = m_camPos;
  }

  public PVector getDirectionCamera() {
    return m_camDir;
  }

  public void setDirectionCamera(PVector m_camDir) {
    this.m_camDir = m_camDir;
  }

  PolarCamera() {
    update(0, 0);
  }

  String Settings() {
    return "new PolarCamera( new PVector(" + m_camPos.x + ", " + m_camPos.y + ", " + m_camPos.z + "), "
        + m_CamAngleX + ", " + m_CamAngleY + ") ";
  }

  public PolarCamera(PVector cpos, float cax, float cay) {
    m_camPos = cpos;
    m_CamAngleX = cax;
    m_CamAngleY = cay;
    update(0, 0);
  }

  public void update(float dy, float dz) {
    m_CamAngleX += dy;
    m_CamAngleY += -dz;
    m_camDir = new PVector((float) (Math.sin(m_CamAngleX) * Math.cos(m_CamAngleY)), (float) (Math.sin(m_CamAngleY)),
        (float) (Math.cos(m_CamAngleX) * Math.cos(m_CamAngleY)));
  }

  public void forward(float v) {
    PVector f = new PVector();
    f.set(m_camDir);
    f.mult(v * 0.25f);
    m_camPos.add(f);
  }

  public void rotate(float ang) {
    m_camDir.x = (float) Math.sin(ang);
    m_camDir.z = (float) Math.cos(ang);
    m_camDir.y = (float) (-0.2f - 0.5 * ang / Math.PI);
    m_camPos.x = (float) (-m_camDir.x * (2. + 0.5 * ang / Math.PI));
    m_camPos.z = (float) (-m_camDir.z * (2. + 0.5 * ang / Math.PI));
    m_camPos.y = (float) (1.0 + 1.5 * ang / Math.PI);
    m_camDir.normalize();

  }

  public void side(float v) {
    PVector f = m_camDir.cross(new PVector(0, 1, 0));
    f.normalize();
    f.mult(v * 0.25f);
    m_camPos.add(f);
  }
}
