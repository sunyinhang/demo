package com.haiercash.acquirer.server;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Administrator on 2017/6/28.
 */
public class ChannelNOTest {

    @Value("${common.other.REPEAT_INCOMING_CHECK}")
    private  String repeatIncomingCheck;   //允许APP重复进件
    @Value("${common.other.VISIT_ORDER_CHANNELNO}")
    private String visitOrderSysChannelNo;

    @Test
    public void main2(){
        String[] channelNos = String.valueOf(visitOrderSysChannelNo).replaceAll(" ", "").split(",");
        List<String> channelNoList = new ArrayList<String>(Arrays.asList(channelNos));
        System.out.println(channelNoList);
    }

    @Test
    public  void main(){

        String channelNo = "13";
        String[] channelNos = String.valueOf(repeatIncomingCheck).replaceAll(" ", "").split(",");
        List<String> channelNoList = new ArrayList<String>(Arrays.asList(channelNos));
        System.out.println(channelNoList);
        if(channelNoList.contains(channelNo)){
            System.out.println("13,14,16,18进行重复校验");
        }else{
            System.out.println("其他不进行重复校验");
        }

    }
}
