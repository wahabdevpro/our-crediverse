import { executeQuery } from "../../utils/database.mjs";

const salesAmountByDate = async (date) => {

  let query = `SELECT DATE(T1.ended) rec_date, SUM(T1.gross_sales_amount) total_sales_amount, COUNT(*) total_count
  FROM ec_transact T1
  LEFT JOIN ec_transact T2 ON (
          T2.reversed_id = T1.id 
      AND T2.type = 'NR'
      AND T2.ret_code = 'SUCCESS')
  WHERE T1.comp_id = 2 
    AND T1.ret_code = 'SUCCESS'
    AND T1.type IN ('SL', 'ST', 'ND')
    AND T1.ended >= '${date.yesterday} 00:00:00'
    AND T1.ended <= '${date.today} 23:59:59'
    AND T2.id IS NULL 
  GROUP BY DATE(ended)`;

  return await executeQuery(query);
};

export { salesAmountByDate };
