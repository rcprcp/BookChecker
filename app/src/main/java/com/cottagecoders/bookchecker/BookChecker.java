package com.cottagecoders.bookchecker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class BookChecker extends Activity {
	private ArrayList<String> upcs;
	static final String UPCs = "UPCs";
	static final String TAG = "BookChecker";

	// data sources, used in the PriceRec.
	static final int BOOKFINDER = 1;

	//font sizes.
	static final float TITLE_SIZE = (float)20;
	static final float TEXT_SIZE = (float)18;
	
	Context ctx;
	Activity act;
	DatabaseCode db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ctx = this.getApplicationContext();
		act = this;
		db = new DatabaseCode(ctx);

		act.setTitle("Book Checker");

		upcs = new ArrayList<>();
		setContentView(R.layout.book_checker_activity);
		findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				{
					// initialize the list...
					upcs.clear();
					callBarcodeScanner();
				}
			}
		});
		findViewById(R.id.view).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				{
					displayResults();
				}
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putStringArrayList(UPCs, upcs);

		// Always call the superclass so it can save the view hierarchy state
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Always call the superclass so it can restore the view hierarchy
		super.onRestoreInstanceState(savedInstanceState);

		// Restore state members from saved instance
		upcs = savedInstanceState.getStringArrayList(UPCs);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		Log.d(TAG, "onActivityResult(): GOT HERE, scanResult = " + scanResult);

		if (scanResult != null) {
			// Scan next barcode if the user does not cancel the previous
			// scanning action
			if (scanResult.getContents() != null) {
				upcs.add(scanResult.getContents());
				TextView log = (TextView) findViewById(R.id.log);
				log.setText(scanResult.toString());

				callBarcodeScanner();
			} else {
				if (!upcs.isEmpty()) {
					new GetInfo().execute();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		TextView log = (TextView) findViewById(R.id.log);
		log.setText(upcs.toString());
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void callBarcodeScanner() {
		IntentIntegrator integrator = new IntentIntegrator(BookChecker.this);
		integrator.addExtra("PROMPT_MESSAGE",
				"Scan the ISBN barcode or press Back when done");
		integrator.addExtra("RESULT_DISPLAY_DURATION_MS", "1L");
		integrator.initiateScan(Collections.singleton("EAN_13"));
	}

	private class GetInfo extends AsyncTask<Void, Integer, Integer> {
		ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			Log.d(TAG, "AsyncTask - onPreExecute");
			progress = new ProgressDialog(act);
			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progress.setIndeterminate(false);
			progress.setCancelable(true);
			progress.setMax(upcs.size());
			progress.setTitle("Processing...");
			progress.setMessage("HaHa you're doomed.");
			progress.show();

		}

		@Override
		protected Integer doInBackground(Void... Void) {
			// create url - the example...
			// https://www.googleapis.com/books/v1/volumes?q=flowers+inauthor:keyes&key=yourAPIKey

			int numDone = 0;
			for (String upc : upcs) {
				BookRec item;

				// get "master" book information from google.
				// if google fails to find it, book object will be null.
				item = getGoogleInfo(upc);

				if (item != null) {
					int theId = db.insertIntoItemTable(item);
					ArrayList<PriceRec> prices;

					// get pricing info from BookFinder...
					prices = getBookFinderUsedBookPrices(upc, theId);
					db.addPrices(prices);
					prices.clear();

				} else {
					Log.d(TAG, "What is this?  All-powerful Google does not know this book!");
				}

				numDone++;
				publishProgress((int) ((numDone / (float) upcs.size()) * 100));
				if (isCancelled()) {
					break;
				}
			}
			return 1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.d(TAG, "AsyncTask - onPostExecute");
			displayResults();
			progress.dismiss();
		}
	}

	private BookRec getGoogleInfo(String upc) {
		String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:"
				+ upc;

		Log.d(TAG, "url: " + url);

		String output = null;
		try {
			output = myHttpGET(url);
		} catch (Exception e) {
			Log.d(TAG, "getGoogleInfo: http fail... " + e);
		}

		// google's reply is in JSON format.
		Log.v(TAG, "output: " + output);

		// JSON testing and debugging code.
		JSONObject json = null;
		try {
			json = new JSONObject(output);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int totalItems = 0;
		try {
			totalItems = json.getInt("totalItems");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "totalitems exception " + e);
		}
		Log.d(TAG, "totalItems is " + totalItems);
		if (totalItems == 0) {
			return null;
		}
		if (totalItems > 1) {
			Log.d(TAG,
					"getGoogleInfo(): more than 1 item returned for this ISBN "
							+ upc);
			return null;
		}

		JSONArray j2 = null;
		try {
			j2 = json.getJSONArray("items");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "items array exception " + e);
			return null;
		}

		Log.d(TAG, "j2 length " + j2.length());

		try {
			json = j2.getJSONObject(0);
		} catch (JSONException e) {
			Log.d(TAG, "get index 0 " + e);
		}

		String isbn10 = "";
		String title = "";
		String subTitle = "";
		String authors = "";
		String publisher = "";
		String published = "";
		double listPrice = 0.0;
		double retailPrice = 0.0;
		String currency = "";
		String imagePath = "";

		JSONObject vol = null;
		try {
			vol = json.getJSONObject("volumeInfo");
		} catch (JSONException e) {
			Log.d(TAG, "failed in volumeInfo " + e);
		}

		try {
			title = vol.getString("title");
		} catch (JSONException e) {
			Log.d(TAG, "failed in title " + e);
		}

		try {
			subTitle = vol.getString("subtitle");
		} catch (JSONException e) {
			Log.d(TAG, "failed in subtitle " + e);
		}

		try {
			JSONArray a = vol.getJSONArray("authors");
			for (int i = 0; i < a.length(); i++) {
				authors += a.getString(i);
				if (a.length() - 1 > i) {
					authors += ", ";
				}
			}
		} catch (JSONException e) {
			Log.d(TAG, "failed in author " + e);
		}

		try {
			JSONArray a = vol.getJSONArray("industryIdentifiers");
			for (int i = 0; i < a.length(); i++) {
				try {
					String thing = a.getJSONObject(i).getString("type");
					if (thing.contains("ISBN_10")) {
						isbn10 = a.getJSONObject(i).getString("identifier");
						break;
					}
				} catch (Exception e) {
					Log.d(TAG, "failed in ISBN_10 " + e);
				}
			}
			Log.d(TAG, "ISBN 10 " + isbn10);
		} catch (JSONException e) {
			Log.d(TAG, "failed in industryIdentifiers " + e);
		}

		try {
			published = vol.getString("publishedDate");
		} catch (JSONException e) {
			Log.d(TAG, "failed in publishedDate " + e);
		}

		try {
			publisher = vol.getString("publisher");
		} catch (JSONException e) {
			Log.d(TAG, "failed in publisher " + e);
		}

		// TODO: fix condition someday.
		int defaultCondition = 1;
		int id = 0; // placeholder
		BookRec book = new BookRec(id, upc, isbn10, title, subTitle, authors,
				publisher, published, listPrice, retailPrice, currency,
				imagePath, defaultCondition);
		return book;
	}

	private String myHttpGET(String u) throws IOException {

		URL url = null;
		url = new URL(u);

		HttpURLConnection httpconn = null;
		httpconn = (HttpURLConnection) url.openConnection();

		BufferedReader input = null;
		if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			input = new BufferedReader(new InputStreamReader(
					httpconn.getInputStream()), 8192);
		}
		StringBuilder response = new StringBuilder();
		String strline = null;
		while ((strline = input.readLine()) != null) {
			response.append(strline);
		}
		input.close();
		return response.toString();
	}

	private ArrayList<PriceRec> getBookFinderUsedBookPrices(String isbn,
			int theId) {
		// http://www.bookfinder.com/search/?author=&title=&lang=en&isbn=9780321804334&new_used=U&destination=us&currency=USD&mode=basic&st=sr&ac=qr
		String url = "http://www.bookfinder.com/search/?isbn=" + isbn
				+ "&mode=isbn&st=sr&ac=qr";
		String output = null;
		try {
			output = myHttpGET(url);
		} catch (Exception e) {
			Log.d(TAG, "getBookFinder: http fail... " + e);
			return null;
		}

		ArrayList<PriceRec> prices = new ArrayList<PriceRec>();

		while (true) {
			int st;
			int end;
			String blockStart = "overlib('";
			String blockEnd = "','BGCLASS";
			st = output.indexOf(blockStart);
			end = output.indexOf(blockEnd);
			if (st == -1 || end == -1) {
				break;
			}

			// Log.d(TAG, "st " + st + " end " + end);
			String myString = fixHex(output.substring(st, end));

			// adjust the output to point to the next vendor block.
			end += blockEnd.length();
			output = output.substring(end);

			// parse vendor name
			String searchFor = "Buy this book from ";
			st = myString.indexOf(searchFor);
			if (st == -1) {
				continue;
			}
			st += searchFor.length();
			end = myString.indexOf("</p>");
			String name = myString.substring(st, end);
			// Log.d(TAG, "name \"" + name + "\"");

			// shift the string.
			myString = myString.substring(end);
			// Log.d(TAG, "bookFinder: myString \"" + myString + "\"");

			// parse book price.
			searchFor = "Book price</th><td>$";
			st = myString.indexOf(searchFor);
			st += searchFor.length();
			myString = myString.substring(st);

			end = myString.indexOf("</td>");
			double price = Double.parseDouble(myString.substring(0, end));
			// Log.d(TAG, "price \"" + price + "\"");

			// shift the string.
			myString = myString.substring(end);
			// Log.d(TAG, "bookFinder: myString \"" + myString + "\"");

			// parse shipping cost.
			searchFor = "Shipping cost</th><td>$";
			st = myString.indexOf(searchFor);
			st += searchFor.length();
			myString = myString.substring(st);

			end = myString.indexOf("</td>");
			double shipping = Double.parseDouble(myString.substring(0, end));
			// Log.d(TAG, "shipping \"" + shipping + "\"");

			Log.d(TAG, "bookFinder: name \"" + name + "\" price " + price
					+ " shipping " + shipping);

			PriceRec p = new PriceRec(theId, "(BF)" + name, price, shipping,
					price + shipping, BOOKFINDER);
			prices.add(p);
		}

		return prices;
	}

	private void displayResults() {
		// start new intent to control viewing books.
		// start activity to add a store.
		Intent intent = new Intent(ctx, ViewBookList.class);
		startActivity(intent);

	}

	private String fixHex(String input) {

		StringBuilder val = new StringBuilder();
		StringBuilder output = new StringBuilder();
		// backslash puts us in state 1, where we skip the 'x', then state 2 to
		// get the next character, then state 3
		// to get the second character and convert from hex to a char.
		// then back to state 0 looking for the next
		// backslash.
		// Log.d(TAG, "fixHex: GOT HERE. string length" + input.length());
		int state = 0;
		for (int i = 0; i < input.length(); i++) {
			if (state == 0) {
				// check for backslash...
				if (input.substring(i, i + 1).contains("\\")) {
					state++;
					val = new StringBuilder(); // reset this.
				} else {
					// not a backslash, pass it through to the output string.
					// output += input.substring(i, i + 1);
					output.append(input.substring(i, i + 1));
				}

			} else if (state == 1) {
				// skip the 'x'...
				state++;

			} else if (state == 2) {
				// save the (ASCII character) first hex digit in a string....
				// val = input.substring(i, i + 1);
				val.append(input.substring(i, i + 1));
				state++;

			} else if (state == 3) {
				// save second hex digit in the string...
				// val += input.substring(i, i + 1);
				val.append(input.substring(i, i + 1));

				// convert two digit hex string to int data type....
				int x = Integer.parseInt(val.toString(), 16);

				// cast int to char, and append it to the output string.
				// output += (char) x;
				output.append((char) x);
				state = 0;

			}
		}
		Log.d(TAG, "fixHex(): output: \"" + output + "\"");
		return output.toString();
	}
}
