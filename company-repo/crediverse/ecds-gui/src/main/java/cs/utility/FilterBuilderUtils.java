package cs.utility;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.Transaction;

public class FilterBuilderUtils {
  private static final String CONST_FILTER_DATE_TIME_FORMAT = "yyyyMMdd'T'HHmmss";
  private static final String CONST_FILTER_VALUE_SEPARATOR = "+";

  public static BigDecimal getBigDecimal(List<Violation> violations, String value, String name, BigDecimal min) {
    DecimalFormat format = new DecimalFormat();
    format.setParseBigDecimal(true);
    ParsePosition pos = new ParsePosition(0);
    Number number = format.parse(value, pos);
    if (number == null)
      violations.add(new Violation(Violation.INVALID_VALUE, name, null, "Must be a numeric value"));
    else {
      BigDecimal bd = new BigDecimal(number.toString());
      if ((min != null) && (bd.longValue() < min.longValue()))
        violations.add(new Violation(Violation.TOO_SMALL, name, bd.toString(),
            String.format("Must be greater than or equal to %s", bd.toString())));
      return bd;
    }
    return null;
  }

  public static String getTime(List<Violation> violations, String value, String name) {
    if (!value.matches("^\\d\\d:\\d\\d$")) {
      violations.add(new Violation(Violation.INVALID_VALUE, name, null, "Must be in HH:MM format"));
      return null;
    }
    String hour = value.substring(0, 2);
    String min = value.substring(3, 5);
    String time = value.replace(":", "");

    int ihour = Integer.parseInt(hour);
    int imin = Integer.parseInt(min);

    if (ihour < 0 || ihour > 23)
      violations.add(new Violation(Violation.INVALID_VALUE, name, null, "Hour must be between 00 and 23"));
    if (imin < 0 || imin > 59)
      violations.add(new Violation(Violation.INVALID_VALUE, name, null, "Minute must be between 00 and 59"));

    return time;
  }

  public static Date getDate(List<Violation> violations, String value, String name, Date min) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    try {
      Date date = sdf.parse(value);
      if ((min != null) && (date.compareTo(min) < 0))
        violations.add(new Violation(Violation.TOO_SMALL, name, sdf.format(min),
            String.format("Must not be before %s", sdf.format(min))));
      return date;
    } catch (ParseException e) {
    }
    return null;
  }

  private static String addFilter(String filter, String name, String operator, String value) {
    if (!value.equals("")) {
      if (!filter.equals(""))
        filter += "+";
      filter += name + operator + "'" + value + "'";
    }
    return filter;
  }

  private static String dayEndFilterValue(LocalDateTime dateTime) {
    StringBuilder filter = new StringBuilder("endTime<='");
    filter.append(dateTime.withHour(23).withMinute(59).withSecond(59)
        .format(DateTimeFormatter.ofPattern(CONST_FILTER_DATE_TIME_FORMAT)));
    filter.append("'");
    return filter.toString().trim();
  }

  private static String dayStartFilterValue(LocalDateTime dateTime) {
    StringBuilder filter = new StringBuilder("startTime>='");
    filter.append(dateTime.withHour(23).withMinute(0).withSecond(0)
        .format(DateTimeFormatter.ofPattern(CONST_FILTER_DATE_TIME_FORMAT)));
    filter.append("'");
    return filter.toString().trim();
  }

  private static String relationFilterValue(String relation) {
    StringBuilder filter = new StringBuilder("relation='");
    filter.append(relation);
    filter.append("'");
    return filter.toString().trim();
  }

  public static String getLastXDaysFilter(long days) {
    return getLastXDaysFilter(days, Transaction.RELATION_ALL);
  }

  public static String getLastXDaysFilter(long days, String relation) {
    // StringBuilder filter = new StringBuilder("filter=");
    StringBuilder filter = new StringBuilder();
    filter.append(dayStartFilterValue(LocalDateTime.now().minusDays(days)));
    filter.append(CONST_FILTER_VALUE_SEPARATOR);
    filter.append(dayEndFilterValue(LocalDateTime.now()));
    filter.append(CONST_FILTER_VALUE_SEPARATOR);
    filter.append(relationFilterValue(relation));

    return filter.toString().trim();
  }

  /*
   * TODO compileFilter exists in multiple places with slightly different
   * implementations.
   * Work needs to be done to consolidate the differences. Suggest using a builder
   * pattern for this.
   */
  public static String compileFilter(Map<String, String> params, List<Violation> violations) {
    String filter = "";
    if (params.containsKey("msisdnA")) {
      String msisdn = params.get("msisdnA").trim().replaceFirst("[*]$", "%");
      if (!msisdn.matches("^[0-9]+[%]?$"))
        violations.add(new Violation(Violation.INVALID_VALUE, "msisdnA", null, "Must be numeric"));
      else
        filter = addFilter(filter, "a_MSISDN", ":", msisdn);
    }
    if (params.containsKey("msisdnB")) {
      String msisdn = params.get("msisdnB").trim().replaceFirst("[*]$", "%");
      if (!msisdn.matches("^[0-9]+[%]?$"))
        violations.add(new Violation(Violation.INVALID_VALUE, "msisdnB", null, "Must be numeric"));
      else
        filter = addFilter(filter, "b_MSISDN", ":", msisdn);
    }
    if (params.containsKey("number"))
      filter = addFilter(filter, "number", ":", params.get("number").trim().replaceFirst("[*]$", "%"));
    if (params.containsKey("type")) {
      switch (params.get("type")) {
        case Transaction.TYPE_REPLENISH:
        case Transaction.TYPE_TRANSFER:
        case Transaction.TYPE_SELL:
        case Transaction.TYPE_NON_AIRTIME_DEBIT:
        case Transaction.TYPE_NON_AIRTIME_REFUND:
        case Transaction.TYPE_REGISTER_PIN:
        case Transaction.TYPE_CHANGE_PIN:
        case Transaction.TYPE_BALANCE_ENQUIRY:
        case Transaction.TYPE_SELF_TOPUP:
        case Transaction.TYPE_TRANSACTION_STATUS_ENQUIRY:
        case Transaction.TYPE_LAST_TRANSACTION_ENQUIRY:
        case Transaction.TYPE_ADJUST:
        case Transaction.TYPE_SALES_QUERY:
        case Transaction.TYPE_DEPOSITS_QUERY:
        case Transaction.TYPE_REVERSE:
        case Transaction.TYPE_REVERSE_PARTIALLY:
        case Transaction.TYPE_PROMOTION_REWARD:
        case Transaction.TYPE_ADJUDICATE:
          filter = addFilter(filter, "type", "=", params.get("type"));
          break;
        default:
          violations.add(new Violation(Violation.INVALID_VALUE, "type", null, "Unsupported transaction type"));
          break;
      }
    }
    if (params.containsKey("channel")) {
      switch (params.get("channel")) {
        case "U":
        case "B":
        case "S":
        case "A":
        case "P":
        case "W":
          filter = addFilter(filter, "channel", "=", params.get("channel"));
          break;
        default:
          violations.add(new Violation(Violation.INVALID_VALUE, "channel", null, "Unsupported channel"));
          break;
      }
    }
    if (params.containsKey("callerID"))
      filter = addFilter(filter, "callerID", "=", params.get("callerID"));
    if (params.containsKey("groupIDA") && !params.get("groupIDA").isEmpty())
      filter = addFilter(filter, "a_GroupID", "=", params.get("groupIDA"));
    if (params.containsKey("groupIDB") && !params.get("groupIDB").isEmpty())
      filter = addFilter(filter, "b_GroupID", "=", params.get("groupIDB"));
    if (params.containsKey("agentIDA") && !params.get("agentIDA").isEmpty())
      filter = addFilter(filter, "a_AgentID", "=", params.get("agentIDA"));
    if (params.containsKey("agentIDB") && !params.get("agentIDB").isEmpty())
      filter = addFilter(filter, "b_AgentID", "=", params.get("agentIDB"));

    if (params.containsKey("a_TierID") && !params.get("a_TierID").isEmpty())
      filter = addFilter(filter, "a_TierID", "=", params.get("a_TierID"));
    if (params.containsKey("b_TierID") && !params.get("b_TierID").isEmpty())
      filter = addFilter(filter, "b_TierID", "=", params.get("b_TierID"));

    if (params.containsKey("a_OwnerID") && !params.get("a_OwnerID").isEmpty())
      filter = addFilter(filter, "a_OwnerAgentID", "=", params.get("a_OwnerID"));
    if (params.containsKey("b_OwnerID") && !params.get("b_OwnerID").isEmpty())
      filter = addFilter(filter, "b_OwnerAgentID", "=", params.get("b_OwnerID"));

    if (params.containsKey("returnCode") && !params.get("returnCode").isEmpty())
      filter = addFilter(filter, "returnCode", "=", params.get("returnCode"));

    if (params.containsKey("followUp") && params.get("followUp").equals("A"))
      filter = addFilter(filter, "followUp", "=", "true");
    if (params.containsKey("followUp") && params.get("followUp").equals("P"))
      filter = addFilter(filter, "UA", "=", "1");

    String timeFrom = "0000";
    String timeTo = "2359";

    if (params.containsKey("timeFrom")) {
      String time = FilterBuilderUtils.getTime(violations, params.get("timeFrom").trim(), "timeFrom");
      if (time != null)
        timeFrom = time;
    }
    if (params.containsKey("timeTo")) {
      String time = FilterBuilderUtils.getTime(violations, params.get("timeTo").trim(), "timeTo");
      if (time != null)
        timeTo = time;
    }

    Date dateFrom = null;
    Date dateTo = null;

    if (params.containsKey("dateFrom")) {
      dateFrom = getDate(violations, params.get("dateFrom").trim(), "dateFrom", dateFrom);
      if (dateFrom != null) {
        String dateFromStr = params.get("dateFrom");
        dateFromStr = dateFromStr.replace("-", "");
        dateFromStr += "T" + timeFrom + "00";
        filter = addFilter(filter, "endTime", ">=", dateFromStr);
      }
    }
    if (params.containsKey("dateTo")) {
      dateTo = getDate(violations, params.get("dateTo").trim(), "dateTo", dateFrom);
      if (dateTo != null) {
        String dateToStr = params.get("dateTo");
        dateToStr = dateToStr.replace("-", "");
        dateToStr += "T" + timeTo + "59";
        filter = addFilter(filter, "endTime", "<=", dateToStr);
      }
    }

    BigDecimal amountFrom = new BigDecimal(0);
    BigDecimal amountTo = new BigDecimal(0);
    BigDecimal bonusFrom = new BigDecimal(0);
    BigDecimal bonusTo = new BigDecimal(0);
    BigDecimal chargeFrom = new BigDecimal(0);
    BigDecimal chargeTo = new BigDecimal(0);

    if (params.containsKey("amountFrom")) {
      amountFrom = FilterBuilderUtils.getBigDecimal(violations, params.get("amountFrom").trim(), "amountFrom",
          amountFrom);
      if (amountFrom != null)
        filter = addFilter(filter, "amount", ">=", params.get("amountFrom"));
    }
    if (params.containsKey("amountTo")) {
      amountTo = FilterBuilderUtils.getBigDecimal(violations, params.get("amountTo").trim(), "amountTo", amountFrom);
      if (amountTo != null)
        filter = addFilter(filter, "amount", "<=", params.get("amountTo"));
    }
    if (params.containsKey("bonusAmountFrom")) {
      bonusFrom = FilterBuilderUtils.getBigDecimal(violations, params.get("bonusAmountFrom").trim(), "bonusAmountFrom",
          bonusFrom);
      if (bonusFrom != null)
        filter = addFilter(filter, "buyerTradeBonusAmount", ">=", params.get("bonusAmountFrom"));
    }
    if (params.containsKey("bonusAmountTo")) {
      bonusTo = FilterBuilderUtils.getBigDecimal(violations, params.get("bonusAmountTo").trim(), "bonusAmountTo",
          bonusFrom);
      if (bonusTo != null)
        filter = addFilter(filter, "buyerTradeBonusAmount", "<=", params.get("bonusAmountTo"));
    }
    if (params.containsKey("chargeAmountFrom")) {
      chargeFrom = FilterBuilderUtils.getBigDecimal(violations, params.get("chargeAmountFrom").trim(),
          "chargeAmountFrom", chargeFrom);
      if (chargeFrom != null)
        filter = addFilter(filter, "chargeLevied", ">=", params.get("chargeAmountFrom"));
    }
    if (params.containsKey("chargeAmountTo")) {
      chargeTo = FilterBuilderUtils.getBigDecimal(violations, params.get("chargeAmountTo").trim(), "chargeAmountTo",
          chargeFrom);
      if (chargeTo != null)
        filter = addFilter(filter, "chargeLevied", "<=", params.get("chargeAmountTo"));
    }
    if (params.containsKey("relation"))
      filter = addFilter(filter, "relation", "=", params.get("relation"));
    else
      filter = addFilter(filter, "relation", "=", Transaction.RELATION_ALL);

    return filter;
  }
}
