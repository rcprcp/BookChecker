package com.cottagecoders.bookchecker;

import java.text.NumberFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ViewBookDetails extends Activity {
	static final String TAG = "ViewItemDetails";
	DatabaseCode db;
	static Context ctx;
	static Activity act;

	int id; 
	static boolean dispDetails = true;
	Button details;
	Button prices;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate(): got here");
		super.onCreate(savedInstanceState);

		ctx = this.getApplicationContext();
		act = this;
		db = new DatabaseCode(ctx);

		id = getIntent().getIntExtra(ViewBookList.ID, -1);

		setContentView(R.layout.view_book_details);
		details = (Button) findViewById(R.id.itemDetails);
		details.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dispDetails = true;
				displayDetails(id);
			}
		});

		prices = (Button) findViewById(R.id.itemPrices);
		prices.setText("" + db.countPricesById(id) + " Prices");
		prices.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dispDetails = false;
				displayPrices(id);
			}
		});
		
		if(dispDetails) {
			displayDetails(id);
		} else {
			displayPrices(id);			
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState(): GOT HERE");
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		Log.d(TAG, "onRestoreInstanceState(): GOT HERE");
		super.onRestoreInstanceState(savedInstanceState);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		Log.d(TAG, "onActivityResult(): GOT HERE");

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume(): GOT HERE. dispDetails " + dispDetails);

		if(dispDetails) {
			displayDetails(id);
		} else {
			displayPrices(id);			
		}
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause(): GOT HERE");
		super.onPause();
	}

	private void displayDetails(int id) {
		Log.d(TAG, "displayDetails(): GOT HERE. id " + id);

		ArrayList<BookRec> books = db.getItemById(id);
		BookRec b;
		if (books.size() > 0) {
			b = books.get(0);
		} else {
			Log.d(TAG, "displayDetails(): no records found for id " + id);
			return;
		}

		TableLayout table = (TableLayout) findViewById(R.id.detailTable);
		table.removeAllViews();

		// TODO: might want to order these differently.
		TextView tv;
		TableRow row;

		row = new TableRow(ctx);
		tv = myCreateTextView("Title ");
		row.addView(tv);
		tv = myCreateTextView(b.getTitle() + " " + b.getSubTitle());
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("Author(s) ");
		row.addView(tv);
		tv = myCreateTextView(b.getAuthor());
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("Publisher ");
		row.addView(tv);
		tv = myCreateTextView(b.getPublisher());
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("Pub Date ");
		row.addView(tv);
		tv = myCreateTextView(b.getPublished());
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("List Price ");
		row.addView(tv);
		tv = myCreateTextView("" + b.getListPrice());
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("Retail Price ");
		row.addView(tv);
		tv = myCreateTextView(""+ b.getRetailPrice());
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("UPC/ISBN ") ;
		row.addView(tv);
		tv = myCreateTextView(b.getUpc());
		row.addView(tv);
		table.addView(row);

	}

	private void displayPrices(int id) {
		Log.d(TAG, "displayPrices(): GOT HERE.");

		NumberFormat formatter;
		formatter = NumberFormat.getCurrencyInstance();
		// price = formatter.format(f.getPrice());

		ArrayList<PriceRec> prices = db.getPricesForId(id, " ORDER BY totalPrice ");
		ArrayList<BookRec> books = db.getItemById(id);
		if(books.size() == 0) {
			Log.d(TAG, "displayPrices(): failed to get BookRec for id " + id);
			return;
		}
		TextView title = (TextView) findViewById(R.id.bookTitle);
		title.setTextSize(BookChecker.TITLE_SIZE);
		title.setText(books.get(0).getTitle()+books.get(0).getSubTitle());
		
		TableLayout table = (TableLayout) findViewById(R.id.detailTable);
		table.removeAllViews();

		int numRecords = prices.size();
		if (numRecords == 0) {
			TableRow row = new TableRow(ctx);
			TextView tv = myCreateTextView("No price data");
			row.addView(tv);
			table.addView(row);
			return;
		}

		// do the math for the top lines.
		double sumOfPrices = 0;
		double meanPrice;
		double medianPrice = 0;
		if (numRecords > 2) {
			medianPrice = prices.get(numRecords / 2).getTotalPrice();
		}

		for (PriceRec p : prices) {
			sumOfPrices += p.getTotalPrice();
		}

		meanPrice = sumOfPrices / (double) numRecords;

		TextView tv;
		TableRow row;

		// create top rows for the table...
		row = new TableRow(ctx);
		tv = myCreateTextView("median " + formatter.format(medianPrice));
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("mean " + formatter.format(meanPrice));
		row.addView(tv);
		table.addView(row);

		// record sequence is assumed to be ascending based on the ORDER BY from the
		// database.
		row = new TableRow(ctx);
		tv = myCreateTextView("low " + formatter.format(prices.get(0).getTotalPrice()));
		row.addView(tv);
		table.addView(row);

		row = new TableRow(ctx);
		tv = myCreateTextView("high "
				+ formatter.format(prices.get(prices.size() - 1).getTotalPrice()));
		row.addView(tv);
		table.addView(row);
		
		row = new TableRow(ctx);
		tv = myCreateTextView("Total ");
		row.addView(tv);
		tv = myCreateTextView(" Price ");
		row.addView(tv);
		tv = myCreateTextView(" Shipping ");
		row.addView(tv);
		tv = myCreateTextView(" Vendor");
		row.addView(tv);
		
		table.addView(row);
		

		for (PriceRec p : prices) {
			tv = myCreateTextView(" "+ formatter.format(p.getTotalPrice())+ " " );
			row = new TableRow(ctx);
			row.addView(tv);

			tv = myCreateTextView(" " + formatter.format(p.getPrice())+ " ");
			row.addView(tv);

			tv = myCreateTextView(" " + formatter.format(p.getShipping())+ " " );
			row.addView(tv);

			tv = myCreateTextView(" " + p.getVendorName()+ " " );
			row.addView(tv);
			
			table.addView(row);
		}
	}

	private TextView myCreateTextView(String stuff) {
		TextView tv = new TextView(ctx);
		tv.setTextSize(BookChecker.TEXT_SIZE);
		tv.setTextColor(getResources().getColor(R.color.Black));
		// Typeface face = tv.getTypeface();
		// tv.setTypeface(face, Typeface.ITALIC);
		tv.setText(stuff);
		return tv;
	}
}
