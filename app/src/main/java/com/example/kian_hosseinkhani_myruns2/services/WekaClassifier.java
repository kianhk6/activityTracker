package com.example.kian_hosseinkhani_myruns2.services;

class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        double p = Double.NaN;
        p = WekaClassifier.N4b5c7f840(i);
        return p;
    }

    static double N4b5c7f840(Object[] i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() <= 13.390311) {
            p = 0;
        } else if (((Double) i[0]).doubleValue() > 13.390311) {
            p = WekaClassifier.N51ad9cec1(i);
        }
        return p;
    }

    static double N51ad9cec1(Object[] i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 1;
        } else if (((Double) i[64]).doubleValue() <= 14.534508) {
            p = WekaClassifier.N111f1f102(i);
        } else if (((Double) i[64]).doubleValue() > 14.534508) {
            p = 2;
        }
        return p;
    }

    static double N111f1f102(Object[] i) {
        double p = Double.NaN;
        if (i[4] == null) {
            p = 1;
        } else if (((Double) i[4]).doubleValue() <= 14.034383) {
            p = WekaClassifier.N3fd5abd83(i);
        } else if (((Double) i[4]).doubleValue() > 14.034383) {
            p = 1;
        }
        return p;
    }

    static double N3fd5abd83(Object[] i) {
        double p = Double.NaN;
        if (i[7] == null) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() <= 4.804712) {
            p = 1;
        } else if (((Double) i[7]).doubleValue() > 4.804712) {
            p = 2;
        }
        return p;
    }
}
