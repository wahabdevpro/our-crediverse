import { salesAmountByDate } from "../../models/StatsModel/Stats.mjs";
import { format } from "date-fns";
import { reportByMonth } from "../../models/StatsModel/Report.mjs";

const getSalesAmount = async (req, res) => {
  try {
    const today = new Date();

    const yesterday = new Date(today);
    yesterday.setDate(today.getDate() - 1);

    let date = {
      yesterday: format(yesterday, "yyyy-MM-dd"),
      today: format(today, "yyyy-MM-dd"),
    };

    let sales = await salesAmountByDate(date);

    let todaySales = 0;
    let yesterdaySales = 0;

    sales.forEach((sale) => {
      let rec_date = sale.rec_date;
      const salesDate = format(rec_date, "yyyy-MM-dd");
      if (salesDate == date.today) todaySales = sale.total_sales_amount;
      else if (salesDate == date.yesterday)
        yesterdaySales = sale.total_sales_amount;
    });

    const reports = {
      today_sales: todaySales,
      yesterday_sales: yesterdaySales,
    };

    res.json(reports);
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "An error occurred" });
  }
};

const getReport = async (req, res) => {
  try {
    let sales = await reportByMonth(req.date);
    res.status(200).json(sales);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Internal Server Error" });
  }
};

export { getSalesAmount, getReport };
