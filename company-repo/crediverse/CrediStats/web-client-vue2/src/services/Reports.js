import Api from "./Api";

const getSalesAmount = async () => {
    try {
        const response = await Api().get("/credistats/daily_sales");
        const result = response.data;
        return result;
    } catch (error) {
        throw new Error(error);
    }
};

const getSalesReport = async (date, aggregation) => {
    try {
        const response = await Api().get("/credistats/report", date, aggregation);
        const result = response.data;
        return result;
    } catch (error) {
        throw new Error(error);
    }
};

export { getSalesAmount, getSalesReport };
