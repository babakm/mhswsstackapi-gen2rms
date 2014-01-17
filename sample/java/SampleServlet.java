package com.mhsystems.ws.stack.server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.URL;
import java.net.HttpURLConnection;

import java.nio.charset.Charset;

import java.util.UUID;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mhsystems.ws.repackaged.json.JSONObject;
import com.mhsystems.ws.repackaged.json.XML;

public class SampleServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private static final String SERVERVERSION = "https://secured.mhsystems.com/stack/v0/";
	//private static final String SERVERVERSION = "http://127.0.0.1:8888/v0/";
	
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
		
		response.setContentType("application/json;charset=utf-8");
		
		try {
						
			response.setStatus(200);
			
			final String client_identifier = request.getParameter("clientid");
			final String client_secret_key = this.getServletContext().getInitParameter(client_identifier);

			this.doConnect(client_identifier, client_secret_key);
			
			final String dump = request.getParameter("dump");
			
			if (dump == null) {
				
				response.getWriter().println((new JSONObject()).put("success", true).toString(4));
				
			} else {
				
				if (Boolean.parseBoolean(dump)) {
					
					response.getWriter().println((new JSONObject()).put("success", true).put("dump", this.doDump(client_identifier, client_secret_key)).toString(4));
					
				} else {
					
					response.getWriter().println((new JSONObject()).put("success", true).toString(4));
					
				}
				
			}
			
		} catch (final Throwable caught) {
			
			response.setStatus(400);
			
			final String report_uuid = UUID.randomUUID().toString();
			
			System.err.println("***** [ " + report_uuid + " ]");

			caught.printStackTrace(System.err);
									
			response.getWriter().println("{\"status\":1, \"report\":\"" + report_uuid + "\"}");
				
		}
		
	}
	
	private final JSONObject doDump(final String client_identifier, final String client_secret_key) throws Exception {
		
		final JSONObject dump = new JSONObject();
		
		dump.put("brand", this.doQuery(client_identifier, client_secret_key, "brand"));
		dump.put("category", this.doQuery(client_identifier, client_secret_key, "category"));
		dump.put("subcategory", this.doQuery(client_identifier, client_secret_key, "subcategory"));
		dump.put("fabric", this.doQuery(client_identifier, client_secret_key, "fabric"));
		dump.put("style", this.doQuery(client_identifier, client_secret_key, "style"));
		dump.put("size", this.doQuery(client_identifier, client_secret_key, "size"));
		dump.put("size2", this.doQuery(client_identifier, client_secret_key, "size2"));
		dump.put("color", this.doQuery(client_identifier, client_secret_key, "color"));
		dump.put("product", this.doQuery(client_identifier, client_secret_key, "product"));
		dump.put("inventory", this.doGetQuantity(client_identifier, client_secret_key));
		
		return dump;
		
	}
	
	private final JSONObject doConnect(final String client_identifier, final String client_secret_key) throws Exception {
		
		return
			this.post(
				client_identifier,
				client_secret_key,
				SERVERVERSION + "connect",
				new JSONObject());
		
	}
	
	private final JSONObject doQuery(final String client_identifier, final String client_secret_key, final String assetname) throws Exception {
		
		return
			this.post(
				client_identifier,
				client_secret_key,
				SERVERVERSION + assetname + "/list",
				new JSONObject());
		
	}
	
	private final JSONObject doGetQuantity(final String client_identifier, final String client_secret_key) throws Exception {
		
		return
			this.post(
				client_identifier,
				client_secret_key,
				SERVERVERSION + "product/quantity",
				(new JSONObject()));
		
	}
	
	@SuppressWarnings("unused")
	private final JSONObject doGetQuantity(final String client_identifier, final String client_secret_key, final String uid) throws Exception {
		
		return
			this.post(
				client_identifier,
				client_secret_key,
				SERVERVERSION + "product/quantity",
				(new JSONObject()).put("uid", uid));
		
	}
	
	@SuppressWarnings("unused")
	private final JSONObject doSetQuantity(final String client_identifier, final String client_secret_key, final String uid, final String reference) throws Exception {
		
		return
			this.post(
				client_identifier,
				client_secret_key,
				SERVERVERSION + "checkout/prepare",
				(new JSONObject()).put("uid", uid).put("reference", reference).put("qty", 1));
		
	}
	
	private JSONObject post(
		final String client_identifier,
		final String client_secret_key,
		final String endpoint,
		final JSONObject message
		) throws Exception {
		
		if (client_identifier == null) {
			throw new NullPointerException();
		}
		
		if (client_secret_key == null) {
			throw new NullPointerException();
		}
		
		if (endpoint == null) {
			throw new NullPointerException();
		}
		
		if (message == null) {
			throw new NullPointerException();
		}
		
		int retry_count = 0;
		
		for (;;) {
		
			try {
				
	            final HttpURLConnection connection = (HttpURLConnection) (new URL(endpoint)).openConnection();
	            
	            connection.setDoOutput(true);
	            connection.setRequestMethod("POST");
	            
	            final String payload = message.toString();
	            
	            connection.setRequestProperty("x-result-format", "XML");
	            connection.setRequestProperty("x-client-identifier", client_identifier);
	            connection.setRequestProperty("x-retry-count", Integer.toString(retry_count));
	            connection.setRequestProperty("x-payload-signature", MessageTokenUtil.computeMD5(client_secret_key + payload));
	
	            final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), Charset.forName("UTF-8"));
	            
	            writer.write(payload);
	            
	            writer.close();
	    
	            final BufferedReader reader =
	            	new BufferedReader(new InputStreamReader(connection.getInputStream(), Charset.forName("UTF-8")));
		        
		        final StringBuffer buffer = new StringBuffer(); String line;
		
		        while ((line = reader.readLine()) != null) {
		        	buffer.append(line);
		        }
		        
		        reader.close();
		        		        		        
		        //final JSONObject result = new JSONObject(buffer.toString());
		        
		        final JSONObject result = XML.toJSONObject(buffer.toString());
		        
		        final int status = result.getInt("status");
		        		        
		        switch (status) {
		        
		        	case 0:
		        		return result;
		        		
	        		default:
	        				        			
	        			return null;
		        
		        }
			
			} catch (final Throwable caught) {
				
				caught.printStackTrace(System.err);
				
				retry_count++;
				
				if (retry_count > 1) {
					throw new IOException(caught);
				}
				
				Thread.sleep(1024);
				
				continue;
			
			}
			
		}
		
	}

}
