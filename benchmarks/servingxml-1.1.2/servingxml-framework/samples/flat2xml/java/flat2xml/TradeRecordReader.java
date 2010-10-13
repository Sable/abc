package flat2xml;

import com.servingxml.app.ServiceContext;
import com.servingxml.components.recordio.AbstractRecordReader;
import com.servingxml.app.Flow;
import com.servingxml.util.Name;
import com.servingxml.util.QualifiedName;
import com.servingxml.util.record.Record;
import com.servingxml.util.record.RecordBuilder;

public class TradeRecordReader extends AbstractRecordReader {
  private static final Name TRADE_RECORD_TYPE = new QualifiedName("trade");
  private static final Name TRANSACTION_RECORD_TYPE = new QualifiedName("transaction");
  private static final Name RECORD_TYPE_NAME = new QualifiedName("record_type");
  private static final Name ID_NAME = new QualifiedName("id");
  private static final Name TRADE_DATE_NAME = new QualifiedName("trade_date");
  private static final Name TRADE_TIME_NAME = new QualifiedName("trade_time");
  private static final Name DESCRIPTION_NAME = new QualifiedName("description");
  private static final Name REFERENCE_NAME = new QualifiedName("reference");
  
  public void readRecords(ServiceContext context, Flow flow) {

    //  Start the record stream
    startRecordStream(context, flow);

    RecordBuilder trRecordBuilder = new RecordBuilder(TRADE_RECORD_TYPE);
    RecordBuilder tnRecordBuilder = new RecordBuilder(TRANSACTION_RECORD_TYPE);

    Record record;

    trRecordBuilder.setString(RECORD_TYPE_NAME,"TR");
    trRecordBuilder.setString(ID_NAME,"0001");
    trRecordBuilder.setString(TRADE_DATE_NAME,"03/25/2005");
    trRecordBuilder.setString(TRADE_TIME_NAME,"01:50:00");
    trRecordBuilder.setString(DESCRIPTION_NAME,"This is a trade record");
    record = trRecordBuilder.toRecord();
    trRecordBuilder.clear();

    //  Write the "trade" record
    Flow newFlow = flow.replaceRecord(context, record);
    writeRecord(context, newFlow);

    tnRecordBuilder.setString(RECORD_TYPE_NAME,"TN");
    tnRecordBuilder.setString(ID_NAME,"0002");
    tnRecordBuilder.setString(REFERENCE_NAME,"X1234");
    tnRecordBuilder.setString(DESCRIPTION_NAME,"A child transaction");
    record = tnRecordBuilder.toRecord();   
    tnRecordBuilder.clear();

    //  Write the first "transaction" record
    newFlow = flow.replaceRecord(context, record);
    writeRecord(context, newFlow);

    tnRecordBuilder.setString(RECORD_TYPE_NAME,"TN");
    tnRecordBuilder.setString(ID_NAME,"0003");
    tnRecordBuilder.setString(REFERENCE_NAME,"X1235");
    tnRecordBuilder.setString(DESCRIPTION_NAME,"Another child transaction");
    record = tnRecordBuilder.toRecord();
    tnRecordBuilder.clear();

    //  Write the second "transaction" record
    newFlow = flow.replaceRecord(context, record);
    writeRecord(context, newFlow);

    //  End the record stream
    endRecordStream(context, flow);
  }
}                              
