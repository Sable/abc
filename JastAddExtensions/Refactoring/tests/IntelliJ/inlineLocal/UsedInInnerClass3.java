// added imports for compilability
import javax.swing.*;
import java.awt.Dimension;

public class IDEA17606 {
    // inserted declaration for compilability
    class Foo extends JComponent { }

    public void foo() {
        final Preferences preferences = new Preferences();
        // try to inline 'preferences'
        final Bar bar = new Bar(/*[*/preferences/*]*/.getComponent());
        bar.toString();

        new ThreadUtils().run(new Runnable() {
            public void run() {
                final Foo foo = new Foo();
                foo.setSize(preferences.getDimension().getSize());
            }
        });
    }

    // changed slightly for compilability
    static class Preferences {
	public JComponent getComponent() {
            return null;
        }

	public Dimension getDimension() {
            return null;
	}
    }

    class Bar {
	public Bar(JComponent component) {
	}
    }

    // changed slightly for compilability
    class ThreadUtils {
        public void run(Runnable runnable) {
        }
    }
}
