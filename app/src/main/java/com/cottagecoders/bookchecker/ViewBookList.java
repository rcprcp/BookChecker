package com.cottagecoders.bookchecker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ViewBookList extends Activity {
	static final String TAG = "ViewBookList";
	DatabaseCode db;
	static Context ctx;
	static Activity act;

	Button name;
	Button author;
	Button upc;
	ToggleButton toggle;

	// use these for passing data in Intents.
	static final String ID = "uid";

	// various order by clauses for sort order...
	static final String[] orderBy = new String[] { " ORDER BY title ",
			" ORDER BY author ", " ORDER BY upc" };
	int sortMode = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ctx = this.getApplicationContext();
		act = this;
		db = new DatabaseCode(ctx);

		Log.d(TAG, "onCreate(): got here");

		setContentView(R.layout.view_book_list);

		name = (Button) findViewById(R.id.byName);
		name.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sortMode = 0;
				buildTable();
			}
		});

		author = (Button) findViewById(R.id.byAuthor);
		author.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sortMode = 1;
				buildTable();
			}
		});
		
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// savedInstanceState.putStringArrayList();

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// Restore state members from saved instance
		// isbns = savedInstanceState.getStringArrayList(ISBNS);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.d(TAG, "onResume(): GOT HERE");
		// just pick a default sort order...
		sortMode = 0;
		buildTable();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void buildTable() {
		Log.d(TAG, "buildTable(): GOT HERE.");
		ArrayList<BookRec> books = db
				.fetchAllBooks(orderBy[sortMode]);

		TextView count = (TextView) findViewById(R.id.recordCount);
		count.setText(Integer.toString(books.size()));

		TableLayout table = (TableLayout) findViewById(R.id.table);
		table.removeAllViews();

		// build a table of book information.
		for (BookRec b : books) {
			Log.d(TAG, "buildTable(): book title " + b.getTitle());

			// get the row ready to be added to the list.
			TableRow row = new TableRow(ctx);
			
			row.setClickable(true);
			row.setOnClickListener(new View.OnClickListener() {
				public void onClick(final View v) {
					BookRec b = (BookRec) v.getTag();

					Log.d(TAG, "onclicklistener(): got here for title: "
							+ b.getTitle() + " " + b.getSubTitle() + " upc " + b.getUpc());
					Intent intent = new Intent(ctx, ViewBookDetails.class);
					intent.putExtra(ViewBookList.ID, b.getId());
					startActivity(intent);

				}
			});

			row.setLongClickable(true);
			row.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View v) {
					BookRec b = (BookRec) v.getTag();

					Log.d(TAG, "onLongClickListener(): got here for " + b.getTitle() + " : " + b.getSubTitle());
					return true;
				}
			});

			row.setTag(b);
			TextView tv;

			if (sortMode == 0) {
				tv = myCreateTextView(b.getTitle() + " " + b.getSubTitle());
				row.addView(tv);

			} else if (sortMode == 1) {
				tv = myCreateTextView(b.getAuthor());
				row.addView(tv);

			} else if (sortMode == 2) {
				tv = myCreateTextView(b.getUpc());
				row.addView(tv);

			}

			Log.d(TAG, "buildTable(): adding row " + row);
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