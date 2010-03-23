package static_ref_in;

///import static static_in.TestStaticImportWrite.setX;

public class StaticImportWriteReference {
	public void foo() {
		static_in.TestStaticImportWrite.///
		setX(10);
	}
}
