//Listing 6.9 An example of a class that removes a table row

import javax.swing.table.*;

public class TableRowRemover implements Runnable {
    DefaultTableModel _model;
    int _row;

    public TableRowRemover(DefaultTableModel model, int row) {
	_model = model;
	_row = row;
    }

    public void run() {
	_model.removeRow(_row);
    }
}
