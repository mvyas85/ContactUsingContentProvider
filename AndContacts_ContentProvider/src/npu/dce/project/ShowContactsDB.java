package npu.dce.project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ShowContactsDB extends Activity
{
	private Button showall,showbyid;
	private EditText txt_searchbox;
	private ContentResolver cr;
	private ListView lv;
	private Cursor cursor = null;
	private SimpleCursorAdapter dataAdapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showdb);
    
    	showbyid = (Button) findViewById(R.id.btn_showbyid);
    	showall = (Button) findViewById(R.id.btn_showall);
    	
    	lv = (ListView)findViewById(R.id.lv_contacts);
    	
    	 cr= getContentResolver();
    	 
  	  	displayListView("ALL");

        showbyid.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				txt_searchbox = (EditText) findViewById(R.id.txt_searchbox); 
				displayListView(txt_searchbox.getText().toString());
			}
		});
        
    	showall.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				
				displayListView("ALL");
			}
		});
    }

	@SuppressLint("NewApi")
	private void displayListView(String whichView)
	{
		if (whichView.equals("ALL"))//which view -> all Contacts
		{
			cursor = cr.query(ContactsProvider.CONTENT_URI, null, null, null, null);
		}
		else //which view -> by Contact Name
		{
			cursor = cr.query(ContactsProvider.CONTENT_URI, null, ContactsProvider.KEY_NAME + " = '" + whichView + "'", null, null);
		}
		 
		  // The desired columns to be bound
		  String[] columns = new String[] {
		    ContactsProvider.KEY_ID,
		    ContactsProvider.KEY_NAME,
		    ContactsProvider.KEY_PHONE,
		    ContactsProvider.KEY_EMAIL,
		    ContactsProvider.KEY_POSTALADDR
		  };
		 
		  // the XML defined views which the data will be bound to
		  int[] to = new int[] { 
		    R.id.lv_id,
		    R.id.lv_name,
		    R.id.lv_phone,
		    R.id.lv_email,
		    R.id.lv_add
		  };

	  dataAdapter = new SimpleCursorAdapter(this, R.layout.each_contact_lv_item,cursor, columns, to,0);
	  lv.setAdapter(dataAdapter);

	 }
}
