package com.gigaspaces.spacemonitor;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.core.GigaSpace;

/**
 * User: jpletka
 * Date: 8/19/13
 * Time: 9:52 AM
 */
public class SpaceMonitor implements Runnable{
    int pollingInterval = 5;
    GigaSpace gigaSpace;
    String fileOutputPath;
    String adminUser;
    String adminPassword;


    public SpaceMonitor(){

    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public GigaSpace getGigaSpace() {
        return gigaSpace;
    }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
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

    public void startCollection(){
        new Thread(this).run();
    }

    @Override
    public void run() {
        AdminFactory factory = new AdminFactory();
        factory.credentials(getAdminUser(),getAdminPassword());
        Admin admin = factory.createAdmin();


    }
}
