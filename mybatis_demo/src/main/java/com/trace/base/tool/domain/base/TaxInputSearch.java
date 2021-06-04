package com.trace.base.tool.domain.base;


import com.trace.base.tool.mybatis.study.base.Page;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * @author wl
 * @description 单据生命流程日志
 * @date 2019/10/9 21:06
 */
public class TaxInputSearch extends Page implements Serializable {

    /**
     * 日志主键编码
     */
    private Integer inputTaxId;
    /**
     * 单据包编码
     */
    private Integer kId;
    /**
     * 单据编码
     */
    private Integer supId;
    /**
     * 节点执行结果
     */
    private Integer orgId;
    /**
     * 节点
     */
    private Integer tax;
    /**
     * 备注
     */
    private String invoiceTitle;
    /**
     * 更新时间
     */
    private String remark;
    /**
     * 更新时间
     */
    private LocalDateTime createtime;
    /**
     * 更新时间
     */
    private LocalDateTime beginTime;
    /**
     * 更新时间
     */
    private LocalDateTime endTime;
    /**
     * 节点执行结果
     */
    private int[] orgIds;

    public Integer getInputTaxId() {
        return inputTaxId;
    }

    public void setInputTaxId(Integer inputTaxId) {
        this.inputTaxId = inputTaxId;
    }

    public Integer getkId() {
        return kId;
    }

    public void setkId(Integer kId) {
        this.kId = kId;
    }

    public Integer getSupId() {
        return supId;
    }

    public void setSupId(Integer supId) {
        this.supId = supId;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
    }

    public Integer getTax() {
        return tax;
    }

    public void setTax(Integer tax) {
        this.tax = tax;
    }

    public String getInvoiceTitle() {
        return invoiceTitle;
    }

    public void setInvoiceTitle(String invoiceTitle) {
        this.invoiceTitle = invoiceTitle;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatetime() {
        return createtime;
    }

    public void setCreatetime(LocalDateTime createtime) {
        this.createtime = createtime;
    }

    public int[] getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(int[] orgIds) {
        this.orgIds = orgIds;
    }

    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "TaxInputSearch{" +
                "inputTaxId=" + inputTaxId +
                ", kId=" + kId +
                ", supId=" + supId +
                ", orgId=" + orgId +
                ", tax=" + tax +
                ", invoiceTitle='" + invoiceTitle + '\'' +
                ", remark='" + remark + '\'' +
                ", createtime=" + createtime +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", orgIds=" + Arrays.toString(orgIds) +
                '}';
    }
}
