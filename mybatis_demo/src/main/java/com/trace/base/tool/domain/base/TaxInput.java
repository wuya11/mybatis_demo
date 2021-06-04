package com.trace.base.tool.domain.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wl
 * @description 单据生命流程日志
 * @date 2019/10/9 21:06
 */
public class TaxInput implements Serializable {

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

    @Override
    public String toString() {
        return "TaxInput{" +
                "inputTaxId=" + inputTaxId +
                ", kId=" + kId +
                ", supId=" + supId +
                ", orgId=" + orgId +
                ", tax=" + tax +
                ", invoiceTitle='" + invoiceTitle + '\'' +
                ", remark='" + remark + '\'' +
                ", createtime=" + createtime +
                '}';
    }
}
