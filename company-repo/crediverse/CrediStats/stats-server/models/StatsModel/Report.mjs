import { executeQuery } from "../../utils/database.mjs";

const reportByMonth = async (date, aggregation) => {
    const validAttributes = ["product", "group", "location", "channel", "distributor", "retailer"];
    const selectedAttributes = Object.keys(aggregation).filter(attr => validAttributes.includes(attr));
    
    if (selectedAttributes.length === 0) {
      throw new Error("At least one valid attribute should be provided.");
    }
  
    let selectClause = `SELECT `;
    selectedAttributes.forEach((attr, index) => {
      selectClause += `${attr === "group" ? "G.name" : attr} ${attr}_name`;
      if (index !== selectedAttributes.length - 1) {
        selectClause += ", ";
      }
    });
  
    let groupByClause = ` GROUP BY `;
    selectedAttributes.forEach((attr, index) => {
      groupByClause += `${attr === "group" ? "G.name" : attr}`;
      if (index !== selectedAttributes.length - 1) {
        groupByClause += ", ";
      }
    });
  
    const query = `
      ${selectClause}, SUM(T.amount)
      FROM ec_transact T
      LEFT JOIN et_group G ON (G.id = T.a_group)
      LEFT JOIN et_tier TR ON (TR.id = T.a_tier)
      LEFT JOIN ec_transact T2 ON (T2.reversed_id = T.id AND T2.ret_code = 'SUCCESS')
      WHERE T.started >= '${date.lastMonth} 00:00:00'
      AND T.started <= '${date.currentMonth} 23:59:59'
      AND T.ret_code = 'SUCCESS'
      AND T.comp_id = 2
      AND T2.id IS NULL
      ${groupByClause}`;
  

      return await executeQuery(query);
  };


  export { reportByMonth };
  