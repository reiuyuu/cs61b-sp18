public class NBody {
  /** Given a file name, it should return a double
    * corresponding to the radius of the universe in that file */
  public static double readRadius(String filename) {
    In in = new In(filename);
    int N = in.readInt();
    double radius = in.readDouble();

    return radius;
  }

  /** Given a file name, it should return an array of Planets
    * corresponding to the planets in the file */  
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

      allPlanet[i] = new Planet(xxPos, yyPos, xxVel, yyPos, mass, imgFileName);
    }

    return allPlanet;
  }
}