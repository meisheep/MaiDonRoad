package com.github.meisheep.maidonroad;

/**
 * Created by meisheep on 2016/5/20.
 */
class Location {
    double lat;
    double lng;

    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public static Location calcTWD97toWSG84(double x, double y) {
        double lat, lng;

        // ref: http://wangshifuola.blogspot.tw/2010/08/twd97wgs84-wgs84twd97.html
        double a = 6378137.0;
        double b = 6356752.314245;
        double lon0 = 121 * Math.PI / 180;
        double k0 = 0.9999;
        int dx = 250000;
        double dy = 0;
        double e = Math.pow((1- Math.pow(b,2) / Math.pow(a,2)), 0.5);

        x -= dx;
        y -= dy;

        // Calculate the Meridional Arc
        double M = y/k0;

        // Calculate Footprint Latitude
        double mu = M/(a*(1.0 - Math.pow(e, 2)/4.0 - 3*Math.pow(e, 4)/64.0 - 5*Math.pow(e, 6)/256.0));
        double e1 = (1.0 - Math.pow((1.0 - Math.pow(e, 2)), 0.5)) / (1.0 + Math.pow((1.0 - Math.pow(e, 2)), 0.5));

        double J1 = (3*e1/2 - 27*Math.pow(e1, 3)/32.0);
        double J2 = (21*Math.pow(e1, 2)/16 - 55*Math.pow(e1, 4)/32.0);
        double J3 = (151*Math.pow(e1, 3)/96.0);
        double J4 = (1097*Math.pow(e1, 4)/512.0);

        double fp = mu + J1*Math.sin(2*mu) + J2*Math.sin(4*mu) + J3*Math.sin(6*mu) + J4*Math.sin(8*mu);

        // Calculate Latitude and Longitude

        double e2 = Math.pow((e*a/b), 2);
        double C1 = Math.pow(e2*Math.cos(fp), 2);
        double T1 = Math.pow(Math.tan(fp), 2);
        double R1 = a*(1-Math.pow(e, 2))/Math.pow((1-Math.pow(e, 2)*Math.pow(Math.sin(fp), 2)), (3.0/2.0));
        double N1 = a/Math.pow((1-Math.pow(e, 2)*Math.pow(Math.sin(fp), 2)), 0.5);

        double D = x/(N1*k0);

        // calc lat
        double Q1 = N1*Math.tan(fp)/R1;
        double Q2 = (Math.pow(D, 2)/2.0);
        double Q3 = (5 + 3*T1 + 10*C1 - 4*Math.pow(C1, 2) - 9*e2)*Math.pow(D, 4)/24.0;
        double Q4 = (61 + 90*T1 + 298*C1 + 45*Math.pow(T1, 2) - 3*Math.pow(C1, 2) - 252*e2)*Math.pow(D, 6)/720.0;
        lat = fp - Q1*(Q2 - Q3 + Q4);

        // calc lng
        double Q5 = D;
        double Q6 = (1 + 2*T1 + C1)*Math.pow(D, 3)/6;
        double Q7 = (5 - 2*C1 + 28*T1 - 3*Math.pow(C1, 2) + 8*e2 + 24*Math.pow(T1, 2))*Math.pow(D, 5)/120.0;
        lng = lon0 + (Q5 - Q6 + Q7)/Math.cos(fp);

        lat = (lat * 180) / Math.PI;
        lng = (lng * 180) / Math.PI;


        return new Location(lat, lng);
    }
}
