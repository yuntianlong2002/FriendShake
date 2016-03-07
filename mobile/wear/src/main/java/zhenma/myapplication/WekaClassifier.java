package zhenma.myapplication;

/**
 * Created by zhenma on 1/25/16.
 */
public class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N59fa0e8d0(i);
        return p;
    }
    static double N59fa0e8d0(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 172.375339) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 172.375339) {
            p = WekaClassifier.N2fd179211(i);
        }
        return p;
    }
    static double N2fd179211(Object []i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 3;
        } else if (((Double) i[0]).doubleValue() <= 942.913444) {
            p = WekaClassifier.N61677e0d2(i);
        } else if (((Double) i[0]).doubleValue() > 942.913444) {
            p = 2;
        }
        return p;
    }
    static double N61677e0d2(Object []i) {
        double p = Double.NaN;
        if (i[8] == null) {
            p = 1;
        } else if (((Double) i[8]).doubleValue() <= 22.313687) {
            p = WekaClassifier.N5184b9703(i);
        } else if (((Double) i[8]).doubleValue() > 22.313687) {
            p = WekaClassifier.N60aa91df4(i);
        }
        return p;
    }
    static double N5184b9703(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 1;
        } else if (((Double) i[3]).doubleValue() <= 52.649604) {
            p = 1;
        } else if (((Double) i[3]).doubleValue() > 52.649604) {
            p = 3;
        }
        return p;
    }
    static double N60aa91df4(Object []i) {
        double p = Double.NaN;
        if (i[8] == null) {
            p = 2;
        } else if (((Double) i[8]).doubleValue() <= 29.900835) {
            p = 2;
        } else if (((Double) i[8]).doubleValue() > 29.900835) {
            p = 3;
        }
        return p;
    }
}
