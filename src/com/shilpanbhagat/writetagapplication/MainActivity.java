package com.shilpanbhagat.writetagapplication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.shilpanbhagat.writetagapplication.R;
import com.google.gson.GsonBuilder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	
	public static int READ_TAG_ACTIVITY = 0;
	public static int ADD_CLOTHING_ACTIVITY = 1;
	
	LinearLayout linearLayout;
	
	Button read;
	Button write;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		write = (Button) findViewById(R.id.button1);
		read = (Button) findViewById(R.id.button2);
		write.setOnClickListener(this);
		read.setOnClickListener(this);
		
		linearLayout = (LinearLayout) findViewById(R.id.myLinearLayout);
		
		//Start a UI thread to get all the data
		GetRecords post = new GetRecords();
		post.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	//Sets the task in the background to check for data
	class GetRecords extends AsyncTask<String, Void, String> {
		
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			return getRecords();
		}
		
		private String getRecords (){  
				// simply execute a post that will return the total number of records in database
				// this is just dummy string to tell the node app what's coming
				String id = "give me records";
				return execute(id);
		}
		
		public String execute(String id) {
		    // creates a JSON object with the id, and this gets posted to node app for a response
			Map<String, String> clothing = new HashMap<String, String>();
		    clothing.put("request", id);
		    String json = new GsonBuilder().create().toJson(clothing, Map.class);
		    return makeRequest("http://styletapp.herokuapp.com/get_all_records", json);
		}
		
		public String makeRequest(String uri, String json) {
		    // Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httpPost = new HttpPost(uri);
			try {
		        //HttpPost httpPost = new HttpPost(uri);
		        httpPost.setEntity(new StringEntity(json));
		        httpPost.setHeader("Accept", "application/json");
		        httpPost.setHeader("Content-type", "application/json");
		        System.out.println("Call successful");
		        //return new DefaultHttpClient().execute(httpPost);
		        // Execute HTTP Post Request
		        ResponseHandler<String> responseHandler=new BasicResponseHandler();
		        return httpclient.execute(httpPost, responseHandler);
		    } catch (UnsupportedEncodingException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    return null;
		}
		
		@Override
        protected void onPostExecute(String result) {
			//Call the intent and return the total number of records to date in database
			try {
				JSONObject response=new JSONObject(result);
				// the "response" gives the total number of records to date in database
				System.out.println(response);
				System.out.println(response.get("tagIds").getClass());
				int totalRecords = (Integer) response.get("records");
				JSONArray totalIDs = (JSONArray) response.get("tagIds");
				System.out.println(totalRecords);
				for(int i = 0; i<totalRecords; i++) {
					GetIndividualRecord record = new GetIndividualRecord(totalIDs.getString(i));
					record.execute();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//Get the clothing for each individual clothing
	class GetIndividualRecord extends AsyncTask<String, Void, String> {
		
		private final String ID;
		
		public GetIndividualRecord(String id) {
			ID = id;
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			return getRecords(ID);
		}
		
		private String getRecords (String id){
			// simply execute a post that will return the total number of records in database
			// this is just dummy string to tell the node app what's coming
			return execute(id);
		}
		
		public String execute(String id) {
		    // creates a JSON object with the id, and this gets posted to node app for a response
			Map<String, String> clothing = new HashMap<String, String>();
		    clothing.put("id", id);
		    String json = new GsonBuilder().create().toJson(clothing, Map.class);
	        Log.v("GetTagInfo", json);
		    return makeRequest("http://styletapp.herokuapp.com/get_tag_info", json);
		}
		
		public String makeRequest(String uri, String json) {
		    // Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httpPost = new HttpPost(uri);
			try {
		        httpPost.setEntity(new StringEntity(json));
		        httpPost.setHeader("Accept", "application/json");
		        httpPost.setHeader("Content-type", "application/json");
		        System.out.println("Call successful");
		        // Execute HTTP Post Request
		        ResponseHandler<String> responseHandler=new BasicResponseHandler();
		        return httpclient.execute(httpPost, responseHandler);
		    } catch (UnsupportedEncodingException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		    return null;
		}
		
		@Override
        protected void onPostExecute(String result) {
			//Call the intent and return the total number of records to date in database
			try {
				JSONObject response=new JSONObject(result);
				// the "response" data for that particular ID
				System.out.println(response);
				linearLayout.addView(addTable(response.get("name").toString(), response.get("brand").toString(), response.get("caption").toString(), response.get("type").toString(), response.get("tapCounter").toString()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	private TableLayout addTable(String name, String brand, String caption, String type, String counter){
	    
		TableLayout table = new TableLayout(this);
	    
	    //Setting table parameters
	    LinearLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    layoutParams.setMargins(0, 5, 0, 5);
	    table.setLayoutParams(layoutParams);
	    
	   
	    //Create a new row to be added.
	    TableRow tr1 = new TableRow(this);
	    tr1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	        
	    //Create the textView for name
	    TextView textView1 = new TextView(this);
	    textView1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    textView1.setText("Name: " + name);
	    	
	    //Add textView to tableRow
	    tr1.addView(textView1);
	        
	    //Add tableRow to table
	    table.addView(tr1);
	    
	    //Create a new row to be added.
	    TableRow tr2 = new TableRow(this);
	    tr2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	        
	    //Create the textView for caption
	    TextView textView2 = new TextView(this);
	    textView2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    textView2.setText("Brand: " + brand);
	    	
	    //Add textView to tableRow
	    tr2.addView(textView2);
	        
	    //Add tableRow to table
	    table.addView(tr2);
	    
	    //Create a new row to be added.
	    TableRow tr3 = new TableRow(this);
	    tr3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	        
	    //Create the textView for caption
	    TextView textView3 = new TextView(this);
	    textView3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    textView3.setText("Caption: " + caption);
	    	
	    //Add textView to tableRow
	    tr3.addView(textView3);
	        
	    //Add tableRow to table
	    table.addView(tr3);
	    
	    //Create a new row to be added.
	    TableRow tr4 = new TableRow(this);
	    tr4.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	        
	    //Create the textView for caption
	    TextView textView4 = new TextView(this);
	    textView4.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    textView4.setText("Type: " + type);
	    	
	    //Add textView to tableRow
	    tr4.addView(textView4);
	        
	    //Add tableRow to table
	    table.addView(tr4);
	    
	  //Create a new row to be added.
	    TableRow tr5 = new TableRow(this);
	    tr5.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	        
	    //Create the textView for caption
	    TextView textView5 = new TextView(this);
	    textView5.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    textView5.setText("Wore it: " + counter + " times");
	    	
	    //Add textView to tableRow
	    tr5.addView(textView5);
	        
	    //Add tableRow to table
	    table.addView(tr5);
	    
	    
	    return table;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		System.out.println(v);
		if(v == read) {
			System.out.println("inside");
			Intent intent = new Intent(this, ReadTagActivity.class);
			startActivityForResult(intent, READ_TAG_ACTIVITY);
		}
		else if(v == write) {
			Intent intent = new Intent(this, AddClothingActivity.class);
			startActivityForResult(intent, ADD_CLOTHING_ACTIVITY);
		}
	}
	
	// Don't restart on rotation
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		  if (requestCode == READ_TAG_ACTIVITY) {

		     if(resultCode == RESULT_OK){      
		         //String result=data.getStringExtra("result"); 
		    	 System.out.println("In the main activity");
		     }
		     if (resultCode == RESULT_CANCELED) {    
		         //Write your code on no result return 
		    	 System.out.println("In the main area");
		    	 GetRecords post = new GetRecords();
				 post.execute();
		     }
		  }
		  else if (requestCode == ADD_CLOTHING_ACTIVITY) {
			  if(resultCode == RESULT_OK){      
			      //String result=data.getStringExtra("result"); 
			      System.out.println("In the main activity");
			  }
			  if (resultCode == RESULT_CANCELED) {    
			      //Write your code on no result return 
			      System.out.println("In the main area");
			      
			      //Refreshing everytime you do a read or write
			      GetRecords post = new GetRecords();
				  post.execute();
			  }
		  }
		}
}
