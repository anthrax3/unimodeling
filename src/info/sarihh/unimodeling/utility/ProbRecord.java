package info.sarihh.unimodeling.utility;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

/*
 * Author: Sari Haj Hussein
 */
public class ProbRecord {

    public ProbRecord(String licensePlate, String probLoc, Timestamp sTime, Timestamp eTime, long sTimeUnix, long eTimeUnix) {
        this.licensePlate = licensePlate;
        this.probLoc = probLoc;
        this.sTime = sTime;
        this.eTime = eTime;
        this.sTimeUnix = sTimeUnix;
        this.eTimeUnix = eTimeUnix;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getProbLoc() {
        return probLoc;
    }

    public Timestamp getsTime() {
        return sTime;
    }

    public Timestamp geteTime() {
        return eTime;
    }

    public long getsTimeUnix() {
        return sTimeUnix;
    }

    public long geteTimeUnix() {
        return eTimeUnix;
    }

    public String getsTimeString() {
        return dateFormat.format(sTime).replace(" ", "!");
    }

    public String geteTimeString() {
        return dateFormat.format(eTime).replace(" ", "!");
    }
    String licensePlate, probLoc;
    Timestamp sTime, eTime;
    long sTimeUnix, eTimeUnix;
    private static SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS", Locale.ENGLISH);
}