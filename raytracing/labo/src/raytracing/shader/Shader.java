package raytracing.shader;

import raytracing.Tracer;
import raytracing.geometrie.PVector;

public class Shader {

  final static int MAXOCTAVES = 8;

  public static PVector lerp(PVector a, PVector b, float t) {
    return new PVector(Sampling.lerp(a.x, b.x, t), Sampling.lerp(a.y, b.y, t), Sampling.lerp(a.z, b.z, t));
  }

  public static float smoothstep(float edge0, float edge1, float x) {
    x = Math.min(Math.max((x - edge0) / (edge1 - edge0), 0.0f), 1.0f);
    return x * x * (3 - 2 * x);
  }

  public static float fmod(float x, float y) {
    x = x / y;
    x = (float) (x - Math.floor(x));
    return x * y;
  }

  public static PVector HexTile(float s, float t, float pwidth) {
    PVector tilecolor = new PVector(.55f, 0.f, 0.f);
    PVector mortarcolor = new PVector(.5f, .5f, .5f);
    float tileradius = 0.2f;
    float mortarwidth = 0.02f;
    float tilevary = 0.15f;
    float scuffing = 0.5f;
    float stains = 0.4f;
    float stainfrequency = 2;
    float scufffrequency = 4;
    PVector scuffcolor = new PVector(.05f, .05f, .05f);

    /* Determine how wide in s-t space one pixel projects to */

    float tilewidth = tileradius * 1.7320508f; /* sqrt(3) */
    float tt = fmod(t, 1.5f * tileradius);
    float ttile = (float) Math.floor(t / (1.5f * tileradius));

    float ss;
    if (fmod(ttile / 2, 1.f) == 0.5f)
      ss = s + tilewidth / 2;
    else
      ss = s;
    float stile = (float) Math.floor(ss / tilewidth);
    ss = fmod(ss, tilewidth);
    float mortar = 0;
    float mw2 = mortarwidth / 2;
    if (tt < tileradius) {
      mortar = 1 - (smoothstep(mw2, mw2 + pwidth, ss)
          * (1 - smoothstep(tilewidth - mw2 - pwidth, tilewidth - mw2, ss)));
    } else {
      float x = tilewidth / 2 - Math.abs(ss - tilewidth / 2);
      float y = 1.7320508f * (tt - tileradius);
      if (y > x) {
        if (fmod(ttile / 2, 1.f) == 0.5)
          stile -= 1;
        ttile += 1;
        if (ss > tilewidth / 2)
          stile += 1;
      }
      mortar = (smoothstep(x - 1.73f * mw2 - pwidth, x - 1.73f * mw2, y)
          * (1 - smoothstep(x + 1.73f * mw2, x + 1.73f * mw2 + pwidth, y)));
    }

    float tileindex = stile + 41 * ttile;
    PVector Ctile = new PVector();
    Ctile.set(tilecolor);
    Ctile.mult(1 + tilevary * (Tracer.getInstance().noise(tileindex + 0.5f) * 2.f - 1.f));

    float stain = stains * smoothstep(.5f, 1.f, Tracer.getInstance().noise(s * stainfrequency, t * stainfrequency));
    float scuff = scuffing * smoothstep(.6f, 1.f,
        Tracer.getInstance().noise(t * scufffrequency - 90.26f, s * scufffrequency + 123.82f));

    PVector Ct;
    Ct = lerp(lerp(Ctile, scuffcolor, scuff), mortarcolor, mortar);
    Ct.mult(1.f - stain);
    // Ct.w = 1. - mortar;//(1-scuff/2)0.2f* (1-scuff/2);
    return Ct;
  }

  public static float snoise(float x) {
    return 2.f * Tracer.getInstance().noise((x)) - 1.f;
  }

  public static float boxstep(float a, float b, float x) {
    return Sampling.limit((x - a) / (b - a), 0.f, 1.f);
  }

  public static final float MINFILTERWIDTH = 1.0e-7f;

  public static PVector LGParquetPlank(float s, float t, float pwidth) {
    final float ringscale = 15, grainscale = 60;
    float txtscale = 1;
    final float plankspertile = 4;
    final float inv_plankspertile = 1.f / plankspertile;
    PVector lightwood = new PVector(0.57f, 0.292f, 0.125f);
    PVector darkwood = new PVector(0.275f, 0.15f, 0.06f);
    PVector groovecolor = new PVector(.05f, .04f, .015f);
    float plankwidth = .05f, groovewidth = 0.001f;
    float plankvary = 0.8f;
    float grainy = 1, wavy = 0.08f;

    float r, r2;
    float w, h, fade, ttt;
    PVector woodcolor;

    final float PGWIDTH = plankwidth + groovewidth;
    final float INV_PGWIDTH = 1.f / PGWIDTH;
    final float planklength = PGWIDTH * plankspertile - groovewidth;
    final float PGHEIGHT = planklength + groovewidth;
    final float INV_PGHEIGHT = 1.f / PGHEIGHT;
    final float GWF = groovewidth * 0.5f / PGWIDTH;
    float GHF = groovewidth * 0.5f / PGHEIGHT;

    // Determine how wide in s-t space one pixel projects to
    float swidth = (pwidth * INV_PGWIDTH) * txtscale;
    float twidth = (pwidth * INV_PGHEIGHT) * txtscale;
    float fwidth = Math.max(swidth, twidth);

    float ss = (txtscale * s) * INV_PGWIDTH;
    float whichrow = (float) Math.floor(ss);
    float tt = (txtscale * t) * INV_PGHEIGHT;
    float whichplank = (float) Math.floor(tt);

    if (fmod(whichrow * inv_plankspertile + whichplank, 2) >= 1.) {
      ss = txtscale * t * INV_PGWIDTH;
      whichrow = (float) Math.floor(ss);
      tt = txtscale * s * INV_PGHEIGHT;
      whichplank = (float) Math.floor(tt);
      float tmp = swidth;
      swidth = twidth;
      twidth = tmp;
    }
    ss -= whichrow;
    tt -= whichplank;

    whichplank += 20 * (whichrow + 10);

    //
    // Figure out where the grooves are. The value groovy is 0 where there
    // are grooves, 1 where the wood grain is visible. Do some simple
    // antialiasing.
    //
    if (swidth >= 1.)
      w = 1 - 2 * GWF;
    else
      w = Sampling.limit(boxstep(GWF - swidth, GWF, ss), Math.max(1 - GWF / swidth, 0), 1f)
          - Sampling.limit(boxstep(1 - GWF - swidth, 1 - GWF, ss), 0, 2 * GWF / swidth);
    if (twidth >= 1)
      h = 1 - 2 * GHF;
    else
      h = Sampling.limit(boxstep(GHF - twidth, GHF, tt), Math.max(1 - GHF / twidth, 0), 1)
          - Sampling.limit(boxstep(1 - GHF - twidth, 1 - GHF, tt), 0, 2 * GHF / twidth);

    // This would be the non-antialiased version:
    // w = step (GWF,ss) - step(1-GWF,ss);
    // h = step (GHF,tt) - step(1-GHF,tt);
    //
    float groovy = w * h;

    //
    // Add the ring patterns
    //
    fade = smoothstep(1 / ringscale, 8 / ringscale, fwidth);
    if (fade < 0.999) {
      ttt = (float) (tt * 1. / 4 + whichplank * 1. / 28.38
          + wavy * Tracer.getInstance().noise(8.f * ss, tt * 1.f / 4));
      r = ringscale * Tracer.getInstance().noise(ss - whichplank, ttt);
      r -= Math.floor(r);
      r = (float) (0.3 + 0.7 * smoothstep(0.2f, 0.55f, r) * (1 - smoothstep(0.75f, 0.8f, r)));
      r = (float) ((1 - fade) * r + 0.65 * fade);
      //
      // Multiply the ring pattern by the fine grain
      //
      fade = smoothstep(2 / grainscale, 8 / grainscale, fwidth);
      if (fade < 0.999) {
        r2 = (float) (1.3 - Tracer.getInstance().noise(ss * grainscale, (tt * grainscale / 4)));
        r2 = grainy * r2 * r2 + (1 - grainy);
        r *= (1 - fade) * r2 + (0.75 * fade);
      } else
        r *= 0.75;
    } else
      r = 0.4875f;

    // Mix the light and dark wood according to the grain pattern
    woodcolor = lerp(lightwood, darkwood, r);

    // Add plank-to-plank variation in overall color
    woodcolor.mult(1 - plankvary / 2 + plankvary * Tracer.getInstance().noise(whichplank + 0.5f));

    return lerp(groovecolor, woodcolor, groovy);
  }

  public static PVector wood2(PVector PP, float pswidth) {
    float ringscale = 15f;
    PVector lightwood = new PVector(0.69f, 0.44f, 0.25f);
    PVector darkwood = new PVector(0.35f, 0.22f, 0.08f);

    PP.x += Tracer.getInstance().noise(PP.x + 125.32f, PP.y + 13.54f, PP.z);
    PP.y += Tracer.getInstance().noise(PP.z + 125.32f, PP.x + 13.54f, PP.y);
    PP.z += Tracer.getInstance().noise(PP.y + 125.32f, PP.z + 13.54f, PP.x);

    /* compute radial distance r from PP to axis of "tree" */
    float y = PP.y;
    float z = PP.z;
    float r = (float) Math.sqrt(y * y + z * z);

    /* map radial distance r nto ring position [0, 1] */
    r *= ringscale;
    r += Math.abs(Tracer.getInstance().noise(r));
    r -= Math.floor(r); /* == mod (r, 1) */

    /* use ring poisition r to select wood color */
    r = smoothstep(0, 0.8f, r) - smoothstep(0.83f, 1.0f, r);
    return lerp(lightwood, darkwood, r);

  }

}
