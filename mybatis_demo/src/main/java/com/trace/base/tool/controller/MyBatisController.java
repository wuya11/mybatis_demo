package com.trace.base.tool.controller;


import com.trace.base.tool.domain.base.PayLifeLog;
import com.trace.base.tool.domain.base.TaxInput;
import com.trace.base.tool.domain.base.TaxInputSearch;
import com.trace.base.tool.mapper.LifeLogMapper;
import com.trace.base.tool.mybatis.study.base.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * mybatis测试demo
 *
 * @author wl
 * @date 2020-12-01
 */
@RestController
@RequestMapping("mybatis")
@Validated
public class MyBatisController {

    /***
     * 只是演示demo,正式环境，不能直接在controller 中调用 mapper ,中间套用一次service
     */
    @Autowired
    private LifeLogMapper lifeLogMapper;

    /**
     * 获取当前日志信息
     *
     * @return 返回存储数据
     */
    @GetMapping("/log")
    public PayLifeLog getTraceService() {
        return lifeLogMapper.getPayLifeLogById("5");
    }

    /**
     * 获取当前服务名称
     *
     * @return 返回存储数据
     */
    @GetMapping("/list")
    public List<PayLifeLog> listTraceService(String billCode, Page page) {
        List<PayLifeLog> payLifeLogList = lifeLogMapper.listPayLifeLogById(billCode, page);
        return payLifeLogList;
    }

    /**
     * 获取测试税务信息
     *
     * @return 返回存储数据
     */
    @GetMapping("/tax")
    public List<TaxInput> listInputTax(TaxInputSearch search) {
        search.setOrgIds(new int[]{0, 100, 101});
        search.setBeginTime(LocalDateTime.now().minusDays(300));
        search.setEndTime(LocalDateTime.now());
        List<TaxInput> taxInputList = lifeLogMapper.listInputTaxSearch(search);
        return taxInputList;
    }
}
