//Listing 4.6 TestHome.java: a simple test to see the effect of multiple advice on a join point

public class TestHome {
    public static void main(String[] args) {
	Home home = new Home();
	home.exit();
	System.out.println();
	home.enter();
    }
}
