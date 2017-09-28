package com.bestvike.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * Created by 许崇雷 on 2017-09-22.
 */
@Data
@AllArgsConstructor
public class Person {
    private long uid;
    private String name;
    private Date birthday;
}
