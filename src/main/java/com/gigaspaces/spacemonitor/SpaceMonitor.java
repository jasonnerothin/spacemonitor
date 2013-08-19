package com.gigaspaces.spacemonitor;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.GridServiceContainers;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.vm.VirtualMachine;
import org.openspaces.core.GigaSpace;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileWriter;
import java.util.HashMap;
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
    FileWriter fw = null;

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

    public boolean isSecured() {
        return secured;
    }

    public void setSecured(boolean secured) {
        this.secured = secured;
    }

    public String getLocators() {
        return locators;
    }

    public void setLocators(String locators) {
        this.locators = locators;
    }

    public void startCollection(){
        try{
            fw = new FileWriter(fileOutputPath);
            fw.write(Stat.getCsvHeader()+"\r\n");
        }catch(Exception e){
            e.printStackTrace();
        }
        new Thread(this).run();
    }

    @Override
    public void run() {
        AdminFactory factory = new AdminFactory();
        factory.credentials(getAdminUser(),getAdminPassword());
        factory.addLocators(locators);
        Admin admin = factory.createAdmin();
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
            stat.usedMemory = vm.getStatistics().getMemoryHeapCommittedInBytes();
            stat.cpuPercent = vm.getStatistics().getCpuPerc();
            stat.gcCollectionCount = vm.getStatistics().getGcCollectionCount();
            stat.hostname = vm.getMachine().getHostName();
            stat.totalThreads = vm.getStatistics().getThreadCount();
            lastCollectedStat.put(vm.getDetails().getPid(),stat);
        }
    }
    public void collectRedologStats(Admin admin){
        Space space = admin.getSpaces().waitFor("mySpace", 10, TimeUnit.SECONDS);
        space.waitFor(space.getNumberOfInstances(), SpaceMode.PRIMARY,10 , TimeUnit.SECONDS);
        SpacePartition partitions[]= space.getPartitions();
        long redologSize = 0;
        for (int i=0;i<partitions.length;i++)
        {
            SpacePartition partition = partitions[i];

            redologSize += partition.getPrimary().getStatistics().getReplicationStatistics().
             getOutgoingReplication().getRedoLogSize();
        }
        for(Long pid : lastCollectedStat.keySet()){
            Stat stat = lastCollectedStat.get(pid);
            stat.redologSize = redologSize;
        }
    }
    public void writeStats(){
        try{
            for(Stat stat : lastCollectedStat.values()){
                fw.write(stat.toCsv()+"\r\n");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    public static void main(String args[]){
        System.setProperty("spaceMonitor.fileOutputPath","/tmp/spacemonitor.log");
        System.setProperty("spaceMonitor.adminUser","gsadmin");
        System.setProperty("spaceMonitor.adminPassword","password");
        System.setProperty("spaceMonitor.locators","gigamemgrid1:4170");
        System.setProperty("spaceMonitor.secured","false");

        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("pu.xml");
        SpaceMonitor spaceMonitor = (SpaceMonitor)appContext.getBean("spaceMonitor");
        //The startCollection automatically runs

    }
}
