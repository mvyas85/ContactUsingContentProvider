package npu.dce.project;

//npu.dce.project.ContactsProvider

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ContactsProvider extends ContentProvider{
	 
	private static final String DATABASE_NAME = "conDatabase.db";
    private static final String DATABASE_TABLE = "contactsTable";
    private static final int DATABASE_VERSION = 2;

    static final String PROVIDER_NAME = "npu.dce.project.ContactsProvider";
	static final String URL = "content://" + PROVIDER_NAME + "/cte";
	static final Uri CONTENT_URI = Uri.parse(URL);
	
    //EACH COLUMN IN DATABASE TABLE
    public static final String KEY_ID = "_id"; //primary key, CursorAdapter will use this
    public static final String KEY_NAME = "NAME";
    public static final String KEY_PHONE = "PHONE";
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_POSTALADDR = "POSTALADDR";

    private SQLiteDatabase db;

    private static final UriMatcher uriMatcher;
    
    private static final int CONTACTS = 1;
    private static final int CONTACT_ID = 2;
    
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "cte", CONTACTS);
        uriMatcher.addURI(PROVIDER_NAME, "cte/*", CONTACT_ID);
    }
   
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
      int count;
      
      switch (uriMatcher.match(uri)) {
        case CONTACTS:
          count = db.delete(DATABASE_TABLE, where, whereArgs);
          break;

        case CONTACT_ID:
          String segment = uri.getPathSegments().get(1);
          count = db.delete(DATABASE_TABLE, KEY_ID + "="
                                      + segment
                                      + (!TextUtils.isEmpty(where) ? " AND (" 
                                      + where + ')' : ""), whereArgs);
          break;

        default: throw new IllegalArgumentException("Unsupported URI: " + uri);
      }

      getContext().getContentResolver().notifyChange(uri, null);
      return count;
    }
    
    public String getType(Uri uri) {
      switch (uriMatcher.match(uri)) {
        case CONTACTS: return "vnd.android.cursor.dir/cte";
        case CONTACT_ID: return "vnd.android.cursor.dir/cte";

        default: throw new IllegalArgumentException("Unsupported URI: " + uri);
      }
    }
    
    @Override
    public Uri insert(Uri _uri, ContentValues _initialValues) {
      // Insert the new row, will return the row number if 
      // successful.
      long rowID = db.insert(DATABASE_TABLE, "quake", _initialValues);
            
      // Return a URI to the newly inserted row on success.
      if (rowID > 0) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
      }
      throw new SQLException("Failed to insert row into " + _uri);
    }
    
    @Override
	public boolean onCreate() {
		Context context = getContext();
		myDatabaseOpenHelper dbHelper = new myDatabaseOpenHelper(context);
		db = dbHelper.getWritableDatabase();
		if (db != null) {
			return true;
		}
		return false;
	}
    
    @Override
    public Cursor query(Uri uri, 
                        String[] projection, 
                        String selection, 
                        String[] selectionArgs, 
                        String sort) {
          
      SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

      qb.setTables(DATABASE_TABLE);

      // If this is a row query, limit the result set to the passed in row. 
      switch (uriMatcher.match(uri)) {
        case CONTACT_ID: qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                       break;
        default      : break;
      }

      // If no sort order is specified sort by id
      String orderBy;
      if (TextUtils.isEmpty(sort)) {
        orderBy = KEY_ID;
      } else {
        orderBy = sort;
      }
    
      // Apply the query to the underlying database.
      Cursor c = qb.query(db, 
                          projection, 
                          selection, selectionArgs, 
                          null, null, 
                          orderBy);

      // Register the contexts ContentResolver to be notified if
      // the cursor result set changes. 
      c.setNotificationUri(getContext().getContentResolver(), uri);
      
      // Return a cursor to the query result.
      return c;
    }

    

   

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
      int count;
      switch (uriMatcher.match(uri)) {
        case CONTACTS: count = db.update(DATABASE_TABLE, values, 
                                                 where, whereArgs);
                     break;

        case CONTACT_ID: String segment = uri.getPathSegments().get(1);
                       count = db.update(DATABASE_TABLE, values, KEY_ID 
                               + "=" + segment 
                               + (!TextUtils.isEmpty(where) ? " AND (" 
                               + where + ')' : ""), whereArgs);
                       break;

        default: throw new IllegalArgumentException("Unknown URI " + uri);
      }

      getContext().getContentResolver().notifyChange(uri, null);
      return count;
    }
    
	///////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////HELPER CLASS///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////
	private static class myDatabaseOpenHelper extends SQLiteOpenHelper 
	{
		public myDatabaseOpenHelper(Context context, String name,
		CursorFactory factory, int version) {
		super(context, name, factory, version);
		}
		
		private static final String CREATE_TABLE =
		"create table " + DATABASE_TABLE + " (" +
		KEY_ID + " integer primary key autoincrement, " +
		KEY_NAME + " text not null, " +
		KEY_PHONE + " text, " +
		KEY_EMAIL + " text, " +
		KEY_POSTALADDR + " text);";
		
		myDatabaseOpenHelper(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL(CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
}
}




/*

   public void open() throws SQLiteException 
    {
        try 
        {
            db = dbHelper.getWritableDatabase();
        } 
        catch (SQLiteException ex) 
        {
            db = dbHelper.getReadableDatabase();
        }
    }

    //wrapper method, release database object
    public void close() 
    {
        db.close();
    }
//Insert a new entry (consists a set of rows) into the table
public long insertEntry(DataModel dataModel) 
{
    ContentValues rows = new ContentValues();
    
    rows.put(KEY_NAME, dataModel.getName());
    rows.put(KEY_PHONE, dataModel.getPhone());
    rows.put(KEY_EMAIL, dataModel.getEmail());
    rows.put(KEY_POSTALADDR, dataModel.getPostaladdr());
    
    return db.insert(DATABASE_TABLE, null, rows);
}
//return a single DataModel object based on what name to search
public Cursor getAllEntries() throws SQLException {
	    
    Cursor cursor = db.query(DATABASE_TABLE,
    		new String[] {KEY_ID, KEY_NAME, KEY_PHONE, KEY_EMAIL, KEY_POSTALADDR},null, null, null, null,null, null);

    if (cursor != null) 
    {
    	cursor.moveToFirst();
    }
    return cursor;       
}
    public boolean removeEntry(long _rowIndex) {
        return db.delete(DATABASE_TABLE, KEY_ID + "=" + _rowIndex, null) > 0;
    }

public void deleteAllEntries() {
    db.execSQL("DELETE FROM " + DATABASE_TABLE);
}

public Cursor getEntry(String searchname) throws SQLException {
	    
	Cursor cursor = db.query(DATABASE_TABLE,
    		new String[] {KEY_ID, KEY_NAME, KEY_PHONE, KEY_EMAIL, KEY_POSTALADDR},
    		KEY_NAME + "=" + "'" + searchname.trim() + "'", null, null, null,null, null);

    return cursor;
}*/
