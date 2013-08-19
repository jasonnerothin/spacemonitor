package com.gigaspaces.spacemonitor;

import java.util.Date;

/**
 * User: jpletka
 * Date: 8/19/13
 * Time: 12:24 PM
 */
public class Stat {
    long pid;
    Date timestamp;
    long totalMemory;
    long usedMemory;
    int totalThreads;
    double cpuPercent;
    long redologSize;

    public String toCsv(){
        StringBuilder sb = new StringBuilder();
        sb.append(pid).append(",");
        sb.append(timestamp).append(",");
        sb.append(totalMemory).append(",");
        sb.append(usedMemory).append(",");
        sb.append(totalThreads).append(",");
        sb.append(cpuPercent).append(",");
        sb.append(redologSize);
        return sb.toString();

    }
    public String toString(){
        return getCsvHeader()+"\r\n"+toCsv();
    }
    public static String getCsvHeader(){
        return "pid,timestamp,totalMemory,usedMemory,totalThreads,cpuPercent,redologSize";
    }

}
