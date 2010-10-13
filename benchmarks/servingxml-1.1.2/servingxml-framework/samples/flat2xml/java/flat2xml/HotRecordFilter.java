package flat2xml;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordFilter;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.ServingXmlException;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;

public class HotRecordFilter extends AbstractRecordFilter {
  private static final Name BKP84_RECORD_TYPE = new QualifiedName("bkp84");
  private static final Name AMOUNT_NAME = new QualifiedName("amount");
  private static final Name PRECISION_NAME = new QualifiedName("precision");
  private static final Name CALCULATED_AMOUNT_NAME = new QualifiedName("calculatedAmount");
  
  public void writeRecord(ServiceContext context, 
                          Flow flow) {

    Record record = flow.getRecord();
    Flow newFlow = flow;
    if (record.getRecordType().getName().equals(BKP84_RECORD_TYPE)) {
      RecordBuilder recordBuilder = new RecordBuilder(record);
      String amountString = record.getString(AMOUNT_NAME);
      if (amountString == null) {
        throw new ServingXmlException("amount is NULL");
      }
      String precisionString = record.getString(PRECISION_NAME);
      if (precisionString == null) {
        throw new ServingXmlException("precision is NULL");
      }
      int amount = Integer.parseInt(amountString,16);
      int precision = Integer.parseInt(precisionString);
      double calculatedAmount = (double)amount/Math.pow(10.0,(double)precision);
      recordBuilder.setDouble(CALCULATED_AMOUNT_NAME,calculatedAmount);
      record = recordBuilder.toRecord();
      newFlow = flow.replaceRecord(context, record);
    }

    super.writeRecord(context, newFlow);
  }
}
