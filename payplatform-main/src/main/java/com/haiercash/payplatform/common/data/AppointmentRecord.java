package com.haiercash.payplatform.common.data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by 许崇雷 on 2017-11-08.
 */
@Data
@Entity
@Table(name = "APPOINTMENT_RECORD")
public class AppointmentRecord {
    @Id
    private String id;
    private String phone;
    private String name;
    private String education;
    private String createdate;
}
