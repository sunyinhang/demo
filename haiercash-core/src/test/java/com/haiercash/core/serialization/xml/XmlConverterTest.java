package com.haiercash.core.serialization.xml;

import com.bestvike.linq.Linq;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.haiercash.core.serialization.JsonSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by 许崇雷 on 2018-02-02.
 */
public class XmlConverterTest {
    @Test
    public void deserialize() {
        String string = "<?xml version=\"1.0\" encoding=\"gb2312\" ?><msgall><errorCode>00000</errorCode><errorMsg>success</errorMsg><LmPmShdList><MX><PS_PERD_NO>0</PS_PERD_NO><PS_DUE_DT>2017-12-21</PS_DUE_DT><PS_PRCP_AMT>0.0</PS_PRCP_AMT><PS_NORM_INT>0.0</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>1000.0</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>0.0</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>1</PS_PERD_NO><PS_DUE_DT>2018-01-12</PS_DUE_DT><PS_PRCP_AMT>79.3</PS_PRCP_AMT><PS_NORM_INT>17.6</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>920.7</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>2</PS_PERD_NO><PS_DUE_DT>2018-02-12</PS_DUE_DT><PS_PRCP_AMT>74.07</PS_PRCP_AMT><PS_NORM_INT>22.83</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>846.63</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>3</PS_PERD_NO><PS_DUE_DT>2018-03-12</PS_DUE_DT><PS_PRCP_AMT>77.94</PS_PRCP_AMT><PS_NORM_INT>18.96</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>768.69</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>4</PS_PERD_NO><PS_DUE_DT>2018-04-12</PS_DUE_DT><PS_PRCP_AMT>77.84</PS_PRCP_AMT><PS_NORM_INT>19.06</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>690.85</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>5</PS_PERD_NO><PS_DUE_DT>2018-05-12</PS_DUE_DT><PS_PRCP_AMT>80.32</PS_PRCP_AMT><PS_NORM_INT>16.58</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>610.53</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>6</PS_PERD_NO><PS_DUE_DT>2018-06-12</PS_DUE_DT><PS_PRCP_AMT>81.76</PS_PRCP_AMT><PS_NORM_INT>15.14</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>528.77</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>7</PS_PERD_NO><PS_DUE_DT>2018-07-12</PS_DUE_DT><PS_PRCP_AMT>84.21</PS_PRCP_AMT><PS_NORM_INT>12.69</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>444.56</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>8</PS_PERD_NO><PS_DUE_DT>2018-08-12</PS_DUE_DT><PS_PRCP_AMT>85.87</PS_PRCP_AMT><PS_NORM_INT>11.03</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>358.69</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>9</PS_PERD_NO><PS_DUE_DT>2018-09-12</PS_DUE_DT><PS_PRCP_AMT>88.0</PS_PRCP_AMT><PS_NORM_INT>8.9</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>270.69</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>10</PS_PERD_NO><PS_DUE_DT>2018-10-12</PS_DUE_DT><PS_PRCP_AMT>90.4</PS_PRCP_AMT><PS_NORM_INT>6.5</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>180.29</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>11</PS_PERD_NO><PS_DUE_DT>2018-11-12</PS_DUE_DT><PS_PRCP_AMT>92.43</PS_PRCP_AMT><PS_NORM_INT>4.47</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>87.86</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>96.9</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX><MX><PS_PERD_NO>12</PS_PERD_NO><PS_DUE_DT>2018-12-12</PS_DUE_DT><PS_PRCP_AMT>87.86</PS_PRCP_AMT><PS_NORM_INT>2.11</PS_NORM_INT><PS_OD_INT_AMT>0.0</PS_OD_INT_AMT><PS_COMM_OD_INT>0.0</PS_COMM_OD_INT><PS_FEE_AMT>0.0</PS_FEE_AMT><SETL_NORM_INT>0.0</SETL_NORM_INT><SETL_PRCP>0.0</SETL_PRCP><SETL_COMM_OD_INT>0.0</SETL_COMM_OD_INT><SETL_OD_INT_AMT>0.0</SETL_OD_INT_AMT><SETL_FEE_AMT>0.0</SETL_FEE_AMT><PROD_PRCP_AMT>0.0</PROD_PRCP_AMT><PROD_INT_AMT>0.0</PROD_INT_AMT><PROD_COMM_INT_AMT>0.0</PROD_COMM_INT_AMT><PS_OD_IND>N</PS_OD_IND><PS_INT_RATE>0.288</PS_INT_RATE><PS_OD_INT_RATE>0.432</PS_OD_INT_RATE><PS_REM_PRCP>0.0</PS_REM_PRCP><ACCT_FEE_AMT>0.0</ACCT_FEE_AMT><SETL_ACCT_FEE_AMT>0.0</SETL_ACCT_FEE_AMT><PENAL_FEE_AMT>0.0</PENAL_FEE_AMT><SETL_PENAL_FEE_AMT>0.0</SETL_PENAL_FEE_AMT><LATE_FEE_AMT>0.0</LATE_FEE_AMT><SETL_LATE_FEE_AMT>0.0</SETL_LATE_FEE_AMT><PS_WV_NM_INT>0.0</PS_WV_NM_INT><PS_WV_OD_INT>0.0</PS_WV_OD_INT><PS_WV_COMM_INT>0.0</PS_WV_COMM_INT><SETL_IND>N</SETL_IND><PS_INSTM_AMT>89.97</PS_INSTM_AMT><ADVANCE_FEE_AMT>0.0</ADVANCE_FEE_AMT><SETL_ADVANCE_FEE_AMT>0.0</SETL_ADVANCE_FEE_AMT></MX></LmPmShdList></msgall>";
        DemoResponsePm result = XmlConverter.deserialize(string, DemoResponsePm.class);
        Assert.assertEquals("00000", result.getErrorCode());
        Assert.assertEquals("success", result.getErrorMsg());
        Assert.assertNotNull(result.getLmPmShdList());
        Assert.assertEquals(13, result.getLmPmShdList().size());

        JsonSerializer.getGlobalConfig().getSerializeConfig().propertyNamingStrategy = null;
        String json = JsonSerializer.serialize(result);
        DemoResponsePm result2 = JsonSerializer.deserialize(json, DemoResponsePm.class);
        Assert.assertEquals(result.getErrorCode(), result2.getErrorCode());
        Assert.assertEquals(result.getErrorMsg(), result2.getErrorMsg());
        Assert.assertTrue(Linq.asEnumerable(result.getLmPmShdList()).sequenceEqual(Linq.asEnumerable(result2.getLmPmShdList())));
    }

    @Data
    public static class DemoResponse {
        private String errorCode;
        private String errorMsg;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class DemoResponsePm extends DemoResponse {
        @JacksonXmlProperty(localName = "LmPmShdList")
        private List<Mx> lmPmShdList;
    }

    @Data
    public static class Mx {
        private String pID;
        private String PS_PERD_NO;//0</PS_PERD_NO;//
        private String PS_DUE_DT;//2017-12-21</PS_DUE_DT;//
        private String PS_PRCP_AMT;//0.0</PS_PRCP_AMT;//
        private String PS_NORM_INT;//0.0</PS_NORM_INT;//
        private String PS_OD_INT_AMT;//0.0</PS_OD_INT_AMT;//
        private String PS_COMM_OD_INT;//0.0</PS_COMM_OD_INT;//
        private String PS_FEE_AMT;//0.0</PS_FEE_AMT;//
        private String SETL_NORM_INT;//0.0</SETL_NORM_INT;//
        private String SETL_PRCP;//0.0</SETL_PRCP;//
        private String SETL_COMM_OD_INT;//0.0</SETL_COMM_OD_INT;//
        private String SETL_OD_INT_AMT;//0.0</SETL_OD_INT_AMT;//
        private String SETL_FEE_AMT;//0.0</SETL_FEE_AMT;//
        private String PROD_PRCP_AMT;//0.0</PROD_PRCP_AMT;//
        private String PROD_INT_AMT;//0.0</PROD_INT_AMT;//
        private String PROD_COMM_INT_AMT;//0.0</PROD_COMM_INT_AMT;//
        private String PS_OD_IND;//N</PS_OD_IND;//
        private String PS_INT_RATE;//0.288</PS_INT_RATE;//
        private String PS_OD_INT_RATE;//0.432</PS_OD_INT_RATE;//
        private String PS_REM_PRCP;//1000.0</PS_REM_PRCP;//
        private String ACCT_FEE_AMT;//0.0</ACCT_FEE_AMT;//
        private String SETL_ACCT_FEE_AMT;//0.0</SETL_ACCT_FEE_AMT;//
        private String PENAL_FEE_AMT;//0.0</PENAL_FEE_AMT;//
        private String SETL_PENAL_FEE_AMT;//0.0</SETL_PENAL_FEE_AMT;//
        private String LATE_FEE_AMT;//0.0</LATE_FEE_AMT;//
        private String SETL_LATE_FEE_AMT;//0.0</SETL_LATE_FEE_AMT;//
        private String PS_WV_NM_INT;//0.0</PS_WV_NM_INT;//
        private String PS_WV_OD_INT;//0.0</PS_WV_OD_INT;//
        private String PS_WV_COMM_INT;//0.0</PS_WV_COMM_INT;//
        private String SETL_IND;//N</SETL_IND;//
        private String PS_INSTM_AMT;//0.0</PS_INSTM_AMT;//
        private String ADVANCE_FEE_AMT;//0.0</ADVANCE_FEE_AMT;//
        private String SETL_ADVANCE_FEE_AMT;//0.0</SETL_ADVANCE_FEE_AMT;//
    }
}
