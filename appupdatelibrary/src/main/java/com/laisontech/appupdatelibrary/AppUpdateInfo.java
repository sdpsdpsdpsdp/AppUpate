package com.laisontech.appupdatelibrary;

import java.io.Serializable;
import java.util.Random;

/**
 * ..................................................................
 * .         The Buddha said: I guarantee you have no bug!          .
 * .                                                                .
 * .                            _ooOoo_                             .
 * .                           o8888888o                            .
 * .                           88" . "88                            .
 * .                           (| -_- |)                            .
 * .                            O\ = /O                             .
 * .                        ____/`---'\____                         .
 * .                      .   ' \\| |// `.                          .
 * .                       / \\||| : |||// \                        .
 * .                     / _||||| -:- |||||- \                      .
 * .                       | | \\\ - /// | |                        .
 * .                     | \_| ''\---/'' | |                        .
 * .                      \ .-\__ `-` ___/-. /                      .
 * .                   ___`. .' /--.--\ `. . __                     .
 * .                ."" '< `.___\_<|>_/___.' >'"".                  .
 * .               | | : `- \`.;`\ _ /`;.`/ - ` : | |               .
 * .                 \ \ `-. \_ __\ /__ _/ .-` / /                  .
 * .         ======`-.____`-.___\_____/___.-`____.-'======          .
 * .                            `=---='                             .
 * ..................................................................
 * Created by SDP on 2018/6/4.
 */

public class AppUpdateInfo implements Serializable {
    private static final long serialVersionUID = 201806041454L;
    private int apkid;//apk的id
    private String appname;//app名称
    private String companyname;//公司名称
    private String projectname;//app包名
    private int versioncode;//版本号
    private String versionname;//版本名称
    private double apkfilesize;//大小
    private String applogolink;//logo
    private String apkdownloadlink;//下载链接
    private String appintroduction;//app描述
    private String updatedate;//更新日期
    private String updatedescription;//更新信息
    private int forceupdating;//是否强制更新  0-【APP判断有新版本时，提示当前已有新版本，是否立即更新】；1-【APP判断有新版本时，提示当前版本太旧，请立即更新】
    private int priority;  //app的等级

    public AppUpdateInfo() {
        setAppPri();
    }

    public int getApkid() {
        return apkid;
    }

    public void setApkid(int apkid) {
        this.apkid = apkid;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname;
    }

    public int getVersioncode() {
        return versioncode;
    }

    public void setVersioncode(int versioncode) {
        this.versioncode = versioncode;
    }

    public String getVersionname() {
        return versionname;
    }

    public void setVersionname(String versionname) {
        this.versionname = versionname;
    }

    public int getForceupdating() {
        return forceupdating;
    }

    public void setForceupdating(int forceupdating) {
        this.forceupdating = forceupdating;
    }

    public double getApkfilesize() {
        return apkfilesize;
    }

    public void setApkfilesize(double apkfilesize) {
        this.apkfilesize = apkfilesize;
    }

    public String getApkdownloadlink() {
        return apkdownloadlink;
    }

    public void setApkdownloadlink(String apkdownloadlink) {
        this.apkdownloadlink = apkdownloadlink;
    }

    public String getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(String updatedate) {
        this.updatedate = updatedate;
    }

    public String getUpdateinfo() {
        return updatedescription;
    }

    public void setUpdateinfo(String updateinfo) {
        this.updatedescription = updateinfo;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getApplogolink() {
        return applogolink;
    }

    public void setApplogolink(String applogolink) {
        this.applogolink = applogolink;
    }

    public String getAppintroduction() {
        return appintroduction;
    }

    public void setAppintroduction(String appintroduction) {
        this.appintroduction = appintroduction;
    }

    private void setAppPri() {
        Random random = new Random();
        priority = random.nextInt(100);
    }

    @Override
    public String toString() {
        return "AppUpdateInfo{" +
                "apkid=" + apkid +
                ", appname='" + appname + '\'' +
                ", companyname='" + companyname + '\'' +
                ", projectname='" + projectname + '\'' +
                ", versioncode=" + versioncode +
                ", versionname='" + versionname + '\'' +
                ", apkfilesize=" + apkfilesize +
                ", applogolink='" + applogolink + '\'' +
                ", apkdownloadlink='" + apkdownloadlink + '\'' +
                ", appintroduction='" + appintroduction + '\'' +
                ", updatedate='" + updatedate + '\'' +
                ", updatedescription='" + updatedescription + '\'' +
                ", forceupdating=" + forceupdating +
                ", priority=" + priority +
                '}';
    }
}
