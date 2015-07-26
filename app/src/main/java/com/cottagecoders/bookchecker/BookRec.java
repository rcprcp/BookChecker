package com.cottagecoders.bookchecker;

public class BookRec {

	private int id;
	private String upc;
	private String isbn10;
	private String title;
	private String subTitle;
	private String author;
	private String publisher;
	private String published;
	private double listPrice;
	private double retailPrice;
	private String currency;
	private String imagePath;
	private int condition;


	public BookRec(int id, String upc, String isbn10, String title,
			String subTitle, 
			String author, String publisher, String published,
			double listPrice, double retailPrice, String currency,
			String imagePath, int condition) {
		
		this.id = id;
		this.upc = upc;
		this.isbn10 = isbn10;
		this.title = title;
		this.subTitle = subTitle;
		this.author = author;
		this.publisher = publisher;
		this.published = published;
		this.listPrice = listPrice;
		this.retailPrice = retailPrice;
		this.imagePath = imagePath;
		this.condition = condition;
	
	}
	public int getId() {
		return id;
	}

	public String getUpc() {
		return upc;
	}

	public String getIsbn10() {
		return isbn10;
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}
	
	public String getAuthor() {
		return author;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getPublished() {
		return published;
	}

	public double getListPrice() {
		return listPrice;
	}

	public double getRetailPrice() {
		return retailPrice;
	}

	public String getCurrency() {
		return currency;
	}

	public String getImagePath() {
		return imagePath;
	}

	public int getCondition() {
		return condition;
	}
}
