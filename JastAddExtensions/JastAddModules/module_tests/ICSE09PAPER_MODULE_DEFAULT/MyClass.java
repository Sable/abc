module somemodule;

import ::javax.swing.Action;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;

public class MyClass {
	public Action a = new Action() {
		public Object getValue(String key) {
			return null;
		}
		public void putValue(String key, Object value) {
			return;
		}
		public void setEnabled(boolean b) {
			return;
		}
		public boolean isEnabled() {
			return false;
		}
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			return;
		}
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			return;
		}
		public void actionPerformed(ActionEvent e) {
			return; 
		}
	};
	public MyClass() {
		System.out.println(a.getClass());
		System.out.println(this.getClass());
	}
}
