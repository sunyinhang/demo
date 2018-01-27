package com.payplatform.impl;

import com.haiercash.core.serialization.JsonSerializer;
import com.haiercash.payplatform.common.dao.AppointmentRecordDao;
import com.haiercash.spring.context.ThreadContext;
import com.haiercash.spring.context.TraceContext;
import com.haiercash.spring.util.HttpUtil;
import com.payplatform.ControllerTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-11-08.
 */
@Rollback(value = false)
public class CommonControllerTest extends ControllerTest {
    static {
        ThreadContext.init("", "", "");
        TraceContext.init();
    }

    @Autowired
    private AppointmentRecordDao appointmentRecordDao;

    //预约测试
    @Test
    public void testAppointment() throws Exception {
        int count = appointmentRecordDao.count(null);
        Map<String, Object> data = new HashMap<>();
        data.put("phone", "18612341234");
        data.put("name", "姓名A");
        data.put("education", "大本");
        data.put("location", "山东省日照市");
        String json = this.mockMvc.perform(MockMvcRequestBuilders.post("/api/payment/appointment")
                .content(JsonSerializer.serialize(data))
        ).andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Assert.assertTrue(HttpUtil.isSuccess(json));
        int countNew = appointmentRecordDao.count(null);
        Assert.assertEquals(count + 1, countNew);
    }
}
