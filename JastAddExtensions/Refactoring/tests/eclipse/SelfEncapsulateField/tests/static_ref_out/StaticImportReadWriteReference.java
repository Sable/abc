package static_ref_in;

///import static static_in.TestStaticImportReadWrite.getX;
///import static static_in.TestStaticImportReadWrite.setX;

public class StaticImportReadWriteReference {
	public void foo() {
		static_in.TestStaticImportReadWrite.///
		setX(
				static_in.TestStaticImportReadWrite.///
				getX() + 10);
	}
}
