package static_ref_in;

///import static static_in.TestStaticImportRead.getX;

public class StaticImportReadReference {
	public void foo() {
		int y= static_in.TestStaticImportRead.///
			   getX();
	}
}
