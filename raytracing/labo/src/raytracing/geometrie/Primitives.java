package raytracing.geometrie;


public class Primitives {

  public static float intersectBox(PVector r_o, PVector invr_d, PVector boxmin, PVector boxmax) {
    // compute intersection of ray with all six bbox planes
    PVector invR = invr_d;
    PVector tbot = new PVector();
    tbot.set(boxmin);
    tbot.sub(r_o);
    tbot = new PVector(invR.x * tbot.x, invR.y * tbot.y, invR.z * tbot.z);

    PVector ttop = new PVector();
    ttop.set(boxmax);
    ttop.sub(r_o);
    ttop = new PVector(invR.x * ttop.x, invR.y * ttop.y, invR.z * ttop.z);
    // re-order intersections to find smallest and largest on each axis

    PVector tmin = new PVector();
    tmin.x = Math.min(ttop.x, tbot.x);
    tmin.y = Math.min(ttop.y, tbot.y);
    tmin.z = Math.min(ttop.z, tbot.z);
    PVector tmax = new PVector();
    tmax.x = Math.max(ttop.x, tbot.x);
    tmax.y = Math.max(ttop.y, tbot.y);
    tmax.z = Math.max(ttop.z, tbot.z);

    // find the largest tmin and the smallest tmax
    float largest_tmin = Math.max(Math.max(tmin.x, tmin.y), Math.max(tmin.x, tmin.z));
    float smallest_tmax = Math.min(Math.min(tmax.x, tmax.y), Math.min(tmax.x, tmax.z));

    if (smallest_tmax > largest_tmin)
      return largest_tmin;
    return 100000.0f;
  }

}
