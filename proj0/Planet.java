/** all methods should be non-static. */
/** all instance variables and methods
  * will be declared using the public keyword. */

public class Planet {
    public double xxPos;
    public double yyPos;
    public double xxVel;
    public double yyVel;
    public double mass;
    public String imgFileName;
    static final double G = 6.67e-11;

    public Planet(double xP, double yP, double xV,
                  double yV, double m, String img) {
                      xxPos = xP;
                      yyPos = yP;
                      xxVel = xV;
                      yyVel = yV;
                      mass = m;
                      imgFileName = img;
                  }

    /** Initialize an identical Planet object (i.e. a copy). */
    public Planet(Planet p) {
        xxPos = p.xxPos;
        yyPos = p.yyPos;
        xxVel = p.xxVel;
        yyVel = p.yyVel;
        mass = p.mass;
        imgFileName = p.imgFileName;
    }

    /** Calculates the distance between two Planets. */
    public double calcDistance(Planet p) {
        double dx = p.xxPos - this.xxPos;
        double dy = p.yyPos - this.yyPos;
        double r = Math.sqrt(dx*dx + dy*dy);
        return r;
    }

    /** Returns a double describing the force
      * exerted on this planet by the given planet. */
    public double calcForceExertedBy(Planet p) {
        double r = this.calcDistance(p);
        double m1 = this.mass;
        double m2 = p.mass;
        double F = (G * m1 * m2) / (r*r);
        return F;
    }

    /** Describe the force exerted in the X and Y direction */
    public double calcForceExertedByX(Planet p) {
        double F = this.calcForceExertedBy(p);
        double dx = p.xxPos - this.xxPos;
        double r = this.calcDistance(p);
        double Fx = F * dx / r;
        return Fx;
    }

    public double calcForceExertedByY(Planet p) {
        double F = this.calcForceExertedBy(p);
        double dy = p.yyPos - this.yyPos;
        double r = this.calcDistance(p);
        double Fy = F * dy / r;
        return Fy;
    }

    /** Take in an array of Planets and calculate the net X and net Y force
      * exerted by all planets in that array upon the current Planet. */
    public double calcNetForceExertedByX(Planet[] allP) {
        double Fx;
        double FNetx = 0;
        for (Planet p : allP) {
            if (this.equals(p)) {
                continue;
            }
            Fx = this.calcForceExertedByX(p);
            FNetx += Fx;
        }
        return FNetx;
    }

    public double calcNetForceExertedByY(Planet[] allP) {
        double Fy;
        double FNety = 0;
        for (Planet p : allP) {
            if (this.equals(p)) {
                continue;
            }
            Fy = this.calcForceExertedByY(p);
            FNety += Fy;
        }
        return FNety;
    }

    /** Determines how much the forces exerted on the planet will cause
      * that planet to accelerate, and the resulting change in the planet’s
      * velocity and position in a small period of time dt. */
    public void update(double dt, double FNetx, double FNety) {
        double aNetx = FNetx / this.mass;
        double aNety = FNety / this.mass;
        this.xxVel = this.xxVel + dt * aNetx;
        this.yyVel = this.yyVel + dt * aNety;
        this.xxPos = this.xxPos + dt * this.xxVel;
        this.yyPos = this.yyPos + dt * this.yyVel;
    }
}