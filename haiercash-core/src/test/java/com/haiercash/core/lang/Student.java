package com.haiercash.core.lang;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by 许崇雷 on 2017-11-24.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
class Student extends Cloneable<Student> {
    private int id;
    private String name;
}
