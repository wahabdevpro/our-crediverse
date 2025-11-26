import Api from "./Api";
import jwt from "jsonwebtoken";

const login = async (data) => {
    try {
        const response = await Api().post(
            "credistats/login/",
            data
        );

        const { accessToken } = await response.data;
        const userInfo = decodeToken(accessToken);
        setUserSession(JSON.stringify(userInfo), accessToken);
        return true;
    } catch (error) {
        return error;
    }
};

const setUserSession = (userInfo, accessToken) => {
    localStorage.setItem("userInfo", userInfo);
    localStorage.setItem("accessToken", accessToken);
};

const getUserSession = () => {
    const userInfo = JSON.parse(localStorage.getItem("userInfo"));
    const accessToken = localStorage.getItem("accessToken");
    return { userInfo, accessToken };
};

const decodeToken = (token) => {
    try {
        const decoded = jwt.decode(token);
        return decoded;
    } catch (error) {
        return null;
    }
};

const logout = () => {
    localStorage.removeItem("userInfo");
    localStorage.removeItem("accessToken");
};

const isTokenValid = () => {
    const userInfo = localStorage.getItem("userInfo");
    if (!userInfo) return false;

    const { exp } = JSON.parse(userInfo);
    if (!exp) return false;

    return new Date().getTime() < exp * 1000;
};

export { login, logout, isTokenValid, getUserSession };
