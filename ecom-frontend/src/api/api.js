import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080/api",  // ✅ HARD CODED GATEWAY
    withCredentials: false,
});

api.interceptors.request.use(
    (config) => {
        const auth = JSON.parse(localStorage.getItem("auth"));
        if (auth && auth.accessToken) {
            config.headers.Authorization = `Bearer ${auth.accessToken}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;