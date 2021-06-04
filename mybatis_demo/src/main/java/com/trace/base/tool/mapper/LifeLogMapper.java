package com.trace.base.tool.mapper;

import com.trace.base.tool.annotation.Enhancer;
import com.trace.base.tool.domain.base.PayLifeLog;
import com.trace.base.tool.domain.base.TaxInput;
import com.trace.base.tool.domain.base.TaxInputSearch;
import com.trace.base.tool.mybatis.study.base.Page;
import com.trace.base.tool.sqlprovider.LifeLogSqlProvider;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wl
 * @description 支付请求日志-数据接口
 * @date 2019/10/10 18:42
 */
@Mapper
@Repository
public interface LifeLogMapper {

    /**
     * 新增请求源数据
     *
     * @param payLifeLog 源数据
     */
    @InsertProvider(type = LifeLogSqlProvider.class, method = "addPayLifeLogSql")
    @Options(useGeneratedKeys = true, keyProperty = "logId", keyColumn = "log_id")
    void addPayLifeLog(PayLifeLog payLifeLog);

    /**
     * 基于单据号获取周期信息
     *
     * @param billCode 单据编码
     * @return 返回信息
     */
    @SelectProvider(type = LifeLogSqlProvider.class, method = "listPayLifeLogByBillCodeSql")
    List<PayLifeLog> listPayLifeLogByBillCode(@Param("billCode") String billCode);

    /**
     * 获取批次支付失败日志
     *
     * @param billCode 单号
     * @return 结果
     */
    @SelectProvider(type = LifeLogSqlProvider.class, method = "getPaymentBatchPayFailLogSql")
    PayLifeLog getPaymentBatchPayFailLog(@Param("billCode") String billCode);

    /**
     * 获取批次支付失败日志
     *
     * @param billCode 单号
     * @return 结果
     */
    @SelectProvider(type = LifeLogSqlProvider.class, method = "getPayLifeLogByIdSql")
    PayLifeLog getPayLifeLogById(@Param("billCode") String billCode);

    /**
     * 获取批次支付失败日志
     *
     * @param billCode 单号
     * @param page     分页参数
     * @return 结果
     */
    @SelectProvider(type = LifeLogSqlProvider.class, method = "listPayLifeLogByIdSql")
    List<PayLifeLog> listPayLifeLogById(@Param("billCode") String billCode, @Param("page") Page page);

    /**
     * 获取进项税信息
     *
     * @param kid  单号
     * @param page 分页参数
     * @return 结果
     */
    @Enhancer
    @SelectProvider(type = LifeLogSqlProvider.class, method = "listInputTaxSql")
    List<TaxInput> listInputTax(@Param("kid") Integer kid, @Param("page") Page page);

    /**
     * 获取进项税信息
     *
     * @param inputSearch 综合查询条件
     * @return 结果
     */
    @Enhancer
    @SelectProvider(type = LifeLogSqlProvider.class, method = "listInputTaxSearchSql")
    List<TaxInput> listInputTaxSearch(@Param("inputSearch") TaxInputSearch inputSearch);
}
