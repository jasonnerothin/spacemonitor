package com.gigaspaces.spacemonitor;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.cluster.replication.async.mirror.MirrorStatistics;
import com.j_spaces.core.filters.ReplicationStatistics;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.machine.Machines;
import org.openspaces.admin.space.*;
import org.openspaces.admin.vm.VirtualMachine;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: jpletka
 * Date: 8/19/13
 * Time: 9:52 AM
 */
public class SpaceMonitor implements Runnable{
    int pollingInterval = 5;
    String fileOutputPath;
    String adminUser;
    String adminPassword;
    boolean secured = false;
    String locators = null;

    Map<Long,Stat> lastCollectedStat = new HashMap<Long, Stat>();


    public SpaceMonitor(){

    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public String getFileOutputPath() {
        return fileOutputPath;
    }

    public void setFileOutputPath(String fileOutputPath) {
        this.fileOutputPath = fileOutputPath;

    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getSecured(){
        return String.valueOf(secured);
    }

    public void setSecured(String secured) {
        this.secured = Boolean.valueOf(secured);
    }

    public String getLocators() {
        return locators;
    }

    public void setLocators(String locators) {
        this.locators = locators;
    }

    public void startCollection(){
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(fileOutputPath,true));
            pw.println(Stat.getCsvHeader());
            pw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        new Thread(this).run();
    }

    @Override
    public void run() {
        AdminFactory factory = new AdminFactory();
        if(secured){
            factory.credentials(getAdminUser(),getAdminPassword());
        }
        factory.addLocators(locators);
        factory.discoverUnmanagedSpaces();
        Admin admin = factory.createAdmin();
        Machines machines = admin.getMachines();
        machines.waitFor(1);
        GridServiceContainers gscs = admin.getGridServiceContainers();
        gscs.waitFor(1);
        Spaces spaces = admin.getSpaces();
        spaces.waitFor("mySpace");
        while(true){
            collectStats(admin);
            writeStats();
            try{
                Thread.sleep(pollingInterval * 1000);
            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }
    public void collectStats(Admin admin){
        collectJVMStats(admin);
        collectRedologStats(admin);
        collectMirrorStats(admin);
        collectActivityStats(admin);
    }
    public void collectJVMStats(Admin admin){
        GridServiceContainer containers[] = admin.getGridServiceContainers().getContainers();
        for(int i=0;i<containers.length;i++){
            VirtualMachine vm = containers[i].getVirtualMachine();
            Stat stat = lastCollectedStat.get(vm.getDetails().getPid());
            if(stat == null){
                stat = new Stat();
                stat.pid = vm.getDetails().getPid();
            }
            stat.totalMemory = vm.getDetails().getMemoryHeapMaxInBytes();
            stat.heapUsedMemory = vm.getStatistics().getMemoryHeapUsedInBytes();
            stat.cpuPercent = vm.getStatistics().getCpuPerc();
            stat.gcCollectionCount = vm.getStatistics().getGcCollectionCount();
            stat.hostname = vm.getMachine().getHostName();
            stat.totalThreads = vm.getStatistics().getThreadCount();
            stat.nonHeapUsedMemory = vm.getStatistics().getMemoryNonHeapUsedInBytes();
            stat.timestamp = new Date();
            lastCollectedStat.put(vm.getDetails().getPid(),stat);
        }
    }
    public void collectRedologStats(Admin admin){
        Space space = admin.getSpaces().waitFor("mySpace", 10, TimeUnit.SECONDS);
        space.waitFor(space.getNumberOfInstances(), SpaceMode.PRIMARY,10 , TimeUnit.SECONDS);
        SpacePartition partitions[]= space.getPartitions();
        long redologSize = 0;
        long redologBytesPerSecond = 0;
        for (int i=0;i<partitions.length;i++)
        {
            SpacePartition partition = partitions[i];
            redologSize += partition.getPrimary().getStatistics().getReplicationStatistics().
             getOutgoingReplication().getRedoLogSize();

            List<ReplicationStatistics.OutgoingChannel> channelList = partition.getPrimary().getStatistics().getReplicationStatistics().getOutgoingReplication().getChannels();
            for(ReplicationStatistics.OutgoingChannel channel : channelList){
                redologBytesPerSecond += channel.getSendBytesPerSecond();
            }

        }
        for(Long pid : lastCollectedStat.keySet()){
            Stat stat = lastCollectedStat.get(pid);
            stat.redologSize = redologSize;
            stat.redologSendBytesPerSecond = redologBytesPerSecond;
        }
    }
    public void collectMirrorStats(Admin admin){
        long mirrorTotalOperations=0;
        long mirrorSuccessfulOperations=0;
        long mirrorFailedOperations=0;

        for (Space space : admin.getSpaces()) {
            for (SpaceInstance spaceInstance : space) {
                MirrorStatistics mirrorStat = spaceInstance.getStatistics().getMirrorStatistics();
                 // check if this instance is mirror
                 if(mirrorStat != null)
                 {

                    mirrorTotalOperations= mirrorStat.getOperationCount();
                    mirrorSuccessfulOperations = mirrorStat.getSuccessfulOperationCount();
                    mirrorFailedOperations = mirrorStat.getFailedOperationCount();
                 }
            }

        }
        for(Long pid : lastCollectedStat.keySet()){
            Stat stat = lastCollectedStat.get(pid);
            stat.mirrorTotalOperations = mirrorTotalOperations;
            stat.mirrorSuccessfulOperations = mirrorSuccessfulOperations;
            stat.mirrorFailedOperations = mirrorFailedOperations;

        }
    }
    public void collectActivityStats(Admin admin){
        double readCountPerSecond = 0;
        double updateCountPerSecond = 0;
        double writeCountPerSecond = 0;
        double changePerSecond = 0;
        double executePerSecond = 0;
        int processorQueueSize = 0;
        long activeTransactionCount = 0;

            for (Space space : admin.getSpaces()) {
                for (SpaceInstance spaceInstance : space) {
                    SpaceInstanceStatistics stats = spaceInstance.getStatistics();

                    readCountPerSecond += stats.getReadPerSecond();
                    updateCountPerSecond += stats.getUpdatePerSecond();
                    writeCountPerSecond += stats.getWritePerSecond();
                    changePerSecond += stats.getChangePerSecond();
                    executePerSecond += stats.getExecutePerSecond();
                    processorQueueSize += stats.getProcessorQueueSize();
                    activeTransactionCount += stats.getActiveTransactionCount();
                }
            }
            for(Long pid : lastCollectedStat.keySet()){
                Stat stat = lastCollectedStat.get(pid);
                stat.readCountPerSecond = readCountPerSecond;
                stat.updateCountPerSecond = updateCountPerSecond;
                stat.writeCountPerSecond = writeCountPerSecond;
                stat.changePerSecond = changePerSecond;
                stat.executePerSecond = executePerSecond;
                stat.processorQueueSize = processorQueueSize;
                stat.activeTransactionCount = activeTransactionCount;
            }
        }
    public void writeStats(){
        try{
            PrintWriter pw = new PrintWriter(new FileWriter(fileOutputPath,true));
            for(Stat stat : lastCollectedStat.values()){
                pw.println(stat.toCsv());
            }
            pw.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public static void main(String args[]){
        System.setProperty("spaceMonitor.fileOutputPath",System.getProperty("spaceMonitor.fileOutputPath","/logs/stress/spacemonitor.log"));
        System.setProperty("spaceMonitor.adminUser",System.getProperty("spaceMonitor.adminUser","deployer"));
        System.setProperty("spaceMonitor.adminPassword",System.getProperty("spaceMonitor.adminPassword","password"));
        System.setProperty("spaceMonitor.locators",System.getProperty("spaceMonitor.locators","localhost:4170"));
        System.setProperty("spaceMonitor.secured",System.getProperty("spaceMonitor.secured","true"));

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("/META-INF/spring/pu.xml");
        SpaceMonitor spaceMonitor = (SpaceMonitor)appContext.getBean("spaceMonitor");
        while(true){}
        //The startCollection automatically runs

    }
}
