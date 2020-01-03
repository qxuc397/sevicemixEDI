package com.sinoservices.customized.zhongs.processor;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sinoservices.wolfdip.core.common.WPA.JAXB.EventReference;
import com.sinoservices.wolfdip.core.common.WPA.JAXB.WPAType;
import com.sinoservices.wolfdip.core.enums.EventType;
import com.sinoservices.wolfdip.core.utils.LogTransactionUtils;
import com.sinoservices.wolfdip.core.utils.WolfDIPContextUtils;
import com.sinoservices.wolfdip.orm.constant.GlobalConstant;

/**
 * 根据可视化传值，动态向中生传递authCode
 * 的值
 * @author xuqing
 *
 */
public class getParameter4AuthCode implements Processor{

	protected static Logger logger = Logger.getLogger(getParameter4AuthCode.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		JAXBElement<WPAType> jaxbElement = null;
		try {
			jaxbElement = WolfDIPContextUtils.decode(exchange);
		} catch (Exception e) {
			jaxbElement = WolfDIPContextUtils.getWpa(in);
		}
		WPAType wpaType = jaxbElement.getValue();
		String httpMethod = (String) in.getHeader("CamelHttpMethod");
		String request = null;
		String authCode = null;
		if("POST".equals(httpMethod)){
			String msg = in.getBody(String.class);
			JSONObject json = JSON.parseObject(msg);
			String trackNo = json.getString("trackNo") == null ? "" : json.getString("trackNo");
			if (!trackNo.equals("")) {
				wpaType.setTransactionCode(trackNo);
			}
			JSONArray data = json.getJSONArray("data");
			request = data.getJSONObject(0).toString();
			authCode = json.getString("userdefine1") == null ? "" : json.getString("userdefine1");
			in.setBody(request);
		}else if("GET".equals(httpMethod)){
			authCode = (String) in.getHeader("authCode");
		}
	    Map<String,String> strParams = new HashMap<String,String>();
	    strParams.put("authCode",authCode);
	    in.setHeader(GlobalConstant.HTTPS_CLIENT_REQUEST_PARAMS,strParams);
		backupBody(in, jaxbElement, request);
		WolfDIPContextUtils.encode(jaxbElement, in);
	}
	
	public static void backupBody(Message in, JAXBElement<WPAType> wpa,String body) throws Exception {
		long startTime = System.currentTimeMillis();
		WPAType wpaType = wpa.getValue();
		EventReference eventReference = WolfDIPContextUtils.resetEventReference(wpaType, EventType.PROCESSOR);
		eventReference.getEventRemarks().add(body);
		LogTransactionUtils.logEvent(in, wpa, startTime);
	}

}
