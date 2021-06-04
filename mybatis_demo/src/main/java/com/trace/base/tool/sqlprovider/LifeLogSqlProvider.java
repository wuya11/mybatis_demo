package com.trace.base.tool.sqlprovider;

import com.trace.base.tool.domain.base.PayLifeLog;
import com.trace.base.tool.domain.base.TaxInputSearch;
import com.trace.base.tool.mybatis.study.base.Page;
import com.trace.base.tool.mybatis.study.util.sql.SQL;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;

import java.util.Objects;

/**
 * @author wl
 * @description 请求日志sql生成器
 * @date 2019/10/8 18:43
 */
public class LifeLogSqlProvider {

    /**
     * 新增请求源数据sql
     *
     * @param payLifeLog 源数据
     * @return 新增请求源数据sql
     */
    public String addPayLifeLogSql(PayLifeLog payLifeLog) {
        return new SQL()
                .insert("caiwu_pay_life_log")
                .values("bill_code", "#{billCode}")
                .values(StringUtils.isNotEmpty(payLifeLog.getPackageId()), "package_id", "#{packageId}")
                .values("life_status", "#{lifeStatus}")
                .values("life_state", "#{lifeState}")
                .values(StringUtils.isNotEmpty(payLifeLog.getMarkMsg()), "mark_msg", "#{markMsg}")
                .duplicateUpdate("updatetime", "#{updatetime}")
                .duplicateUpdate("life_status", "#{lifeStatus}")
                .duplicateUpdate(StringUtils.isNotEmpty(payLifeLog.getMarkMsg()), "mark_msg", "#{markMsg}")
                .duplicateUpdate(StringUtils.isNotEmpty(payLifeLog.getPackageId()), "package_id", "#{packageId}")
                .build();
    }

    /**
     * 基于单据号获取周期信息
     *
     * @param billCode 单据编码
     * @return 返回信息
     */
    public String listPayLifeLogByBillCodeSql(String billCode) {
        return new SQL()
                .select(" package_id,bill_code,life_state,life_status,mark_msg,updatetime")
                .from("caiwu_pay_life_log")
                .where("bill_code=#{billCode}")
                .orderBy("life_state")
                .build();
    }

    /**
     * 获取批次支付失败日期
     *
     * @param billCode 单号
     * @return 结果
     */
    public String getPaymentBatchPayFailLogSql(@Param("billCode") String billCode) {
        return new SQL()
                .select(" package_id,bill_code,life_state,life_status,mark_msg,updatetime")
                .from("caiwu_pay_life_log")
                .where("bill_code = #{billCode}")
                .limit(0, 1)
                .build();
    }

    public String getPayLifeLogByIdSql(@Param("billCode") String billCode) {
        String sql = " select log_id as logId, package_id as packageId,bill_code as billCode,life_state as lifeState,life_status as lifeStatus,mark_msg as markMsg,updatetime  " +
                "from caiwu_pay_life_log where " +
                "bill_code = #{billCode}" +
                "limit 0,1";
        return sql;
    }

    /**
     * 获取批次支付失败日志
     *
     * @param billCode 单号
     * @param page     分页参数
     * @return 结果
     */
    public String listPayLifeLogByIdSql(@Param("billCode") String billCode, @Param("page") Page page) {
        return new SQL()
                .select(" package_id,bill_code,life_state,life_status,mark_msg,updatetime")
                .from("caiwu_pay_life_log")
                .where("bill_code = #{billCode}")
                .build();
    }

    public String listInputTaxSql(@Param("kid") Integer kid, @Param("page") Page page) {
        return new SQL()
                .select("input_tax_id, k_id,sup_id,k_sup_id,org_id,a.tax,invoice_title,remark")
                .from("tx_sup_goods_input_tax a")
                .innerJoin("tx_tax b on a.tax=b.tax")
                .where(kid > 0, "a.k_id = #{kid}")
                .orderBy("a.k_id desc")
                .build();
    }

    public String listInputTaxSearchSql(@Param("inputSearch") TaxInputSearch inputSearch) {
        return new SQL()
                .select("input_tax_id, k_id,sup_id,k_sup_id,org_id,a.tax,invoice_title,remark")
                .from("tx_sup_goods_input_tax a")
                .innerJoin("tx_tax b on a.tax=b.tax")
                .where("1=1")
                .where(Objects.nonNull(inputSearch.getkId()), "a.k_id = #{inputSearch.kId}")
                .andIn("a.org_id", inputSearch.getOrgIds())
                .and("a.createtime> #{inputSearch.beginTime}")
                .and("a.createtime< #{inputSearch.endTime}")
                .orderBy("a.k_id desc")
                .build();
    }


}
