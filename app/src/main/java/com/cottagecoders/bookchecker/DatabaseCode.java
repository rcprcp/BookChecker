package com.cottagecoders.bookchecker;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseCode extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseCode";
	private static final String DB_NAME = "PriceChecker.sqlite";
	private static final int VERSION = 1;

	private static final String T_BOOKS = "bookTable";
	private static final String T_PRICES = "priceTable";
	private static final String T_COLLECTIONS = "collectionTable";
	private static final String T_COLLECTION_NAMES = "collectionNameTable";

	private static SQLiteDatabase db = null;

	public DatabaseCode(Context context) {
		super(context, DB_NAME, null, VERSION);
		return;
	}

	/**
	 * create all the tables... this will be called when the database does not
	 * exist.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {

		String stmt;

		// books...
		stmt = "CREATE TABLE " + T_BOOKS
				+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ " upc VARCHAR(20),  " + " isbn10 VARCHAR(20), "
				+ " itemType INT, " + " title VARCHAR(200), "
				+ " subTitle VARCHAR(200), " + " author VARCHAR(200), "
				+ " publisher  VARCHAR(200), " + " published VARCHAR(20), "
				+ " listPrice DOUBLE, " + " retailPrice DOUBLE, "
				+ " currency VARCHAR(20), " + " imagePath VARCHAR(200),"
				+ " condition INTEGER) ";

		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_BOOKS + "_ix1  ON " + T_BOOKS + "(title) ";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_BOOKS + "_ix2  ON " + T_BOOKS + "(author) ";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_BOOKS + "_pk  ON " + T_BOOKS + "(upc) ";
		myExecSQL(db, "onCreate()", stmt);

		// prices for this item with vendor info (we buy based on this)
		stmt = "CREATE TABLE " + T_PRICES + " ( id INTEGER REFERENCES "
				+ T_BOOKS + " (id) ON DELETE CASCADE, " + " upc VARCHAR(20), "
				+ " vendorName VARCHAR(100), " + "price DOUBLE, "
				+ " shipping DOUBLE, totalPrice DOUBLE, source INT )";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_PRICES + "_ix1  ON " + T_PRICES
				+ " (id, vendorName) ";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_PRICES + "_ix2  ON " + T_PRICES
				+ " (id, price, vendorName) ";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_PRICES + "_ix3  ON " + T_PRICES
				+ " (id, shipping, vendorName) ";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_PRICES + "_ix4  ON " + T_PRICES
				+ " (id, totalPrice, vendorName) ";
		myExecSQL(db, "onCreate()", stmt);

		// collection names - such as "For Sale", "myLibrary", etc.
		stmt = "CREATE TABLE " + T_COLLECTION_NAMES
				+ " (collection INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ " collectionName VARCHAR(50) )";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_COLLECTION_NAMES + "_ix1  ON "
				+ T_COLLECTION_NAMES + " (collectionName) ";
		myExecSQL(db, "onCreate()", stmt);

		// collection - link table containing the ids of the books in this
		// collection.
		stmt = "CREATE TABLE " + T_COLLECTIONS + " (id INTEGER REFERENCES "
				+ T_BOOKS + " (id) ON DELETE CASCADE, " + " myPrice DOUBLE, "
				+ " collection INTEGER " + " shipping DOUBLE )";
		myExecSQL(db, "onCreate()", stmt);

		stmt = "CREATE INDEX " + T_COLLECTIONS + "_ix1  ON " + T_COLLECTIONS
				+ " (myPrice) ";
		myExecSQL(db, "onCreate()", stmt);

	}

	/**
	 * simple routine to execute an SQL statement and handle errors. this little
	 * routine makes the code a bit more concise.
	 * <p>
	 * obviously, this will only work for statements without host variables.
	 * 
	 * @param db
	 *            database object.
	 * @param rtn
	 *            calling routine name for the Log statement in case of trouble.
	 * @param stmt
	 *            the SQL statement to execute.
	 */
	public void myExecSQL(SQLiteDatabase db, String rtn, String stmt) {
		try {
			db.execSQL(stmt);
		} catch (Exception e) {
			Log.i(TAG, rtn + " myExecSQL(): " + stmt + " failed " + e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// implement migration code here.
		Log.i(TAG, "onUpgrade() -- get here.");

	}

	public int insertIntoItemTable(BookRec item) {
		if (db == null) {
			db = getWritableDatabase();
		}

		String stmt = "INSERT INTO "
				+ T_BOOKS
				+ " (upc, isbn10, title, subTitle, author, publisher, published, listPrice, "
				+ " retailPrice, currency, imagePath, condition) " + " VALUES "
				+ "(" + "\"" + item.getUpc() + "\", " + "\"" + item.getIsbn10()
				+ "\", " + "\"" + item.getTitle() + "\", \""
				+ item.getSubTitle() + "\", \"" + item.getAuthor() + "\", "
				+ "\"" + item.getPublisher() + "\", " + "\""
				+ item.getPublished() + "\", " + item.getListPrice() + ", "
				+ item.getRetailPrice() + ", " + "\"" + item.getCurrency()
				+ "\", " + "\"" + item.getImagePath() + "\", "
				+ item.getCondition() + " )";

		myExecSQL(db, "insertIntoitemTable()", stmt);

		String stmt2 = "SELECT last_insert_rowid()";
		Cursor c = null;
		try {
			c = db.rawQuery(stmt2, null);
		} catch (Exception e) {
			Log.d(TAG, "insertIntoItemTable(): sql failed " + stmt + " " + e);
		}
		c.moveToFirst();
		int theId = c.getInt(0);
		c.close();
		return theId;
	}

	public void deleteFromItems(BookRec item) {
		if (db == null) {
			db = getWritableDatabase();
		}
		Log.d(TAG, "deleteFromItems(): no code here.");
	}

	public ArrayList<BookRec> getItemById(int id) {
		if (db == null) {
			db = getWritableDatabase();
		}

		// TODO: fix this. must return arraylist - there can be dupe UPCs.

		String stmt = "SELECT id, upc, isbn10, title, subTitle, author, "
				+ " publisher, published, listPrice, "
				+ " retailPrice, currency, imagePath, condition";
		stmt += " FROM " + T_BOOKS + " WHERE id = " + id;

		ArrayList<BookRec> books = new ArrayList<BookRec>();
		Cursor tt = null;
		try {
			tt = db.rawQuery(stmt, null);
		} catch (Exception e) {
			Log.e(TAG, "getItemByUPC() stmt " + stmt + " failed " + e);
			return books;
		}

		if (tt == null) {
			return books;
		}

		while (tt.moveToNext()) {
			BookRec item = new BookRec(tt.getInt(0), tt.getString(1),
					tt.getString(2), tt.getString(3), tt.getString(4),
					tt.getString(5), tt.getString(6), tt.getString(7),
					tt.getDouble(8), tt.getDouble(9), tt.getString(10),
					tt.getString(11), tt.getInt(12));
			books.add(item);
		}

		tt.close();
		return books;
	}

	public ArrayList<BookRec> fetchAllBooks(String orderBy) {
		if (db == null) {
			db = getWritableDatabase();
		}

		// set up return value.
		ArrayList<BookRec> books = new ArrayList<BookRec>();
		String stmt = "SELECT id, upc, isbn10, title, subTitle, "
				+ "author, publisher, published, listPrice, "
				+ "retailPrice, currency, imagePath, condition";
		stmt += " FROM " + T_BOOKS + "  " + orderBy;

		Cursor tt = null;
		try {
			tt = db.rawQuery(stmt, null);
		} catch (Exception e) {
			Log.e(TAG, "fetchAllBooks(): stmt " + stmt + " failed " + e);
		}

		// failed... no cursor.
		if (tt == null) {
			return books;
		}

		while (tt.moveToNext()) {
			BookRec item = new BookRec(tt.getInt(0), tt.getString(1),
					tt.getString(2), tt.getString(3), tt.getString(4),
					tt.getString(5), tt.getString(6), tt.getString(7),
					tt.getDouble(8), tt.getDouble(9), tt.getString(10),
					tt.getString(11), tt.getInt(12));
			books.add(item);
		}

		tt.close();
		return books;
	}

	public int countPricesById(int id) {
		if (db == null) {
			db = getWritableDatabase();
		}

		String stmt = "SELECT count(price) FROM " + T_PRICES + " WHERE id = "
				+ id;

		Cursor c = null;
		try {
			c = db.rawQuery(stmt, null);
		} catch (Exception e) {
			Log.e(TAG, "countPricesById(): stmt " + stmt + " failed " + e);
			return 0;
		}

		// failed... no cursor.
		if (c == null) {
			return 0;
		}
		
		c.moveToFirst();
		int val = c.getInt(0);
		c.close();
		return val;
	}

	public ArrayList<PriceRec> getPricesForId(int id, String orderBy) {
		if (db == null) {
			db = getWritableDatabase();
		}

		String stmt = "SELECT id, vendorName, price, shipping, totalPrice, source FROM "
				+ T_PRICES + " WHERE id = " + id + "  " + orderBy;

		// set up return value.
		ArrayList<PriceRec> prices = new ArrayList<PriceRec>();
		Cursor c = null;
		try {
			c = db.rawQuery(stmt, null);
		} catch (Exception e) {
			Log.e(TAG, "getPricesForId(): stmt " + stmt + " failed " + e);
			return prices;
		}

		// failed... no cursor.
		if (c == null) {
			return prices;
		}

		while (c.moveToNext()) {
			PriceRec p = new PriceRec(c.getInt(0), c.getString(1),
					c.getDouble(2), c.getDouble(3), c.getDouble(4), c.getInt(5));
			prices.add(p);
		}

		c.close();
		return prices;
	}

	public void addPrices(ArrayList<PriceRec> prices) {
		if (db == null) {
			db = getWritableDatabase();
		}

		String stmt;

		for (PriceRec p : prices) {
			stmt = "DELETE FROM " + T_PRICES + " WHERE id = \"" + p.getId()
					+ "\"";
			myExecSQL(db, "addPrices(): delete previous data: ", stmt);

		}

		for (PriceRec p : prices) {
			stmt = "INSERT INTO " + T_PRICES
					+ " (id, vendorName, price, shipping, totalPrice, source) "
					+ " VALUES ( \"" + p.getId() + "\", \"" + p.getVendorName()
					+ "\", " + p.getPrice() + ", " + p.getShipping() + ", "
					+ p.getTotalPrice() + ", " + p.getSource() + ")";
			myExecSQL(db, "addPrices(): ", stmt);
			Log.d(TAG, "addPrices():" + stmt);
		}
	}
}
