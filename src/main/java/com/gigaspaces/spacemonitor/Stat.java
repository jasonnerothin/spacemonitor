package com.gigaspaces.spacemonitor;

import java.util.Date;

/**
 * User: jpletka
 * Date: 8/19/13
 * Time: 12:24 PM
 */
public class Stat {
    long pid;
    String hostname;
    Date timestamp;
    long totalMemory;
    long heapUsedMemory;
    long nonHeapUsedMemory;
    int totalThreads;
    double cpuPercent;
    long redologSize;
    long redologSendBytesPerSecond;
    long gcCollectionCount;

    public String toCsv(){
        StringBuilder sb = new StringBuilder();
        sb.append(pid).append(",");
        sb.append(hostname).append(",");
        sb.append(timestamp).append(",");
        sb.append(totalMemory).append(",");
        sb.append(heapUsedMemory).append(",");
        sb.append(nonHeapUsedMemory).append(",");
        sb.append(totalThreads).append(",");
        sb.append(cpuPercent).append(",");
        sb.append(redologSize).append(",");
        sb.append(gcCollectionCount).append(",");
        sb.append(redologSendBytesPerSecond);
        return sb.toString();

    }
    public String toString(){
        return getCsvHeader()+"\r\n"+toCsv();
    }
    public static String getCsvHeader(){
        return "pid,hostname,timestamp,totalMemory,heapUsedMemory,nonHeapUsedMemory,totalThreads,cpuPercent,redologSize,gcCollectionCount,redologSendBytesPerSecond";
    }

}
