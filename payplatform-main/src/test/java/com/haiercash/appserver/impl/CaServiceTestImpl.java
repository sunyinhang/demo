package com.haiercash.appserver.impl;

import com.haiercash.appserver.Application;
import com.haiercash.appserver.apporder.AppOrderController;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.service.DhkService;
import com.haiercash.appserver.service.FileSignService;
import com.haiercash.appserver.web.CmisDataController;
import com.haiercash.common.data.AppContractRepository;
import com.haiercash.common.data.AppOrderRepository;
import com.haiercash.common.data.CityRepository;
import com.haiercash.common.data.CommonRepaymentPersonRepository;
import com.haiercash.common.data.MsgRequest;
import com.haiercash.common.data.MsgRequestRepository;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.common.data.UAuthUserTokenRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.jdbc.SqlScriptsTestExecutionListener;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;

/**
 * AppOrder service test impl.
 *
 * @author Liu qingxiang
 * @since 1.2.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        SqlScriptsTestExecutionListener.class })
@Transactional
public class CaServiceTestImpl  {

    @Autowired
    private AppOrderController appOrderController;

    @Autowired
    private AppContractRepository appContractRepository;

    @Autowired
    private AttachService attachService;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private AppOrderService appOrderService;
    @Autowired
    private CommonRepaymentPersonService commonRepaymentPersonService;
    @Autowired
    DhkService dhkService;
    @Autowired
    private MsgRequestRepository msgRequestRepository;

    // 注入订单dao接口
    @Autowired
    AppOrderRepository appOrderRepository;
    @Autowired
    UAuthCASignRequestRepository uAuthCASignRequestRepository;
    @Autowired
    UAuthUserTokenRepository uAuthUserTokenRepository;
    @Autowired
    CASignService cASignService;
    @Autowired
    CmisDataController dataController;
    @Autowired // 共同还款人dao
            CommonRepaymentPersonRepository commonRepaymentPersonRepository;
    @Autowired // 信贷service层
            CmisApplService cmisApplService;

    @Autowired
    private FileSignService fileSignService;


    @Autowired
    @LoadBalanced
    public RestTemplate restTemplate;

    @Value("${common.address.gateUrl}")
    private String gateUrl;

    @Before
    public void init() {
        this.gateUrl = "http://localhost:8010";
        this.appOrderController = PowerMockito.mock(AppOrderController.class);

    }



    @Test
    public void contractConfirmTest(){
//        Map<String, Object> map = appOrderService.getContractConfirmData("a6235a51c09743dbbf94251352a15972");
//        System.out.print(map.get("loanpayment"));
        MsgRequest msgRequest = msgRequestRepository.findByUserIdLimit1("411381198212073534");
        System.out.println(msgRequest.getPayCode());
        int i = 1;
    }


    @Test
    public void checkIfMs() {
        //String order= fileSignService.signAgreementByCmis("1145354","DOUZIPERSONAL","0", false, "978e2f48e02746d0bdc724f311ca3074");
        String order= fileSignService.signAgreementByCmis("1254641","LOANUOKO","0", false, "22f4b9800e474ca1aadc1f40f9df7932");
    }

    /**
     * 测试删除共同还款人替代影像信息.
     */
    @Test
    public void deleteReplaceImages() {
        attachService.deleteCommonReplaceImage("1254965", "C201612020510001406010", "16129a");
    }

    /***
     * 测试合同签章请求数据
     */
    @Test
    public void requestData() {
        //String order= fileSignService.signAgreementByCmis("1145354","DOUZIPERSONAL","0", false, "978e2f48e02746d0bdc724f311ca3074");
/**/        String order= fileSignService.signAgreementByCmis("1144444","LOANUOKO","0", false, "6f498c7c56014d82a094c8c432d7e542");
    }

    /***
     * 测试合同互动合同
     */
    @Test
    public void requestData1() {
      String order= fileSignService.signAgreementByCmis("1255829","HDJR_JKHT","0", false, "e9c3b73a1857411fa97f9270f8a39c94");
    }

    /***
     * 测试合同签章请求数据
     */
    @Test
    public void requestyouke() {
        String order= fileSignService.signAgreementByCmis("1145395","LOANUOKO","0", false, "7d7a1e1ec7ff4e07a6c492cff575b2eb");
    }

    /***
     * 测试新签章系统
     */
    @Test
    public void signnew() {
       // String order= newFileSignService.signNew();
    }


}

