package com.shilpanbhagat.writetagapplication;


import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.GsonBuilder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PatternMatcher;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

@SuppressLint("NewApi")
public class ReadTagActivity extends Activity implements OnClickListener {
	private static final String TAG = "NFCReadTag";
	private NfcAdapter mNfcAdapter;
	private IntentFilter[] mNdefExchangeFilters;
	private PendingIntent mNfcPendingIntent;

	private Button read;
	private Button home;
	
	private ImageView imageView;
	
	private LinearLayout linearLayout;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_tag);
        
        home = (Button) findViewById(R.id.button1);
		read = (Button) findViewById(R.id.button2);
		home.setOnClickListener(this);
		read.setOnClickListener(this);
		
		linearLayout = (LinearLayout) findViewById(R.id.myLinearLayout);
        
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

	    mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
	    

		IntentFilter tapwise = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		tapwise.addDataScheme("http");
		tapwise.addDataAuthority("www.tapwise.com", null);
		tapwise.addDataPath(".*", PatternMatcher.PATTERN_SIMPLE_GLOB);
		
		mNdefExchangeFilters = new IntentFilter[] { tapwise };
		
		imageView = (ImageView) findViewById(R.id.imageView1);
        
        try {
			InputStream bitmap=getAssets().open("tap-phone.png");
			Bitmap bit=BitmapFactory.decodeStream(bitmap);
       		imageView.setImageBitmap(bit);
		} catch (Exception e) {
			e.printStackTrace();
		}	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	protected void onResume() {
		super.onResume();
		if(mNfcAdapter != null) {

			if (!mNfcAdapter.isEnabled()){
				
	            LayoutInflater inflater = getLayoutInflater();
	        	View dialoglayout = inflater.inflate(R.layout.nfc_settings_layout,null);
	            new AlertDialog.Builder(this).setView(dialoglayout)
	            	    .setPositiveButton("Update Settings", new DialogInterface.OnClickListener() {
			                public void onClick(DialogInterface arg0, int arg1) {
			    				Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			    				startActivity(setnfc);
			                }
		                })
	                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
	                        
	                    	public void onCancel(DialogInterface dialog) {
	                    		finish(); // exit application if user cancels
	                        }	                    	
	                    }).create().show();

			} else {
				mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
			}
			
		} else {
			Toast.makeText(getApplicationContext(), "Sorry, No NFC Adapter found.", Toast.LENGTH_SHORT).show();
		}
		

	}
    
	@Override
	protected void onPause() {
		super.onPause();
		if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		System.out.println("got here");
		super.onNewIntent(intent);		
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
			NdefMessage[] messages = null;
			Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
			if (rawMsgs != null) {
				messages = new NdefMessage[rawMsgs.length];
				for (int i = 0; i < rawMsgs.length; i++) {
					messages[i] = (NdefMessage) rawMsgs[i];
				}
			}
			if(messages[0] != null) {
				String result="";
				byte[] payload = messages[0].getRecords()[0].getPayload();
				// this assumes that we get back am SOH followed by host/code
				for (int b = 1; b<payload.length; b++) { // skip SOH
					result += (char) payload[b];
				}
				//Toast.makeText(getApplicationContext(), "Tag Contains " + result, Toast.LENGTH_SHORT).show();
				//System.out.println(result);
				System.out.println(result.substring(result.indexOf("/") + 1, result.length()));
				GetInfo getInfo = new GetInfo(result.substring(result.indexOf("/") + 1, result.length()));
				getInfo.execute();
			}
		}
	}
	
	class GetInfo extends AsyncTask<String, Void, String> {
		
		private String mainID;
		
		public GetInfo(String id) {
			mainID = id;
		}
		
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			return getTag(mainID);
		}
		
		private String getTag (String mainID){
			// give the tagID and we'll post it to database, increment counter by 1, and get the rest of the info from database
			// right now it is hardcoded, but whatever tag ID is passed in, it will send back the rest of the info
			return execute(mainID);
		}
		
		public String execute(String id) {
		    // creates a JSON object with the id, and this gets posted to node app for a response
			Map<String, String> clothing = new HashMap<String, String>();
		    clothing.put("id", id);
		    String json = new GsonBuilder().create().toJson(clothing, Map.class);
	        Log.v("GetTagInfo", json);
		    return makeRequest("http://styletapp.herokuapp.com/read_tag_info", json);
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
			//Call the intent and get back the complete info from the http post response
			try {
				JSONObject response=new JSONObject(result);
				// the "response" gives the complete info about the tag in a JSON object
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
		if(v == home) {
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED,returnIntent);     
			finish();
		}
		else if(v == read) {
			Intent newIntent = new Intent(this, ReadTagActivity.class);
			startActivityForResult(newIntent, 1);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		  if (requestCode == 1) {

		     if(resultCode == RESULT_OK){
		         
		     }
		     if (resultCode == RESULT_CANCELED) {    
		         //Write your code on no result return 
		    	 System.out.println("going back to previous activity");
		    	 Intent returnIntent = new Intent();
		    	 setResult(RESULT_CANCELED, returnIntent);        
		    	 finish();
		     }
		  }
		}

}