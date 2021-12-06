public class NBody {
  /**
   * Given a file name, it should return a double
   * corresponding to the radius of the universe in that file.
   */
  public static double readRadius(String filename) {
    In in = new In(filename);
    int N = in.readInt();
    double radius = in.readDouble();

    return radius;
  }

  /**
   * Given a file name, it should return an array of Planets
   * corresponding to the planets in the file.
   */
  public static Planet[] readPlanets(String filename) {
    In in = new In(filename);
    int N = in.readInt();
    double radius = in.readDouble();
    Planet[] allP = new Planet[N];

    for (int i = 0; i < N; i++) {
      double xxPos = in.readDouble();
      double yyPos = in.readDouble();
      double xxVel = in.readDouble();
      double yyVel = in.readDouble();
      double mass = in.readDouble();
      String imgFileName = in.readString();

      allP[i] = new Planet(xxPos, yyPos, xxVel, yyVel, mass, imgFileName);
    }

    return allP;
  }

  public static void main(String[] args) {
    /** Drawing the Initial Universe State */
    /** Collecting All Needed Input */
    double T = Double.parseDouble(args[0]);
    double dt = Double.parseDouble(args[1]);
    String filename = args[2];
    Planet[] allPlanets = readPlanets(filename);
    double radius = readRadius(filename);
    int N = allPlanets.length;

    /** Drawing the Background */
    StdDraw.setScale(0 - radius, radius);
    // Why (0, 0)
    StdDraw.picture(0, 0, "images/starfield.jpg");

    /** Drawing All of the Planets */
    for (Planet p : allPlanets) {
      p.draw();
    }

    /** Creating an Animation */
    /** Enable double buffering. */
    StdDraw.enableDoubleBuffering();

    /** Loop until time variable is T */
    double time = 0;
    while (time <= T) {
      /**
       * Calculate the net x and y forces for each planet, storing these in the
       * xForces and yForces arrays respectively.
       */
      double[] xForces = new double[N];
      double[] yForces = new double[N];
      for (int i = 0; i < N; i++) {
        xForces[i] = allPlanets[i].calcNetForceExertedByX(allPlanets);
        yForces[i] = allPlanets[i].calcNetForceExertedByY(allPlanets);
      }

      /** Update each planet’s position, velocity, and acceleration. */
      for (int i = 0; i < N; i++) {
        allPlanets[i].update(dt, xForces[i], yForces[i]);
      }

      /** Draw the background image. */
      StdDraw.picture(0, 0, "images/starfield.jpg");

      /** Draw all of the planets. */
      for (Planet p : allPlanets) {
        p.draw();
      }

      /** Show the offscreen buffer. */
      StdDraw.show();

      /** Pause the animation for 10 milliseconds. */
      StdDraw.pause(10);

      /** Increase time variable by dt. */
      time += dt;
    }

    /** Printing the Universe */
    StdOut.printf("%d\n", N);
    StdOut.printf("%.2e\n", radius);
    for (Planet p : allPlanets) {
      StdOut.printf("%11.4e %11.4e %11.4e %11.4e %11.4e %12s\n",
          p.xxPos, p.yyPos, p.xxVel, p.yyVel, p.mass, p.imgFileName);
    }
  }
}