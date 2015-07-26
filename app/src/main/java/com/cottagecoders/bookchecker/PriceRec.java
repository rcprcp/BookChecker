package com.cottagecoders.bookchecker;

public class PriceRec {
	private int id;
	private String vendorName;
	private double price;
	private double shipping;
	private double totalPrice;
	private int source;
	
	public PriceRec(int id, String vendorName, double price, double shipping, double totalPrice, int source) {
		this.id = id;
		this.vendorName = vendorName;
		this.price = price;
		this.shipping = shipping;
		this.totalPrice = totalPrice;
		this.source = source;
		
	}

	public int getId() {
		return id;
	}

	public String getVendorName() {
		return vendorName;
	}

	public double getPrice() {
		return price;
	}

	public double getShipping() {
		return shipping;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public int getSource() {
		return source;
	}
}
