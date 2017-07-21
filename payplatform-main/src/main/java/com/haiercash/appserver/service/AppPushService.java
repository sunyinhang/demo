package com.haiercash.appserver.service;

import com.haiercash.appserver.util.push.Message;
import com.haiercash.appserver.util.push.MessageIOS;
import com.haiercash.appserver.util.push.PushUtil;
import com.haiercash.appserver.util.push.TimeInterval;
import com.haiercash.appserver.util.push.XingeApp;
import com.haiercash.common.data.AppPush;
import com.haiercash.common.data.AppPushRepository;
import com.haiercash.commons.util.HttpUtil;
import com.haiercash.commons.util.RedisUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.haiercash.appserver.web.AppPushController.getRedisXingeKey;

/**
 * 消息推送(包括单个token、批量token、单个账号、批量账号推送)
 */
@Service
public class AppPushService {
    private Log logger = LogFactory.getLog(this.getClass());
    @Autowired
    private AppPushRepository appPushRepository;

    /**
     * 单个通知token推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param token
     * @param custom
     * @return
     */
    public String singleTokenPush(String userId,String phoneType,Map<String,Object> map, String title,String message,String token,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, "", map, title, message, "01", token);// 01:singleNotice,02:singleMessage,03:multiNotice,04:multiMessage
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("And".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_NOTIFICATION, custom);// 填充Message类
                resultAndroid = AndroidSingleTokenPush(token, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                resultIOS = IOSSingleTokenPush(token, msgIOS);// 推送
            }
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return  (String) map.get("err_msg");
    }

    /**
     * 单个token消息推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param token
     * @param custom
     * @return
     */
    public String singleTokenMsgPush(String userId,String phoneType,Map<String,Object> map, String title,String message,String token,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, "", map, title, message, "02", token);// 01:singleNotice,
            // 02:singleMessage,
            // 03:multiNotice,
            // 04:multiMessage
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("And".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_MESSAGE, custom);// 填充Message类
                resultAndroid = AndroidSingleTokenPush(token, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                resultIOS = IOSSingleTokenPush(token, msgIOS); // 推送
            }
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return  (String) map.get("err_msg");
    }

    /**
     * 批量token通知推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param tokens
     * @param custom
     * @return
     */
    public String multiNoticeTokensPush(String userId,String phoneType,Map<String,Object> map, String title,String message,String tokens,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, "", map, title, message, "03", tokens);// 01:singleNotice,
            // 02:singleMessage,
            // 03:multiNotice,
            // 04:multiMessage
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("And".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_NOTIFICATION, custom);// 填充Message类
                resultAndroid = AndroidmultiTokensPush(tokens, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                resultIOS = IOSmultiTokensPush(tokens, msgIOS); // 推送
            }
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return  (String) map.get("err_msg");
    }

    /**
     * 批量tokens消息推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param tokens
     * @param custom
     * @return
     */
    public String multiTokensMsgPush(String userId,String phoneType,Map<String,Object> map, String title,String message,String tokens,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, "", map, title, message, "04", tokens);
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("And".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_MESSAGE, custom);// 填充Message类
                resultAndroid = AndroidmultiTokensPush(tokens, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                resultIOS = IOSmultiTokensPush(tokens, msgIOS); // 推送
            }
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return (String) map.get("err_msg");
    }

    /**
     * 单个账号通知推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param account
     * @param custom
     * @return
     */
    public String singleNoticePush(String userId,String phoneType,Map<String,Object> map, String title,String message,String account,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, account, map, title, message, "01", "");// 01:singleNotice,
            // 02:singleMessage,
            // 03:multiNotice,
            // 04:multiMessage
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("AND".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_NOTIFICATION, custom);// 填充Message类
                resultAndroid = AndroidSinglePush(account, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                logger.debug("--------填充完毕--------");
                resultIOS = IOSSinglePush(account, msgIOS); // 推送
            }
            logger.debug("-------单个账号推送-----------resultAndroid=" + resultAndroid + " ,resultIOS=" + resultIOS);
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return (String) map.get("err_msg");
    }

    /**
     * 单个账号消息推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param account
     * @param custom
     * @return
     */
    public String singleMsgPush(String userId,String phoneType,Map<String,Object> map, String title,String message,String account,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, account, map, title, message, "02", "");// 01:singleNotice,
            // 02:singleMessage,
            // 03:multiNotice,
            // 04:multiMessage
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("And".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_MESSAGE, custom);// 填充Message类
                resultAndroid = AndroidSinglePush(account, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                resultIOS = IOSSinglePush(account, msgIOS); // 推送
            }
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return  (String) map.get("err_msg");
    }

    /**
     * 批量账号通知推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param accounts
     * @param custom
     * @return
     */
    public String multiNoticePush(String userId,String phoneType,Map<String,Object> map, String title,String message,String accounts,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, accounts, map, title, message, "03", "");// 01:singleNotice,
            // 02:singleMessage,
            // 03:multiNotice,
            // 04:multiMessage
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("Android".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_NOTIFICATION, custom);// 填充Message类
                resultAndroid = AndroidmultiPush(accounts, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                resultIOS = IOSmultiPush(accounts, msgIOS); // 推送
            }
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return  (String) map.get("err_msg");
    }

    /**
     * 批量账号消息推送接口
     * @param userId
     * @param phoneType
     * @param map
     * @param title
     * @param message
     * @param accounts
     * @param custom
     * @return
     */
    public String multiMsgPush(String userId,String phoneType,Map<String,Object> map, String title,String message,String accounts,Map<String, Object> custom){
        if ("0".equals(map.get("result"))) {
            // 1.存储
            AppPush appPush = savePushMsg(userId, phoneType, accounts, map, title, message, "04", "");
            // 2.推送
            String resultAndroid = "", resultIOS = "";
            if ("Android".equals(phoneType)) {
                Message msg = FillMessage(title, message, map, Message.TYPE_MESSAGE, custom);// 填充Message类
                resultAndroid = AndroidmultiPush(accounts, msg); // 推送
            } else if ("IOS".equals(phoneType)) {
                MessageIOS msgIOS = FillMessageIOS(message, map, custom);// 填充MessageIOS类
                resultIOS = IOSmultiPush(accounts, msgIOS); // 推送
            }
            return checkResult(resultAndroid, resultIOS, appPush);
        }
        return  (String) map.get("err_msg");
    }

    /**
     * 安卓通知单个token推送
     */
    public String AndroidSingleTokenPush(String token, Message message) {
        XingeApp push = new XingeApp(PushUtil.ACCESSID_ANDROID_USER, PushUtil.SECRETKEY_ANDROID_USER);
        JSONObject ret = push.pushSingleDevice(token, message);
        if (0 == (Integer) ret.get("ret_code")) {
            return "success";
        }
        return (String) ret.get("err_msg");
    }

    /**
     * IOS通知单个token推送
     */
    public String IOSSingleTokenPush(String token, MessageIOS msgIOS) {
        XingeApp push = new XingeApp(PushUtil.ACCESSID_IOS_USER, PushUtil.SECRETKEY_IOS_USER);
        JSONObject ret = push.pushSingleDevice(token, msgIOS, XingeApp.IOSENV_DEV);
        if (0 == (Integer) ret.get("ret_code")) {
            return "success";
        }
        return (String) ret.get("err_msg");
    }

    /**
     * 安卓系统批量tokens推送至信鸽
     * @param tokens
     * @param message
     * @return
     */
    public String AndroidmultiTokensPush(String tokens, Message message) {
        XingeApp push = new XingeApp(PushUtil.ACCESSID_ANDROID_USER, PushUtil.SECRETKEY_ANDROID_USER);
        String[] strTokens = tokens.split(",");
        List<String> tokenList = new ArrayList<>();

        if (strTokens.length <= 1000) {// token的数目<=1000
            for (String str : strTokens) {
                tokenList.add(str);
            }
            String result = bigAndroidMultiTokensPush(push, message, tokenList);
            return result;
        } else {// token的数目>1000
            int len = strTokens.length;
            int num = len / 1000;
            int restNum = len % 1000;
            int listNum = num + (restNum == 0 ? 0 : 1);
            String result = "";
            for (int i = 1; i <= listNum; i++) {
                List<String> tokenListPart = new ArrayList<>();
                for (int j = (i - 1) * 1000; j < i * 1000; j++) {
                    tokenListPart.add(strTokens[j]);
                }
                result = bigAndroidMultiTokensPush(push, message, tokenListPart);
                if (!"success".equals(result)) {
                    return result;
                }
                tokenListPart.clear();
            }
            return result;
        }
    }

    /**
     * 安卓创建大批量token推送消息
     *
     * @return
     */
    public String bigAndroidMultiTokensPush(XingeApp push, Message message, List<String> tokenList) {
        JSONObject ret_create = push.createMultipush(message); // 创建大批量推送消息
        if (0 == (Integer) ret_create.get("ret_code")) {
            Map<String, Object> map = HttpUtil.json2Map(ret_create.get("result").toString());
            int push_id = Integer.parseInt((String) map.get("push_id"));
            JSONObject ret = push.pushDeviceListMultiple(push_id, tokenList);// 推送
            if (0 == (Integer) ret.get("ret_code")) {
                return "success";
            }
            return (String) ret.get("err_msg");
        }
        return (String) ret_create.get("err_msg");
    }

    /**
     * IOS批量tokens推送至信鸽
     *
     * @param tokens
     * @param messageIOS
     * @return
     */
    public String IOSmultiTokensPush(String tokens, MessageIOS messageIOS) {
        XingeApp push = new XingeApp(PushUtil.ACCESSID_IOS_USER, PushUtil.SECRETKEY_IOS_USER);
        String[] strTokens = tokens.split(",");
        List<String> tokenList = new ArrayList<>();

        if (strTokens.length <= 1000) {// 账号的数目<=1000
            for (String str : strTokens) {
                tokenList.add(str);
            }
            String result = bigIOSMultiPush(push, messageIOS, tokenList);
            return result;
        } else {// 账号的数目>1000
            int len = strTokens.length;
            int num = len / 1000;
            int restNum = len % 1000;
            int listNum = num + (restNum == 0 ? 0 : 1);
            String result = "";
            for (int i = 1; i <= listNum; i++) {
                List<String> tokenListPart = new ArrayList<>();
                for (int j = (i - 1) * 1000; j < i * 1000; j++) {
                    tokenListPart.add(strTokens[j]);
                }
                result = bigIOSMultiTokensPush(push, messageIOS, tokenListPart);
                if (!"success".equals(result)) {
                    return result;
                }
                tokenListPart.clear();
            }
            return result;
        }
    }

    /**
     * IOS创建大批量tokens推送消息
     *
     * @return
     */
    public String bigIOSMultiTokensPush(XingeApp push, MessageIOS messageIOS, List<String> tokenList) {
        JSONObject ret_create = push.createMultipush(messageIOS, XingeApp.IOSENV_DEV); // 创建大批量推送消息
        if (0 == (Integer) ret_create.get("ret_code")) {
            Map<String, Object> map = HttpUtil.json2Map(ret_create.get("result").toString());
            int push_id = Integer.parseInt(map.get("push_id").toString());
            JSONObject ret = push.pushDeviceListMultiple(push_id, tokenList);// 推送
            if (0 == (Integer) ret.get("ret_code")) {
                return "success";
            }
            return (String) ret.get("err_msg");
        }
        return (String) ret_create.get("err_msg");
    }

    /**
     * 存储消息
     */
    public AppPush savePushMsg(String userId, String phoneType, String accounts, Map<String, Object> map, String title,
                               String message, String pushType, String tokens) {
        AppPush appPush = new AppPush();
        appPush.setId(UUID.randomUUID().toString());
        appPush.setUserId(userId);
        appPush.setPhoneType(phoneType);
        appPush.setPushType(pushType);
        if (!"".equals(accounts) && null != accounts) {
            appPush.setAccounts(accounts);
        }
        appPush.setState("01"); // 01:推送中 02:推送成功 03:推送失败
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        appPush.setSubmitTime(format.format(new Date()));
        TimeInterval t = (TimeInterval) map.get("acceptTime");
        appPush.setStartTime(t.getM_startHour() + ":" + t.getM_startMin());
        appPush.setEndTime(t.getM_endHour() + ":" + t.getM_endMin());
        appPush.setTitle(title);
        appPush.setMessage(message);
        appPush.setTimes(1);
        if (!"".equals(tokens) && null != tokens) {
            appPush.setTokens(tokens);
        }

        return appPushRepository.save(appPush);
    }

    /**
     * 填充Message
     *
     * @return
     */
    public Message FillMessage(String title, String message, Map<String, Object> map, int type,
                               Map<String, Object> custom) {
        Message msg = new Message();
        msg.setTitle(title);
        msg.setContent(message);
        msg.setExpireTime(0); // 默认最长存储时间3天
        msg.addAcceptTime((TimeInterval) map.get("acceptTime"));
        msg.setType(type); // 通知或消息
        msg.setCustom(custom);

        return msg;
    }

    /**
     * 填充MessageIOS
     *
     * @return
     */
    public MessageIOS FillMessageIOS(String message, Map<String, Object> map, Map<String, Object> custom) {
        // MessageIOS msgIOS = new MessageIOS();
        // msgIOS.setExpireTime(0); // 默认最长存储时间3天
        // msgIOS.addAcceptTime((TimeInterval) map.get("acceptTime"));
        // JSONObject obj = new JSONObject();
        // JSONObject aps = new JSONObject();
        // aps.put("sound", "default");
        // aps.put("alert", message);
        // aps.put("badge", 1);
        // aps.put("content-available", 1);
        // obj.put("aps", aps);
        // msgIOS.setRaw(obj.toString());
        // msgIOS.setCustom(custom);
        // return msgIOS;
        logger.debug("custom=" + custom.toString());
        MessageIOS msgIOS = new MessageIOS();
        msgIOS.setExpireTime(0); // 默认最长存储时间3天
        msgIOS.addAcceptTime((TimeInterval) map.get("acceptTime"));
        msgIOS.setCustom(custom);
        JSONObject obj = new JSONObject();
        JSONObject aps = new JSONObject();
        aps.put("sound", "default");
        aps.put("alert", message);
        aps.put("badge", 1);
        aps.put("content-available", 1);
        aps.put("applseq", (String) custom.get("applSeq"));
        aps.put("typGrp", (String) custom.get("typGrp"));
        obj.put("aps", aps);
        msgIOS.setRaw(obj.toString());

        return msgIOS;
    }

    /**
     * 安卓系统单个账号推送至信鸽
     *
     * @param account
     * @param message
     * @return
     */
    public String AndroidSinglePush(String account, Message message) {

        XingeApp push = new XingeApp(2100196085, "cb13b89ca36f73c268c7e0c4192f3ba4");
        logger.debug("account=" + account);
        JSONObject ret = push.pushSingleAccount(0, account, message);
        if (0 == (Integer) ret.get("ret_code")) {
            return "success";
        }
        return (String) ret.get("err_msg");
    }

    /**
     * 安卓系统批量账号推送至信鸽
     *
     * @param accounts
     * @param message
     * @return
     */
    public String AndroidmultiPush(String accounts, Message message) {
        XingeApp push = new XingeApp(PushUtil.ACCESSID_ANDROID_USER, PushUtil.SECRETKEY_ANDROID_USER);
        JSONObject ret = null;
        String[] strAccounts = accounts.split(",");
        List<String> accountList = new ArrayList<String>();

        if (strAccounts.length <= 1000) {// 账号的数目<=1000
            for (String str : strAccounts) {
                accountList.add(str);
            }
            if (accountList.size() <= 100) {// 账号的数目<=100
                ret = push.pushAccountList(0, accountList, message);
                logger.debug("AppPushController,ret_code=" + ret.get("ret_code"));
            } else { // 100 < 账号的数目 <= 1000
                String result = bigAndroidMultiPush(push, message, accountList);
                return result;
            }
        } else {// 账号的数目>1000
            int len = strAccounts.length;
            int num = len / 1000;
            int restNum = len % 1000;
            int listNum = num + (restNum == 0 ? 0 : 1);
            String result = "";
            for (int i = 1; i <= listNum; i++) {
                List<String> accountListPart = new ArrayList<>();
                for (int j = (i - 1) * 1000; j < i * 1000; j++) {
                    accountListPart.add(strAccounts[j]);
                }
                result = bigAndroidMultiPush(push, message, accountListPart);
                if (!"success".equals(result)) {
                    return result;
                }
                accountListPart.clear();
            }
            return result;
        }
        if (0 == (Integer) ret.get("ret_code")) {
            return "success";
        }
        return (String) ret.get("err_msg");
    }

    /**
     * 安卓创建大批量推送消息
     *
     * @return
     */
    public String bigAndroidMultiPush(XingeApp push, Message message, List<String> accountList) {
        JSONObject ret_create = push.createMultipush(message); // 创建大批量推送消息
        return multiplePush(push, ret_create, accountList);
    }

    /**
     * IOS系统单个账号通知推送值信鸽
     *
     * @param account
     * @param messageIOS
     * @return
     */
    public String IOSSinglePush(String account, MessageIOS messageIOS) {
        XingeApp push = new XingeApp(PushUtil.ACCESSID_IOS_USER, PushUtil.SECRETKEY_IOS_USER);
        JSONObject ret = push.pushSingleAccount(0, account, messageIOS, XingeApp.IOSENV_DEV);
        if (0 == (Integer) ret.get("ret_code")) {
            return "success";
        }
        logger.debug("-----------单个账号IOS推送ret_code=" + ret.get("ret_code"));
        return (String) ret.get("err_msg");
    }

    /**
     * IOS批量账号推送至信鸽
     *
     * @param accounts
     * @param messageIOS
     * @return
     */
    public String IOSmultiPush(String accounts, MessageIOS messageIOS) {
        XingeApp push = new XingeApp(PushUtil.ACCESSID_IOS_USER, PushUtil.SECRETKEY_IOS_USER);
        JSONObject ret = null;
        String[] strAccounts = accounts.split(",");
        List<String> accountList = new ArrayList<>();

        if (strAccounts.length <= 1000) {// 账号的数目<=1000
            for (String str : strAccounts) {
                accountList.add(str);
            }
            if (accountList.size() <= 100) {// 账号的数目<=100
                ret = push.pushAccountList(0, accountList, messageIOS, XingeApp.IOSENV_DEV);
            } else { // 100 < 账号的数目 <= 1000
                String result = bigIOSMultiPush(push, messageIOS, accountList);
                return result;
            }
        } else {// 账号的数目>1000
            int len = strAccounts.length;
            int num = len / 1000;
            int restNum = len % 1000;
            int listNum = num + (restNum == 0 ? 0 : 1);
            String result = "";
            for (int i = 1; i <= listNum; i++) {
                List<String> accountListPart = new ArrayList<>();
                for (int j = (i - 1) * 1000; j < i * 1000; j++) {
                    accountListPart.add(strAccounts[j]);
                }
                result = bigIOSMultiPush(push, messageIOS, accountListPart);
                if (!"success".equals(result)) {
                    return result;
                }
                accountListPart.clear();
            }
            return result;
        }
        if (0 == (Integer) ret.get("ret_code")) {
            return "success";
        }
        return (String) ret.get("err_msg");
    }

    /**
     * IOS创建大批量推送消息
     *
     * @return
     */
    public String bigIOSMultiPush(XingeApp push, MessageIOS messageIOS, List<String> accountList) {
        JSONObject ret_create = push.createMultipush(messageIOS, XingeApp.IOSENV_DEV); // 创建大批量推送消息
        return multiplePush(push, ret_create, accountList);
    }

    /**
     * 批量推送公共方法
     * @param push
     * @param ret_create
     * @param accountList
     * @return
     */
    private String multiplePush(XingeApp push, JSONObject ret_create, List<String> accountList){
        if (0 == (Integer) ret_create.get("ret_code")) {
            Map<String, Object> map = HttpUtil.json2Map(ret_create.get("result").toString());
            int push_id = Integer.parseInt(map.get("push_id").toString());
            JSONObject ret = push.pushAccountListMultiple(push_id, accountList);// 推送
            if (0 == (Integer) ret.get("ret_code")) {
                return "success";
            }
            return (String) ret.get("err_msg");
        }else{
            return (String) ret_create.get("err_msg");
        }
    }
    /**
     * 参数判断
     * @param params
     * @return
     */
    public Map<String, Object> validateParams(Map<String, Object> params) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (StringUtils.isEmpty((String) params.get("userId"))) {
            resultMap.put("code", "01");
            resultMap.put("msg", "用户id不能为空!");
            return resultMap;
        }
        if (StringUtils.isEmpty((String) params.get("phoneType"))) {
            resultMap.put("code", "01");
            resultMap.put("msg", "手机系统类型不能为空!");
            return resultMap;
        }
        if (StringUtils.isEmpty((String) params.get("message"))) {
            resultMap.put("code", "01");
            resultMap.put("msg", "发送内容不能为空!");
            return resultMap;
        }
        if ("singleToken".equals((String) params.get("type"))) {
            if (StringUtils.isEmpty((String) params.get("token"))) {
                resultMap.put("code", "01");
                resultMap.put("msg", "token不能为空!");
                return resultMap;
            }
        }
        if ("multiToken".equals((String) params.get("type"))) {
            if (StringUtils.isEmpty((String) params.get("tokens"))) {
                resultMap.put("code", "01");
                resultMap.put("msg", "tokens不能为空!");
                return resultMap;
            }
        }
        if ("singleAccount".equals((String) params.get("type"))) {
            if (StringUtils.isEmpty((String) params.get("account"))) {
                resultMap.put("code", "01");
                resultMap.put("msg", "账号不能为空!");
                return resultMap;
            }
        }
        if ("multiAccount".equals((String) params.get("type"))) {
            if (StringUtils.isEmpty((String) params.get("accounts"))) {
                resultMap.put("code", "01");
                resultMap.put("msg", "账号不能为空!");
                return resultMap;
            }
        }
        resultMap.put("code", "00");
        resultMap.put("msg", "验证通过!");
        return resultMap;
    }

    /**
     * 判断推送返回结果
     * @param resultAndroid
     * @param resultIOS
     * @param appPush
     * @return
     */
    public String checkResult(String resultAndroid, String resultIOS, AppPush appPush){
        if ((!"success".equals(resultAndroid) && !"".equals(resultAndroid)) || (!"success".equals(resultIOS) && !"".equals(resultIOS))) {
            // 推送信息保存到redis队列左侧
            RedisUtil.lpush(getRedisXingeKey(), appPush.getId());
            logger.debug("推送信息失败，把任务放回队列左侧: " + appPush.getId());
            return resultAndroid + " " + resultIOS;
        } else if ("success".equals(resultAndroid) || "success".equals(resultIOS)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            appPushRepository.updatePushInfo(appPush.getId(), format.format(new Date()), "02");
            return "00000";
        } else if ("".equals(resultAndroid) && "".equals(resultIOS)) {
            // 推送信息保存到redis队列左侧
            RedisUtil.lpush(getRedisXingeKey(), appPush.getId());
            logger.debug("手机类型错误!");
            return "手机类型错误!";
        }
        return "未知错误!";
    }


}
