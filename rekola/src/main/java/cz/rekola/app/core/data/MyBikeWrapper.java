package cz.rekola.app.core.data;

import cz.rekola.app.api.model.BorrowedBike;
import cz.rekola.app.api.model.LockCode;

/**
 * Holds the BorrowedBike or LockCode information if a bike is borrowed.
 *
 * If null then it is not known whether a bike is borrowed.
 */
public class MyBikeWrapper {

	/**
	 * If null then no bike is borrowed or waiting for an update when lockCode is present.
	 */
	public final BorrowedBike bike;

	/**
	 * Partial information about new borrowed bike. Available until bike is updated.
	 */
	public final LockCode lockCode;

	public MyBikeWrapper() {
		bike = null;
		lockCode = null;
	}

	public MyBikeWrapper(BorrowedBike bike) {
		this.bike = bike;
		lockCode = null;
	}

	public MyBikeWrapper(LockCode lockCode) {
		bike = null;
		this.lockCode = lockCode;
	}

	public boolean isBorrowed() {
		return (bike != null || lockCode != null);
	}

}
