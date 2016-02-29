package zhenma.myapplication;

/**
 * Created by zhenma on 1/25/16.
 */
public class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N372684e0(i);
        return p;
    }
    static double N372684e0(Object []i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 1;
        } else if (((Double) i[64]).doubleValue() <= 14.37522) {
            p = WekaClassifier.N4c1ddaa61(i);
        } else if (((Double) i[64]).doubleValue() > 14.37522) {
            p = WekaClassifier.N3bfdef542(i);
        }
        return p;
    }
    static double N4c1ddaa61(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 169.172941) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 169.172941) {
            p = 1;
        }
        return p;
    }
    static double N3bfdef542(Object []i) {
        double p = Double.NaN;
        if (i[10] == null) {
            p = 2;
        } else if (((Double) i[10]).doubleValue() <= 82.419987) {
            p = 2;
        } else if (((Double) i[10]).doubleValue() > 82.419987) {
            p = 3;
        }
        return p;
    }
}
