import axios from "axios";
import { Config } from "../config/Config";
import { getUserSession } from "./Authentication";

const axiosInstance = axios.create({
    baseURL: `${Config.SERVER_URL}:${Config.SERVER_PORT}`,
    headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
    },
});

axiosInstance.interceptors.request.use(
    (config) => {
        const { accessToken } = getUserSession();
        if (accessToken) {
            config.headers["Authorization"] = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default () => {
    return axiosInstance;
};
