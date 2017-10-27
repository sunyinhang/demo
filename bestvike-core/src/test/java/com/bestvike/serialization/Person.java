package com.bestvike.serialization;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-09-22.
 */
@Data
@AllArgsConstructor
public class Person {
    @JSONField(ordinal = 1)
    private long uid;
    @JSONField(ordinal = 2)
    private String name;
    @JSONField(ordinal = 3)
    private Date birthday;
}
