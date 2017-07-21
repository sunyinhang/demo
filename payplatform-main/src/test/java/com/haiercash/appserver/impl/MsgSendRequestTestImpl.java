package com.haiercash.appserver.impl;

import com.haiercash.appserver.Application;
import com.haiercash.common.data.MsgRequestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * msg send request test.
 * @author Liu qingxiang
 * @since v1.5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class })
@Transactional
@SqlConfig(encoding = "utf-8")
public class MsgSendRequestTestImpl {

    @Autowired
    private MsgRequestRepository msgRequestRepository;


    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    @Sql("/dbfile/msgRequest.sql")
    @Rollback(value = true)
    @Test
    public void testTransaction(){
        /*List<MsgRequest> msgRequest = (List<MsgRequest>) msgRequestRepository.findAll();
        MsgRequest msgRequest1 = msgRequest.get(0);
        String id = msgRequest1.getId();
        msgRequest1.setUserId("sdfdslkfsdlkf");
        msgRequestRepository.save(msgRequest1);
        String sql = "update msg_send_request set user_id = '23423' where id = '" + id + "'";
        Query query = entityManager.createNativeQuery(sql);
        int result = query.executeUpdate();
        MsgRequest msgRequest2 = msgRequestRepository.findOne(id);
        String sql2 = "select * from msg_send_request where id = '" + id + "'";
        Query query2 = entityManager.createNativeQuery(sql2, MsgRequest.class);
        List<MsgRequest> list = (List<MsgRequest>)query2.getResultList();

        System.out.println(result);*/

    }
    @Transactional
    @Rollback
    @Test
    public void testMsgXcdTask(){

        //msgSendTask.getXcdPassApplseq();
    }
}
