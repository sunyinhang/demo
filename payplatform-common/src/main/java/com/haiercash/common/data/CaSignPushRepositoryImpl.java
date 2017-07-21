package com.haiercash.common.data;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class CaSignPushRepositoryImpl {
    @PersistenceContext
    EntityManager entityManager;


    public List queryCaSignPush(String applSeq, String date, String channelNo, Integer page, Integer size,String signType) {
        int start = -1;
        int end = -1;
        if (page != null && size != null && page > 0 && size > 0) {
            start = (page - 1) * size + 1;
            end = start + size - 1;
        }
        String sql;
        sql = "select id,appl_seq,channel_no,insert_time ,push_time,flag,sign_type FROM  uauth_ca_sign_push where 1=1";

        if(!StringUtils.isEmpty(applSeq)){
            sql += " and appl_seq = '" + applSeq +"'";
        }
        if(!StringUtils.isEmpty(date)){
            sql += " and to_char(insert_time,'YYYY-MM-DD')  = '" + date + "'";
        }
        if(!StringUtils.isEmpty(channelNo)){
            sql += " and channel_no  = '" + channelNo +"'";
        }
        if(!StringUtils.isEmpty(signType)){
            sql += " and sign_type  = '" + signType +"'";
        }
        if (start >= 0) {
            sql = String.format("select * from ( select t.*, rownum rn from (%s) t where rownum <= %d) where rn >= %d",
                    sql, end, start);
        }
        System.out.println("执行查询sql为：" + sql);
        Query query = entityManager.createNativeQuery(sql);
        return query.getResultList();
    }
}
