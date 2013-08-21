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
    long mirrorTotalOperations;
    long mirrorSuccessfulOperations;
    long mirrorFailedOperations;
    double readCountPerSecond;
    double updateCountPerSecond;
    double writeCountPerSecond;
    double changePerSecond;
    double executePerSecond;
    int processorQueueSize;
    long activeTransactionCount;


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
        sb.append(redologSendBytesPerSecond).append(",");
        sb.append(gcCollectionCount).append(",");
        sb.append(mirrorTotalOperations).append(",");
        sb.append(mirrorSuccessfulOperations).append(",");
        sb.append(mirrorFailedOperations).append(",");
        sb.append(processorQueueSize).append(",");
        sb.append(readCountPerSecond).append(",");
        sb.append(updateCountPerSecond).append(",");
        sb.append(writeCountPerSecond).append(",");
        sb.append(changePerSecond).append(",");
        sb.append(executePerSecond).append(",");
        sb.append(activeTransactionCount);
        return sb.toString();

    }
    public String toString(){
        return getCsvHeader()+"\r\n"+toCsv();
    }
    public static String getCsvHeader(){
        return "pid,hostname,timestamp,totalMemory,heapUsedMemory,nonHeapUsedMemory," +
                "totalThreads,cpuPercent,redologSize,redologSendBytesPerSecond,gcCollectionCount," +
                "mirrorTotalOperations,mirrorSuccessfulOperations,mirrorFailedOperations,processorQueueSize,readCountPerSecond," +
                "updateCountPerSecond,writeCountPerSecond,changePerSecond,executePerSecond," +
                "activeTransactionCount";
    }

}
