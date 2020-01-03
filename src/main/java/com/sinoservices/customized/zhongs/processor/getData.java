package com.sinoservices.customized.zhongs.processor;


import javax.xml.bind.JAXBElement;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;

import com.sinoservices.wolfdip.core.common.WPA.JAXB.EventReference;
import com.sinoservices.wolfdip.core.common.WPA.JAXB.WPAType;
import com.sinoservices.wolfdip.core.enums.EventType;
import com.sinoservices.wolfdip.core.utils.LogTransactionUtils;
import com.sinoservices.wolfdip.core.utils.WolfDIPContextUtils;

/**
 * 获取get反馈的response的值
 * @author xuqing
 *
 */
public class getData implements Processor {

	protected static Logger logger = Logger.getLogger(getData.class);

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
		String msg = in.getBody(String.class);
		String flowCode = wpaType.getFlowCode();
		String request = null;
		if(flowCode.contains("EnterpriseProbe")|| flowCode.contains("Storages")){
			String authCode = (String) in.getHeader("authCode");
		    request = msg.replace("\"code\":0,","\"code\":0,\"systemId\":\"ZHONGS\",\"authCode\":\""+authCode+"\",");
		}
		in.setBody(request);
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