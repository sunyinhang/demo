package com.haiercash.appserver.apporder;

import com.haiercash.appserver.service.AttachService;
import com.haiercash.appserver.service.CommonRepaymentPersonService;
import com.haiercash.appserver.util.ConstUtil;
import com.haiercash.appserver.util.sign.FileSignUtil;
import com.haiercash.appserver.web.BaseController;
import com.haiercash.common.data.AppOrdernoTypgrpRelation;
import com.haiercash.common.data.AppOrdernoTypgrpRelationRepository;
import com.haiercash.common.data.CommonRepaymentPerson;
import com.haiercash.common.data.CommonRepaymentPersonRepository;
import com.haiercash.commons.util.RestUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CommonRepaymentPersonController extends BaseController {

    private static String MODULE_NO = "11";

    public CommonRepaymentPersonController() {
        super(MODULE_NO);
    }

    @Autowired
    CommonRepaymentPersonService commonRepaymentPersonService;
    @Autowired
    AttachService attachService;
    @Autowired
    AppOrdernoTypgrpRelationRepository appOrdernoTypgrpRelationRepository;

    /**
     * 新增共同还款人
     */
    @RequestMapping(value = "/app/appserver/apporder/addCommonRepaymentPerson", method = RequestMethod.POST)
    public Map<String, Object> addCommonRepaymentPerson(@RequestBody CommonRepaymentPerson commonRepaymentPerson, String version) {
        try {
            //身份证号将小写变成大写
            String idNo = commonRepaymentPerson.getIdNo();
            if (!StringUtils.isEmpty(idNo)) {
                commonRepaymentPerson.setIdNo(idNo.toUpperCase());
            }
            return commonRepaymentPersonService.addCommonRepaymentPerson(commonRepaymentPerson, "1");
        } catch (Exception e) {
            logger.error("添加共同还款人发生异常：" + e.getMessage());
            return fail(RestUtil.ERROR_INTERNAL_CODE, RestUtil.ERROR_INTERNAL_MSG);
        }
    }

    /**
     * 修改共同还款人
     */
    @RequestMapping(value = "/app/appserver/apporder/updateCommonRepaymentPerson", method = RequestMethod.POST)
    public Map<String, Object> updateCommonRepaymentPerson(@RequestBody CommonRepaymentPerson commonRepaymentPerson) {
        return commonRepaymentPersonService.updateCommonRepaymentPerson(commonRepaymentPerson);
    }

    /**
     * 删除共同还款人
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/deleteCommonRepaymentPerson", method = RequestMethod.POST)
    public Map<String, Object> deleteCommonRepaymentPerson(@RequestBody Map<String, Object> map) {
        if (!map.containsKey("id")) {
            return fail("99", "服务器未接收到必传参数，删除失败！");
        }
        String id = (String) map.get("id");
        return commonRepaymentPersonService.deleteCommonRepaymentPerson(id);
    }

    /**
     * 根据id查询共同还款人
     *
     * @param id
     * @return
     */
    @Deprecated
    @RequestMapping(value = "/app/appserver/apporder/getCommonRepaymentPerson", method = RequestMethod.GET)
    public Map<String, Object> getCommonRepaymentPerson(@RequestParam(value = "id") String id) {
        return fail(RestUtil.ERROR_INTERNAL_CODE, "该接口已废弃");
    }

    /**
     * 查询共同还款人的数量
     *
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "/app/appserver/apporder/countCommonRepaymentPerson", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> countCommonRepaymentPerson(@RequestParam(value = "orderNo") String orderNo) {
        if (StringUtils.isEmpty(orderNo)) {
            return fail(ConstUtil.ERROR_PARAM_INVALID_CODE, "orderNo不能为空");
        }
        AppOrdernoTypgrpRelation relation = appOrdernoTypgrpRelationRepository.findOne(orderNo);
        if (relation == null) {
            return fail("36", "订单不存在");
        }
        return commonRepaymentPersonService.countCommonRepaymentPerson(relation.getApplSeq());
    }

    /**
     * 根据订单编号查询多个共同还款人 orderNo
     */
    @RequestMapping(value = "/app/appserver/apporder/getCommonRepaymentPersonList", method = RequestMethod.GET)
    public Map<String, Object> getCommonRepaymentPersonList(String applSeq) {
        if (StringUtils.isEmpty(applSeq)) {
            return fail("89", "请求参数订单编号不可为空！");
        }
        try {
            Map<String, Object> resultMap = commonRepaymentPersonService.getCommonRepaymentPerson(applSeq);
            return resultMap;
        } catch (RuntimeException e) {
            logger.error("删除共同还款人失败！" + e.getMessage());
            return fail("90", e.getMessage());
        }
    }

}
