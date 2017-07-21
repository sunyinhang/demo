package com.haiercash.appserver.impl;

import com.haiercash.appserver.AppOrderServiceTest;
import com.haiercash.appserver.Application;
import com.haiercash.appserver.apporder.AppOrderController;
import com.haiercash.appserver.service.AcquirerService;
import com.haiercash.appserver.service.AppManageService;
import com.haiercash.appserver.service.AppOrderService;
import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CASignService;
import com.haiercash.appserver.service.CmisApplService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.service.DhkService;
import com.haiercash.appserver.service.FileSignService;
import com.haiercash.appserver.web.CmisDataController;
import com.haiercash.common.apporder.utils.FormatUtil;
import com.haiercash.common.data.AppContractRepository;
import com.haiercash.common.data.AppOrder;
import com.haiercash.common.data.AppOrderRepository;
import com.haiercash.common.data.BusinessType;
import com.haiercash.common.data.CityRepository;
import com.haiercash.common.data.CommonRepaymentPersonRepository;
import com.haiercash.common.data.MsgRequest;
import com.haiercash.common.data.MsgRequestRepository;
import com.haiercash.common.data.UAuthCASignRequestRepository;
import com.haiercash.common.data.UAuthUserTokenRepository;
import com.haiercash.common.util.ThreadLocalFactory;
import org.apache.commons.collections.map.HashedMap;
import org.apache.http.client.utils.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
public class AppOrderServiceTestImpl implements AppOrderServiceTest {

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
    private AppManageService appManageService;

    @Autowired
    private MsgRequestRepository msgRequestRepository;

    @Autowired
    private AcquirerService acquirerService;

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

    @Override
    @Test
    public void saveAppOrder() {

    }

    @Test
    public void contractConfirmTest(){
        Map<String, Object> map = appOrderService.getContractConfirmData("a6235a51c09743dbbf94251352a15972");
        System.out.print(map.get("loanpayment"));
    }

    @Test
    public void testtest(){
        List<MsgRequest> byApplSeq = msgRequestRepository.findByApplSeq("74827321");
        System.out.print(byApplSeq);
    }

    @Override
    @Test
    public void checkIfMsgComplete() throws Exception {
        Map<String, Object> params = new HashedMap();
        params.put("isOrder", "N");
        AppOrder appOrder = appOrderRepository.findByApplseq("1098776");
        if (appOrder == null){
            return;
        }
        params.put("orderNo", appOrder.getOrderNo());
        params.put("custNo", appOrder.getCustNo());
        params.put("applSeq", appOrder.getApplSeq());
        params.put("custName", appOrder.getCustName());
        params.put("typCde", appOrder.getTypCde());
        params.put("userId", "18104686333");
        params.put("idNo", appOrder.getIdNo());
        Map<String, Object> result =  appOrderService.checkIfMsgComplete("A", BusinessType.EDSQ.toString(), params);
        System.out.println(result);
        Assert.assertNotNull(result);
        result =  appOrderService.checkIfMsgComplete("SHH", BusinessType.TE.toString(), params);
        System.out.println(result);
        Assert.assertNotNull(result);
        result =  appOrderService.checkIfMsgComplete("B", BusinessType.SPFQ.toString(), params);
        System.out.println(result);
        Assert.assertNotNull(result);
        result =  appOrderService.checkIfMsgComplete("A", BusinessType.GRXX.toString(), params);
        System.out.println(result);
        Assert.assertNotNull(result);

    }

    @Test
    public void checkIfMs() {
        //String order= fileSignService.signAgreementByCmis("1145354","DOUZIPERSONAL","0", false, "978e2f48e02746d0bdc724f311ca3074");
        String order= fileSignService.signAgreementByCmis("1255756","HCFC-JK-YK-V2.0","0", false, "15d7f7b390eb4e0aa92bfa880daf8c50");
    }

    /**
     * 测试删除共同还款人替代影像信息.
     */
    @Test
    public void deleteReplaceImages() {
        attachService.deleteCommonReplaceImage("1254965", "C201612020510001406010", "16129a");
    }

    @Test
    public void updateDeleteCardToEmpty(){
        appOrderService.updateDeleteCardToEmpty("6227002394040579334");
        List<AppOrder> list = appOrderRepository.findOrdersByRepayApplCardNo("6227002394040579334");
        System.out.print("========="+list.size());
    }

    @Test
    public void testXcdLoan(){
//        cmisApplService.xcdLoan("141024197902120031","27");

    }

    @Test
    public void testGetSaleMsg(){
        Map<String, Object> saleMsg = appManageService.getSaleMsg("16115a");
        System.out.println(saleMsg);
        Map<String,Object> map = new HashMap<>();
        map.put("11","22");
        appManageService.putSaleMsgIntoMap("16115a", map);
        System.out.println(map);
    }

    @Test
    public void testJson(){
        Map<String, Object> stringObjectMap = cASignService.checkCaFourKeysInfo("9781747a8de74de28f8349f53c6eb562", null);
        System.out.println(stringObjectMap);
    }

    @Test
    public void testPutCooprToMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("11","22");
        appManageService.putCooprSettingToMap(map, "17");
        System.out.println(map);
        JSONArray jsonArray = new JSONArray();
        List<JSONObject> list = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++){
            list.add((JSONObject) jsonArray.get(i));
        }
        String channel = "16";
        JSONObject jsonObject = list.stream().filter(jo -> jo.get("channel").equals(channel)).findFirst().get();
    }

    @Test
    public void makeJson() {
        File file = new File("F:\\Downloads\\现有产品划分2.csv");
        FileReader reader = null;
        BufferedReader br = null;
        try {
            reader = new FileReader(file);
            br = new BufferedReader(reader);
            String s = br.readLine();
            while (s != null) {
                String[] split = s.split(",");
                StringBuffer sb = new StringBuffer();
                sb.append("INSERT INTO \"APPSERVER\".\"APP_DICT\" VALUES ('")
                        .append(UUID.randomUUID().toString().replace("-", ""))
                        .append("', 'sale_")
                        .append(split[0].trim())
                        .append("', '")
                        .append(split[0].trim())
                        .append("销售信息', '[{\"saler_cde\":\"")
                        .append(split[11].trim())
                        .append("\"},{\"saler_name\":\"")
                        .append(split[8].trim())
                        .append("\"},{\"saler_mobile\":\"")
                        .append(split[12].trim())
                        .append("\"},{\"coopr_name\":\"")
                        .append(split[6].trim())
                        .append("\"},{\"coopr_cde\":\"")
                        .append(split[7].trim())
                        .append("\"},{\"operator_name\":\"")
                        .append(split[4].trim())
                        .append("\"},{\"operator_cde\":\"")
                        .append(split[9].trim())
                        .append("\"},{\"operator_tel\":\"")
                        .append(split[10].trim())
                        .append("\"}]', null, null, 'sale_msg', '9e589c213db24b56be3ced52bb7cae32', '0', null);");
                System.out.println(sb);

                s = br.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Test
    public void test() {
        Instant now = Instant.now();
        System.out.println(now);
        String time = DateUtils.formatDate(new Date(), "yyyy-MM-dd");
        System.out.println(time);
    }

    @Test
    public void testFormat() {
        AppOrder appOrder = new AppOrder();
        appOrder.setApplCde("213123");
        appOrder.setChannelNo("24");
        appOrder.setAccBankName("节能隔热饿哦gore");
        appOrder.setEmail("ihofjoeWjofe");
        Map<String, Object> stringObjectMap = FormatUtil.obj2Map(appOrder);
        System.out.println(stringObjectMap);
        Map<String, Object> stringObjectMap1 = FormatUtil.obj2UnderScoreMap(appOrder);
        System.out.println(stringObjectMap1);
    }

    @Test
    public void outputEnum() {
        File srcFile = new File("F:\\Downloads\\appt.csv");
        File outPutFile = new File("F:\\Downloads\\apptoutput");

        FileReader reader = null;
        BufferedReader br = null;
        FileWriter writer = null;
        BufferedWriter bw = null;
        try {
            reader = new FileReader(srcFile);
            br = new BufferedReader(reader);
            writer = new FileWriter(outPutFile);
            bw = new BufferedWriter(writer);
            String line = br.readLine();
            while (line != null) {
                String[] split = line.split(",");
                String zhuShi = split[0];
                String underScoreName = split[1];
                String camelName = underScoreToCamelCase(underScoreName);
                StringBuffer sb = new StringBuffer();
                sb.append(underScoreName.toUpperCase()).append("(\"")
                        .append(camelName).append("\",\"").append(underScoreName)
                        .append("\"),").append("//").append(zhuShi);
                String newLine = sb.toString();
                System.out.println(newLine);
                bw.write(newLine);
                bw.newLine();
                line = br.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public String underScoreToCamelCase(String underScore) {
        char[] chars = underScore.toCharArray();
        List<Character> resultList = new ArrayList<>();
        for (int i = 0; i < chars.length; i++) {
            if ('_' == (chars[i])) {
                i++;
                resultList.add(Character.toUpperCase(chars[i]));
            } else {
                resultList.add(chars[i]);
            }
        }
        StringBuffer sb = new StringBuffer();
        resultList.stream().forEach(ch -> sb.append(ch));
        return sb.toString();
    }


    @Test
    public void testGetAppOrderFromAcq() {
        ThreadLocal threadLocal = ThreadLocalFactory.getThreadLocal();
        threadLocal.remove();
        String channel = "14";
        String channelNO = "34";
        Map<String, Object> threadMap = new HashMap<>();
        threadMap.put("channel", channel);
        threadMap.put("channelNo", channelNO);
        threadLocal.set(threadMap);
        AppOrder appOrder = acquirerService.getAppOrderFromAcquirer("1261042", "34");
        System.out.println(appOrder);
    }

}
