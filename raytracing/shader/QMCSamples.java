package raytracing.shader;

public class QMCSamples {
  int ranX;
  int ranY;

  public QMCSamples(int rx, int ry) {
    ranX = rx;
    ranY = ry;
  }

  public float Getf(int sc, int D) {
    return Sampling.Sobol(sc, D, ranX);
  }

  public float[] Get(int sc, int D) {
    float samples[] = new float[2];
    samples[0] = Sampling.Sobol(sc, D * 2, ranX);
    samples[1] = Sampling.Sobol(sc, D * 2 + 1, ranY);
    return samples;
  }
}
