package com.trace.base.tool.service.impl;



import com.trace.base.tool.service.TraceService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author wl
 * @description trace test
 * @date 2020-03-31
 */
@Service
public class TraceServiceImpl implements TraceService {



    @Async
    @Override
    public void testJdkAsync(int i) {
        try {
            Thread.sleep(1000);
            String message = "执行子线程：" + i  + ",traceid->" ;
            System.out.println(message);
        } catch (Exception e) {

        }
    }

    @Async("decorator_task")
    @Override
    public void testDecoratorAsync(int i) {
        try {
            Thread.sleep(1000);
          String message = "执行子线程：" + i +  ",traceid->";
            System.out.println(message);
        } catch (Exception e) {

        }
    }

    @Async("task_normal")
    @Override
    public void testBaseAsync(int i){
        try {
            Thread.sleep(1000);
            String message = "执行子线程：" + i +  ",traceid->" ;
            System.out.println(message);
        } catch (Exception e) {

        }
    }


}
