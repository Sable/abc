//Listing 6.8 An example of a class that sets the value of a table cell

import javax.swing.table.*;

public class TableValueSetter implements Runnable {
    TableModel _model;
    Object _value;
    int _row;
    int _column;

    public TableValueSetter(TableModel model, Object value,
			    int row, int column) {
	_model = model;
	_value = value;
	_row = row;
	_column = column;
    }

    public void run() {
	_model.setValueAt(_value, _row, _column);
    }
}
