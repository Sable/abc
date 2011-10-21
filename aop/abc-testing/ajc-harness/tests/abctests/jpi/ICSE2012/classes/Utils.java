package classes;

public class Utils {
	
	public static void SendEmail(Customer cus, String msg){
		System.out.println(msg + " to: "+cus.getEmail());
	}

	public static void SendBestSellerCoupon(Customer cus, Item it){
		SendEmail(cus,"Sending a Best Seller coupon");
	}

	public static void SendEcoFriendlyCoupon(Customer cus, Item it) {
		SendEmail(cus,"Sending an Eco Friendly coupon");
	}

	public static void RegisterEvent(String kind, String message){
		System.out.println("Registering "+kind + " details :"+message);
	}
	
}
