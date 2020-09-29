package raytracing.shader;

import processing.core.PVector;

public class  ConstantColor implements SurfaceShader {
	  PVector v;
	  ConstantColor( PVector _v)
	  {
	    v = _v;
	  }
	 public  PVector Sample( PVector PP, float pwidth)
	  {
	    return v;
	  }
	 public    PVector ApproxSample(PVector PP, float pwidth)
	  {
	    return v;
	  }
	}