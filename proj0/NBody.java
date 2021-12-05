public class NBody {
  /**
   * Given a file name, it should return a double
   * corresponding to the radius of the universe in that file
   */
  public static double readRadius(String filename) {
    In in = new In(filename);
    int N = in.readInt();
    double radius = in.readDouble();

    return radius;
  }

  /**
   * Given a file name, it should return an array of Planets
   * corresponding to the planets in the file
   */
  public static Planet[] readPlanets(String filename) {
    In in = new In(filename);
    int N = in.readInt();
    double radius = in.readDouble();
    Planet[] allPlanet = new Planet[N];

    for (int i = 0; i < N; i++) {
      double xxPos = in.readDouble();
      double yyPos = in.readDouble();
      double xxVel = in.readDouble();
      double yyVel = in.readDouble();
      double mass = in.readDouble();
      String imgFileName = in.readString();

      allPlanet[i] = new Planet(xxPos, yyPos, xxVel, yyVel, mass, imgFileName);
    }

    return allPlanet;
  }

  public static void main(String[] args) {
    /** Collecting All Needed Input */
    double T = Double.parseDouble(args[0]);
    double dt = Double.parseDouble(args[1]);
    String filename = args[2];
    Planet[] Planets = readPlanets(filename);
    double radius = readRadius(filename);

    /** Set the scale so that it matches the radius of the universe. */
    StdDraw.setScale(0 - radius, radius);

    /** Drawing the Background */
    // Why (0, 0)
    StdDraw.picture(0, 0, "images/starfield.jpg");

    /** Drawing All of the Planets */
    for (int i = 0; i < Planets.length; i++) {
      Planets[i].draw();
    }
  }
}