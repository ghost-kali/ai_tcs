import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useSearchParams } from "react-router-dom";
import { dashboardProductsAction, fetchProducts } from "../store/actions";

const useProductFilter = () => {
    const [searchParams] = useSearchParams();
    const dispatch = useDispatch();

    useEffect(() => {
        const params = new URLSearchParams();

        // ✅ FIXED PAGE PARAM
        const currentPage = searchParams.get("page")
            ? Number(searchParams.get("page"))
            : 1;

        params.set("page", currentPage - 1); // ✅ backend expects "page"
        params.set("size", 8);              // ✅ REQUIRED

        // ✅ FIXED SORT PARAMS
        const sortOrder = searchParams.get("sortby") || "asc";

        params.set("sortBy", "price");
        params.set("sortDirection", sortOrder); // ✅ backend expects this

        // OPTIONAL FILTERS
        const categoryId = searchParams.get("categoryId");
        const keyword = searchParams.get("keyword");

        if (categoryId) {
            params.set("categoryId", categoryId);
        }

        if (keyword) {
            params.set("keyword", keyword);
        }

        const queryString = params.toString();

        console.log("✅ FIXED QUERY STRING:", queryString);

        dispatch(fetchProducts(queryString));

    }, [dispatch, searchParams]);
};

export const useDashboardProductFilter = () => {

    const { user } = useSelector((state) => state.auth);
    const isAdmin = user && user?.roles?.includes("ROLE_ADMIN");

    const [searchParams] = useSearchParams();
    const dispatch = useDispatch();

    useEffect(() => {
        const params = new URLSearchParams();

        const currentPage = searchParams.get("page")
            ? Number(searchParams.get("page"))
            : 1;

        params.set("page", currentPage - 1); // ✅ FIXED
       params.set("size", 8);             // ✅ ADD

        const queryString = params.toString();

        dispatch(dashboardProductsAction(queryString, isAdmin));

    }, [dispatch, searchParams, isAdmin]);
};

export default useProductFilter;
