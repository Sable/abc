public class AnonPreinit {
    public AnonPreinit() {
        this(new Runnable() { public void run() {}});
    }
    public AnonPreinit(Runnable r) {}

    // We just care that the class loads and verifies ok
    public static void main(String[] args) { }
}

