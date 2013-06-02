package com.sugarcrm.voodoo.webservices;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Conrad Warmbold
 *
 */
public class WS {
	
	public enum TYPE { JSON, XML };
	public enum OP { DELETE, GET, POST, PUT };

	public static String getValue(TYPE type, String body, String key) throws Exception {
		String value = null;
		switch (type) {
			case JSON:
				JSONObject jsonObj = (JSONObject)JSONValue.parse(body);
				//TODO
				break;
			case XML:
				SAXParserFactory factory = SAXParserFactory.newInstance();
			    factory.setValidating(true);
			    SAXParser saxParser = factory.newSAXParser();
		        saxParser.parse(new ByteArrayInputStream(body.getBytes()), new DefaultHandler());
		        //TODO
		    	break;
			default:
				throw new Exception("WS:TYPE not recognized.");
		}
		return value;
	}
	
	public static Map<String, Object> request(OP op, String uri, Map<String, String> headers, String body) throws Exception {
		
		HttpUriRequest request = null;
	    Map<String, Object> mapParse = null;
		switch (op) {
			case DELETE:
				request = new HttpDelete(uri);
	            mapParse = requestIt(request, headers);
				break;
			case GET:
				request = new HttpGet(uri);
	            mapParse = requestIt(request, headers);
				break;
			case POST:
				HttpPost post = new HttpPost(uri);
	            mapParse = postRequest(post, headers, body); 
				break;
			case PUT:
				HttpPut put = new HttpPut(uri);
				if (body != null) put.setEntity(new StringEntity(body));
				request = put;
	            mapParse = requestIt(request, headers);
				break;
			default:
				throw new Exception("WS:OP type not recognized.");
		}
		
		return mapParse;
	}
	
	private static Map<String, Object> requestIt(HttpUriRequest request, Map<String, String> headers) {
		
		Header[] hdrs = new Header[headers.size()];
		
		int i = 0;
		for (Map.Entry<String, String> header : headers.entrySet()) {
			hdrs[i++] = new BasicHeader(header.getKey(), header.getValue());
		}
		
		request.setHeaders(hdrs);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse execute = null;
        try {
			execute = httpClient.execute(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		httpErrorSimpleCheckHttp(execute);

		HttpEntity entity = execute.getEntity();

		JSONObject parse = null;
		try {
			parse = (JSONObject) JSONValue.parse(new InputStreamReader(entity
					.getContent()));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> mapParse = (Map<String, Object>) parse;

		return mapParse;
	}
	
	private static Map<String, Object> postRequest(HttpPost post, Map<String, String> headers, String body) {

        MultipartEntity me = setMultipartEntity(headers);
		post.setEntity(me);

		if (body != null) {
			try {
				post.setEntity(new StringEntity(body));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
		
		DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
		HttpResponse execute = null;
		try {
			execute = defaultHttpClient.execute(post);
		} catch (IOException e) {
			e.printStackTrace();
		}

		httpErrorSimpleCheckHttp(execute);

		HttpEntity entity = execute.getEntity();

		JSONObject parse = null;
		try {
			parse = (JSONObject) JSONValue.parse(new InputStreamReader(entity
					.getContent()));
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> mapParse = (Map<String, Object>) parse;

		return mapParse;
	}
	
	private static MultipartEntity setMultipartEntity(Map<String, String> headers) {

		MultipartEntity multipartEntity = new MultipartEntity();
		try {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				multipartEntity.addPart(entry.getKey(),
						new StringBody(entry.getValue()));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return multipartEntity;
	}

	private static void httpErrorSimpleCheckHttp(HttpResponse execute) {

		int code = execute.getStatusLine().getStatusCode();
		if (code != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + code);
		}
	}
	
	public static void main(String[] args) {
		try {
//			System.out.println("=======decode=======");
//			String s = "[0,{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}]";
//			Object obj = JSONValue.parse(s);
//			JSONArray arr = (JSONArray) obj;
//			System.out.println(arr.get(1));
//			System.out.println(arr.has("2"));
//			System.out.println(arr.get("2"));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}