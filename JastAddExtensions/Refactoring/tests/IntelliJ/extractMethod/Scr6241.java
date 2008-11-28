import java.util.*;

class Test extends GregorianCalendar {
    public boolean isSaturday() {
        return /*[*/get( Calendar.DAY_OF_WEEK )/*]*/ == SATURDAY;
    }    
}