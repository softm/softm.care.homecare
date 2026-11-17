package net.softm.data.kata.dto;

/**
 * CodeDTO
 * @author softm
 */
public class PurchaseDTO {
	private String orderId;
	private String packageName;
	private String productId;
	private long purchaseTime;
	private int purchaseState;
	private String purchaseToken;
	private int quantity;
	private boolean acknowledged;
//	{
//		"orderId": "GPA.3302-9345-4742-81886",
//			"packageName": "net.softm.data.kata",
//			"productId": "inapp_lifetime",
//			"purchaseTime": 1739452149811,
//			"purchaseState": 0,
//			"purchaseToken": "aelhlnhpkagmodkbohkhinji.AO-J1OyMytiB1g1rNO9P9wGjx1GEYqohGFWyJePfsEV_Y-tOs8u0t86i_2TpKoFFg8WHhKlWfEf2pmklOyiH42nGgIgw2D9AA7hKbBr8tuaBGDgCjB0IB2w",
//			"quantity": 1,
//			"acknowledged": true,
//	}

	public PurchaseDTO() {
		super();
	}
	
	

}
