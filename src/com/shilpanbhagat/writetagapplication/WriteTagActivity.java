package com.shilpanbhagat.writetagapplication;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class WriteTagActivity extends Activity implements OnClickListener {
	private static final String TAG = "NFCWriteTag";
	private NfcAdapter mNfcAdapter;
    private IntentFilter[] mWriteTagFilters;
	private PendingIntent mNfcPendingIntent;
	private boolean silent=false;
	private boolean writeProtect = false;
	private Context context;
	private TextView textView;
	
	public static String ID;
	
	private Button home;
	private Button addAnother;
	
	private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_to_tag);
        
        //getMenuInflater().inflate(R.layout.nfc_settings_layout, null);
        Intent intent = getIntent();
        ID = intent.getStringExtra("id");
        
        context = getApplicationContext();
        
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        
        IntentFilter discovery=new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);        
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        // Intent filters for writing to a tag
        mWriteTagFilters = new IntentFilter[] { discovery };
        
        textView = (TextView) findViewById(R.id.textView1);
        
        home = (Button) findViewById(R.id.button1);
        home.setOnClickListener(this);
        
        addAnother = (Button) findViewById(R.id.button2);
        addAnother.setOnClickListener(this);
        
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
        getMenuInflater().inflate(R.menu.activity_write_to_tag, menu);
        return true;
    }

	@Override
	protected void onResume() {
		super.onResume();
		if(mNfcAdapter != null) {
			if (!mNfcAdapter.isEnabled()){
	            LayoutInflater inflater = getLayoutInflater();
	        	View dialoglayout = inflater.inflate(R.layout.nfc_settings_layout,null); //(ViewGroup) findViewById(R.id.nfc_settings_layout));
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

			}
			mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
		} else {
			Toast.makeText(context, "Sorry, No NFC Adapter found.", Toast.LENGTH_SHORT).show();
		}
		

	}
    
	@Override
	protected void onPause() {
		super.onPause();
		if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);		
		if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
        	// validate that this tag can be written....
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            
            //Print out all the supported formats
            for(int k =0; k < detectedTag.getTechList().length; k++) {
            	System.out.println(detectedTag.getTechList()[k]);
            }
            
            if(supportedTechs(detectedTag.getTechList())) {
	            // check if tag is writable (to the extent that we can
	            if(writableTag(detectedTag)) {
	            	//writeTag here
	            	WriteResponse wr = writeTag(getTagAsNdef(), detectedTag);
	            	String message = (wr.getStatus() == 1? "Success: " : "Failed: ") + wr.getMessage();
	            	Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
	            } else {
	            	Toast.makeText(context,"This tag is not writable",Toast.LENGTH_SHORT).show();
	            	//Sounds.PlayFailed(context, silent);
	            	
	            }	            
            } else {
            	Toast.makeText(context,"This tag type is not supported",Toast.LENGTH_SHORT).show();
            	//Sounds.PlayFailed(context, silent);
            }
        }
    
	}
	
	
    public WriteResponse writeTag(NdefMessage message, Tag tag) {
        int size = message.toByteArray().length;
        String mess = "";

        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();

                if (!ndef.isWritable()) {
                    return new WriteResponse(0,"Tag is read-only");

                }
                if (ndef.getMaxSize() < size) {
                    mess = "Tag capacity is " + ndef.getMaxSize() + " bytes, message is " + size
                            + " bytes.";
                    return new WriteResponse(0,mess);
                }

                ndef.writeNdefMessage(message);
                if(writeProtect)  ndef.makeReadOnly();
                mess = "Yup. You're done!";
                textView.setText("Good lookin', you are");
                return new WriteResponse(1,mess);
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        mess = "Formatted tag and wrote message";
                        return new WriteResponse(1,mess);
                    } catch (IOException e) {
                        mess = "Failed to format tag.";
                        return new WriteResponse(0,mess);
                    }
                } else {
                    mess = "Tag doesn't support NDEF.";
                    return new WriteResponse(0,mess);
                }
            }
        } catch (Exception e) {
            mess = "Failed to write tag";
            return new WriteResponse(0,mess);
        }
    }
    
    private class WriteResponse {
    	int status;
    	String message;
    	WriteResponse(int Status, String Message) {
    		this.status = Status;
    		this.message = Message;
    	}
    	public int getStatus() {
    		return status;
    	}
    	public String getMessage() {
    		return message;
    	}
    }
    
	public static boolean supportedTechs(String[] techs) {
	    boolean ultralight=false;
	    boolean nfcA=false;
	    boolean ndef=false;
	    for(String tech:techs) {
	    	if(tech.equals("android.nfc.tech.MifareUltralight") || tech.equals("android.nfc.tech.MifareClassic")) {
	    		ultralight=true;
	    	}else if(tech.equals("android.nfc.tech.NfcA")) { 
	    		nfcA=true;
	    	} else if(tech.equals("android.nfc.tech.Ndef") || tech.equals("android.nfc.tech.NdefFormatable")) {
	    		ndef=true;
	   		
	    	}
	    }
        if(ultralight && nfcA && ndef) {
        	return true;
        } else {
        	return false;
        }
	}
	
    private boolean writableTag(Tag tag) {

        try {
            Ndef ndef = Ndef.get(tag);
            System.out.println(ndef);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(context,"Tag is read-only.",Toast.LENGTH_SHORT).show();
                    //Sounds.PlayFailed(context, silent);
                    System.out.println("This tag is readonly");
                    ndef.close(); 
                    return false;
                }
                ndef.close();
                return true;
            } 
        } catch (Exception e) {
            Toast.makeText(context,"Failed to read tag",Toast.LENGTH_SHORT).show();
            //Sounds.PlayFailed(context, silent);
        }

        return false;
    }
    
    private NdefMessage getTagAsNdef() {
    	boolean addAAR = false;
    	String uniqueId = "tapwise.com/" + ID;       
        byte[] uriField = uniqueId.getBytes(Charset.forName("US-ASCII"));
        byte[] payload = new byte[uriField.length + 1];              //add 1 for the URI Prefix
        payload[0] = 0x01;                                      	//prefixes http://www. to the URI
        
        System.out.println(ID);
        System.arraycopy(uriField, 0, payload, 1, uriField.length);  //appends URI to payload
        NdefRecord rtdUriRecord = new NdefRecord(
            NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, new byte[0], payload);
        
        
        
        if(addAAR) {
        	// note:  returns AAR for different app (nfcreadtag)
        	return new NdefMessage(new NdefRecord[] {
            rtdUriRecord, NdefRecord.createApplicationRecord("com.tapwise.nfcreadtag")
        }); 
        } else {
        	return new NdefMessage(new NdefRecord[] {
        		rtdUriRecord});
        }
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == home) {
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED,returnIntent);     
			finish();
		}
		else if(v == addAnother) {
			Intent newIntent = new Intent(this, AddClothingActivity.class);
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