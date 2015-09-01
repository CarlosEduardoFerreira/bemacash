package android.cashdrawer;

public class CashDrawer {
	private static final String TAG = "CashDrawer";
	
/* CashDrawer.
 * 
 */
	public CashDrawer() throws SecurityException {
	}

	// Getters and setters
	public void openCashDrawer() {
		open();
	}

	public int getCashDrawerStatus() {
		return status();
	}

	// JNI
	private native static void open();
	public native int status();
	static {
		System.loadLibrary("cash_drawer");
	}
}
