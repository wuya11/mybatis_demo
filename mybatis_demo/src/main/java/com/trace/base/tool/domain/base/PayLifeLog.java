package com.trace.base.tool.domain.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wl
 * @description 单据生命流程日志
 * @date 2019/10/9 21:06
 */
public class PayLifeLog implements Serializable {

    /**
     * 日志主键编码
     */
    private Integer logId;
    /**
     * 单据包编码
     */
    private String packageId;
    /**
     * 单据编码
     */
    private String billCode;
    /**
     * 节点执行结果
     */
    private Byte lifeStatus;
    /**
     * 节点
     */
    private Byte lifeState;
    /**
     * 备注
     */
    private String markMsg;
    /**
     * 更新时间
     */
    private LocalDateTime updatetime;

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getBillCode() {
        return billCode;
    }

    public void setBillCode(String billCode) {
        this.billCode = billCode;
    }

    public Byte getLifeStatus() {
        return lifeStatus;
    }

    public void setLifeStatus(Byte lifeStatus) {
        this.lifeStatus = lifeStatus;
    }

    public Byte getLifeState() {
        return lifeState;
    }

    public void setLifeState(Byte lifeState) {
        this.lifeState = lifeState;
    }

    public String getMarkMsg() {
        return markMsg;
    }

    public void setMarkMsg(String markMsg) {
        this.markMsg = markMsg;
    }

    public LocalDateTime getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(LocalDateTime updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public String toString() {
        return "PayLifeLog{" +
                "logId=" + logId +
                ", packageId='" + packageId + '\'' +
                ", billCode='" + billCode + '\'' +
                ", lifeStatus=" + lifeStatus +
                ", lifeState=" + lifeState +
                ", markMsg='" + markMsg + '\'' +
                ", updatetime='" + updatetime + '\'' +
                '}';
    }
}
