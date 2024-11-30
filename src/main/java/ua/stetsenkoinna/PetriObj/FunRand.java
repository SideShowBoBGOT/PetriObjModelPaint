package ua.stetsenkoinna.PetriObj;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.Random;

public class FunRand {

    public static double exp(double timeMean) {
        double a = 0;
        while (a == 0) {
            a = Math.random();
        }
        a = -timeMean * Math.log(a);

        return a;
    }

    public static double unif(double timeMin, double timeMax) throws ExceptionInvalidTimeDelay {
        double a = 0;
        while (a == 0) {
            a = Math.random();
        }
        a = timeMin + a * (timeMax - timeMin);
        if (a<0)
            throw new ExceptionInvalidTimeDelay("Negative time delay is generatated: Check parameters for time delay.");
        return a;
    }

    public static double norm(double timeMean, double timeDeviation) throws ExceptionInvalidTimeDelay {
        double a;
        Random r = new Random();
        a = timeMean + timeDeviation * r.nextGaussian();
        if (a<0)
            throw new ExceptionInvalidTimeDelay("Negative time delay is generatated: Check parameters for time delay.");
        return a;
    }

    public static double poisson(final double timeMean) {
        PoissonDistribution poisson = new PoissonDistribution(timeMean);
        return poisson.sample();
    }
}
