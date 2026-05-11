import api from "../../api/api";

export const fetchProducts = (queryString) => async (dispatch) => {
  try {
    dispatch({ type: "IS_FETCHING" });
    const qs = new URLSearchParams(queryString);
    const keyword = qs.get("keyword");
    const categoryId = qs.get("categoryId");
    const page = Number(qs.get("page") ?? 0);
   const size = Number(qs.get("size") ?? 8);

    let data;
    if (keyword && categoryId) {
      const res = await api.get(
        `/products/search?keyword=${encodeURIComponent(keyword)}&categoryId=${encodeURIComponent(categoryId)}&page=${page}&size=${size}`,
      );
      data = res.data;
    } else if (keyword) {
      const res = await api.get(`/products/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`);
      data = res.data;
    } else if (categoryId) {
      const res = await api.get(`/products/category/${categoryId}?page=${page}&size=${size}`);
      data = res.data;
    } else {
  const res = await api.get(`/products?${queryString}`);
  data = res.data;
}

    // Spring `Page<T>` compatibility: support both custom and default field names.
    const pageNumber = data.pageNumber ?? data.number ?? data.pageable?.pageNumber ?? 0;
    const pageSize = data.pageSize ?? data.size ?? data.pageable?.pageSize ?? 8;
    const lastPage = data.lastPage ?? data.last ?? false;

    dispatch({
      type: "FETCH_PRODUCTS",
      payload: data.content,
      pageNumber,
      pageSize,
      totalElements: data.totalElements,
      totalPages: data.totalPages,
      lastPage,
    });
    dispatch({ type: "IS_SUCCESS" });
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error?.response?.data?.message || "Failed to fetch products",
    });
  }
};

export const fetchCategories = () => async (dispatch) => {
  try {
    dispatch({ type: "CATEGORY_LOADER" });

    const { data } = await api.get(`/categories`);

    dispatch({
      type: "FETCH_CATEGORIES",
      payload: data, // ✅ FIXED
    });

    dispatch({ type: "IS_SUCCESS" });
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error?.response?.data?.message || "Failed to fetch categories",
    });
  }
};

export const addToCart =
  (data, qty = 1, toast) =>
  (dispatch, getState) => {
    // Find the product
    const { products } = getState().products;
    const getProduct = products.find(
      (item) => item.productId === data.productId,
    );

    // Check for stocks
    const isQuantityExist = getProduct.quantity >= qty;

    // If in stock -> add
    if (isQuantityExist) {
      dispatch({ type: "ADD_CART", payload: { ...data, quantity: qty } });
      toast.success(`${data?.productName} added to the cart`);
      localStorage.setItem("cartItems", JSON.stringify(getState().carts.cart));
    } else {
      // error
      toast.error("Out of stock");
    }
  };

export const increaseCartQuantity =
  (data, toast, currentQuantity, setCurrentQuantity) =>
  (dispatch, getState) => {
    // Find the product
    const { products } = getState().products;

    const getProduct = products.find(
      (item) => item.productId === data.productId,
    );

    const isQuantityExist = getProduct.quantity >= currentQuantity + 1;

    if (isQuantityExist) {
      const newQuantity = currentQuantity + 1;
      setCurrentQuantity(newQuantity);

      dispatch({
        type: "ADD_CART",
        payload: { ...data, quantity: newQuantity + 1 },
      });
      localStorage.setItem("cartItems", JSON.stringify(getState().carts.cart));
    } else {
      toast.error("Quantity Reached to Limit");
      
    }
  };

export const decreaseCartQuantity =
  (data, newQuantity) => (dispatch, getState) => {
    dispatch({
      type: "ADD_CART",
      payload: { ...data, quantity: newQuantity },
    });
    localStorage.setItem("cartItems", JSON.stringify(getState().carts.cart));
  };

export const removeFromCart = (data, toast) => (dispatch, getState) => {
  dispatch({ type: "REMOVE_CART", payload: data });
  toast.success(`${data.productName} removed from cart`);
  localStorage.setItem("cartItems", JSON.stringify(getState().carts.cart));
};

export const authenticateSignInUser =
  (sendData, toast, reset, navigate, setLoader) =>
  async (dispatch, getState) => {
    try {
      setLoader(true);

      // 🔐 Login API
      const { data } = await api.post("/auth/signin", sendData);

      // ✅ Store user
      dispatch({ type: "LOGIN_USER", payload: data });
      localStorage.setItem("auth", JSON.stringify(data));

      // 🛒 STEP 1: Get local cart
      const localCartItems = JSON.parse(localStorage.getItem("cartItems"));

      // 🛒 STEP 2: If local cart exists → send to backend
      if (localCartItems && localCartItems.length > 0) {
        await dispatch(createUserCart(localCartItems));

        // 🧹 STEP 3: Clear local cart
        localStorage.removeItem("cartItems");
      }

      // 🛒 STEP 4: Always fetch backend cart
      await dispatch(getUserCart());

      // ✅ UI handling
      reset();
      toast.success("Login Success");
      navigate("/");
    } catch (error) {
      console.log(error);
      toast.error(error?.response?.data?.message || "Internal Server Error");
    } finally {
      setLoader(false);
    }
  };

export const registerNewUser =
  (sendData, toast, reset, navigate, setLoader) => async (dispatch) => {
    try {
      setLoader(true);
      const { data } = await api.post("/auth/signup", sendData);
      reset();
      toast.success(data?.message || "User Registered Successfully");
      navigate("/login");
    } catch (error) {
      console.log(error);
      toast.error(
        error?.response?.data?.message ||
          error?.response?.data?.password ||
          "Internal Server Error",
      );
    } finally {
      setLoader(false);
    }
  };

export const logOutUser = (navigate) => (dispatch) => {
  dispatch({ type: "LOG_OUT" });
  localStorage.removeItem("auth");
  navigate("/login");
};

export const addUpdateUserAddress =
  (sendData, toast, addressId, setOpenAddressModal) =>
  async (dispatch, getState) => {
    /*
    const { user } = getState().auth;
    await api.post(`/addresses`, sendData, {
          headers: { Authorization: "Bearer " + user.jwtToken },
        });
    */
    dispatch({ type: "BUTTON_LOADER" });
    try {
      if (!addressId) {
        const { data } = await api.post("/addresses", sendData);
      } else {
        await api.put(`/addresses/${addressId}`, sendData);
      }
      dispatch(getUserAddresses());
      toast.success("Address saved successfully");
      dispatch({ type: "IS_SUCCESS" });
    } catch (error) {
      console.log(error);
      toast.error(error?.response?.data?.message || "Internal Server Error");
      dispatch({ type: "IS_ERROR", payload: null });
    } finally {
      setOpenAddressModal(false);
    }
  };









export const addPaymentMethod = (method) => {
  return {
    type: "ADD_PAYMENT_METHOD",
    payload: method,
  };
};

export const createUserCart = (sendCartItems) => async (dispatch, getState) => {
  try {
    dispatch({ type: "IS_FETCHING" });
    await api.post("/cart/create", sendCartItems);
    await dispatch(getUserCart());
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error?.response?.data?.message || "Failed to create cart items",
    });
  }
};

export const getUserCart = () => async (dispatch, getState) => {
  try {
    dispatch({ type: "IS_FETCHING" });
    const { data } = await api.get("/carts/users/cart");

    dispatch({
      type: "GET_USER_CART_PRODUCTS",
      payload: data.products,
      totalPrice: data.totalPrice,
      cartId: data.cartId,
    });
    localStorage.setItem("cartItems", JSON.stringify(getState().carts.cart));
    dispatch({ type: "IS_SUCCESS" });
  } catch (error) {
    console.log(error);
    dispatch({
      type: "IS_ERROR",
      payload: error?.response?.data?.message || "Failed to fetch cart items",
    });
  }
};

export const placeOrder =
  (orderData, toast, navigate, setLoader) => async (dispatch, getState) => {
    try {
      setLoader(true);
      const { data } = await api.post("/orders/place", orderData);
      
      // Clear cart in backend
      try {
        await api.delete("/carts/users/cart");
      } catch (err) {
        console.error("Failed to clear cart in backend:", err);
      }

      // Clear cart locally after successful order
      localStorage.removeItem("cartItems");
      localStorage.removeItem("CHECKOUT_ADDRESS");
      dispatch({ type: "CLEAR_CART" });

      toast.success("Order placed successfully!");
      navigate("/order-confirm", { state: { orderData: data } });
    } catch (error) {
      console.log(error);
      toast.error(error?.response?.data?.message || "Failed to place order");
    } finally {
      setLoader(false);
    }
  };

export const createPayPalOrder =
  (payload, toast, setLoader) => async () => {
    try {
      setLoader(true);
      const { data } = await api.post("/payments/paypal/order", payload);
      return data;
    } catch (error) {
      console.log(error);
      toast.error(error?.response?.data?.message || "Failed to start PayPal checkout");
      return null;
    } finally {
      setLoader(false);
    }
  };

export const capturePayPalOrder = async (payPalOrderId) => {
  const { data } = await api.post(`/payments/paypal/order/${payPalOrderId}/capture`);
  return data;
};

export const finalizePayPalOrder =
  (orderData, toast, navigate, setLoader) => async (dispatch) => {
    try {
      setLoader(true);
      const { data } = await api.post("/orders/place", orderData);

      try {
        await api.delete("/carts/users/cart");
      } catch (err) {
        console.error("Failed to clear cart in backend:", err);
      }

      localStorage.removeItem("cartItems");
      localStorage.removeItem("CHECKOUT_ADDRESS");
      localStorage.removeItem("PENDING_PAYPAL_ORDER");
      dispatch({ type: "CLEAR_CART" });

      toast.success("Payment captured and order placed successfully!");
      if (navigate) {
        navigate("/order-confirm", { state: { orderData: data } });
      }
      return data;
    } catch (error) {
      console.log(error);
      toast.error(error?.response?.data?.message || "Failed to finalize PayPal order");
      return null;
    } finally {
      setLoader(false);
    }
  };

export const analyticsAction = () => async (dispatch, getState) => {
  try {
    dispatch({ type: "IS_FETCHING" });
    const { data } = await api.get("/admin/app/analytics");
    dispatch({
      type: "FETCH_ANALYTICS",
      payload: data,
    });
    dispatch({ type: "IS_SUCCESS" });
  } catch (error) {
    dispatch({
      type: "IS_ERROR",
      payload:
        error?.response?.data?.message || "Failed to fetch analytics data",
    });
  }
};

export const getOrdersForDashboard =
  (queryString, isAdmin) => async (dispatch, getState) => {
    try {
      dispatch({ type: "IS_FETCHING" });

      const state = getState();
      const resolvedIsAdmin =
        typeof isAdmin === "boolean"
          ? isAdmin
          : Boolean(state?.auth?.user?.roles?.includes("ROLE_ADMIN"));

      let resolvedQueryString = queryString;
      if (!resolvedQueryString) {
        const pageNumber = state?.order?.pagination?.pageNumber ?? 0;
        const pageSize = state?.order?.pagination?.pageSize ?? 10;
        const params = new URLSearchParams();
        params.set("pageNumber", pageNumber);
        params.set("pageSize", pageSize);
        resolvedQueryString = params.toString();
      }

      const endpoint = resolvedIsAdmin ? "/orders" : "/seller/orders";
      const { data } = await api.get(`${endpoint}?${resolvedQueryString}`);

      // Spring `Page<T>` compatibility: support both custom and default field names.
      const pageNumber = data.pageNumber ?? data.number ?? data.pageable?.pageNumber ?? 0;
      const pageSize = data.pageSize ?? data.size ?? data.pageable?.pageSize ?? 10;
      const lastPage = data.lastPage ?? data.last ?? false;

      dispatch({
        type: "GET_ADMIN_ORDERS",
        payload: data.content,
        pageNumber,
        pageSize,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        lastPage,
      });
      dispatch({ type: "IS_SUCCESS" });
    } catch (error) {
      console.log(error);
      dispatch({
        type: "IS_ERROR",
        payload:
          error?.response?.data?.message || "Failed to fetch orders data",
      });
    }
  };

export const updateOrderStatusFromDashboard =
  (orderId, orderStatus, toast, setLoader, isAdmin) =>
  async (dispatch, getState) => {
    try {
      setLoader(true);
      const endpoint = isAdmin ? "/orders/" : "/seller/orders/";
      const { data } = await api.put(`${endpoint}${orderId}/status`, {
        status: orderStatus,
      });
      toast.success(data.message || "Order updated successfully");
      await dispatch(getOrdersForDashboard());
    } catch (error) {
      console.log(error);
      toast.error(error?.response?.data?.message || "Internal Server Error");
    } finally {
      setLoader(false);
    }
  };

export const dashboardProductsAction =
  (queryString, isAdmin) => async (dispatch, getState) => {
    try {
      dispatch({ type: "IS_FETCHING" });

      const state = getState();
      const resolvedIsAdmin =
        typeof isAdmin === "boolean"
          ? isAdmin
          : Boolean(state?.auth?.user?.roles?.includes("ROLE_ADMIN"));

      let resolvedQueryString = queryString;
      if (!resolvedQueryString) {
        const pageNumber = state?.products?.pagination?.pageNumber ?? 0;
        const pageSize = state?.products?.pagination?.pageSize ?? 20;
        const params = new URLSearchParams();
        params.set("page", pageNumber);
        params.set("size", pageSize);
        resolvedQueryString = params.toString();
      }

      const endpoint = resolvedIsAdmin ? "/products" : "/seller/products";
      const { data } = await api.get(`${endpoint}?${resolvedQueryString}`);
      dispatch({
        type: "FETCH_PRODUCTS",
        payload: data.content,
        pageNumber: data.pageNumber,
        pageSize: data.pageSize,
        totalElements: data.totalElements,
        totalPages: data.totalPages,
        lastPage: data.lastPage,
      });
      dispatch({ type: "IS_SUCCESS" });
    } catch (error) {
      console.log(error);
      dispatch({
        type: "IS_ERROR",
        payload:
          error?.response?.data?.message ||
          "Failed to fetch dashboard products",
      });
    }
  };

export const updateProductFromDashboard =
  (sendData, toast, reset, setLoader, setOpen, isAdmin) => async (dispatch) => {
    try {
      setLoader(true);
      const auth = JSON.parse(localStorage.getItem("auth"));
      console.log(auth);
      const endpoint = "/products/";
      await api.put(`${endpoint}${sendData.id}`, sendData, {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      });
      toast.success("Product update successful");
      reset();
      setLoader(false);
      setOpen(false);
      await dispatch(dashboardProductsAction(undefined, isAdmin));
    } catch (error) {
      toast.error(
        error?.response?.data?.description || "Product update failed",
      );
    }
  };

export const addNewProductFromDashboard =
  (sendData, toast, reset, setLoader, setOpen, isAdmin) =>
  async (dispatch, getState) => {
    try {
      setLoader(true);
      const auth = JSON.parse(localStorage.getItem("auth"));
      console.log(auth);
      const endpoint = isAdmin ? "/products" : "/seller/categories/";
      await api.post(`${endpoint}`, sendData, {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      });
      toast.success("Product created successfully");
      reset();
      setOpen(false);
      await dispatch(dashboardProductsAction());
    } catch (error) {
      console.error(err);
      toast.error(
        err?.response?.data?.description || "Product creation failed",
      );
    } finally {
      setLoader(false);
    }
  };

export const deleteProduct =
  (setLoader, productId, toast, setOpenDeleteModal, isAdmin) =>
  async (dispatch, getState) => {
    try {
      setLoader(true);
      const auth = JSON.parse(localStorage.getItem("auth"));
      console.log(auth);
      const endpoint = "/products/";
      await api.delete(`${endpoint}${productId}`, {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      });
      toast.success("Product deleted successfully");
      setLoader(false);
      setOpenDeleteModal(false);
      await dispatch(dashboardProductsAction(undefined, isAdmin));
    } catch (error) {
      console.log(error);
      toast.error(error?.response?.data?.message || "Some Error Occured");
    }
  };

export const updateProductImageFromDashboard =
  (formData, productId, toast, setLoader, setOpen, isAdmin) =>
  async (dispatch) => {
    setLoader(true);
    try {
      const auth = JSON.parse(localStorage.getItem("auth"));
      const endpoint = isAdmin ? "/products/" : "/seller/products/";

      await api.post(`${endpoint}${productId}/image`, formData, {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      });

      toast.success("Image upload successful");
      setOpen(false);

      try {
        await dispatch(dashboardProductsAction(undefined, isAdmin));
      } catch (refreshError) {
        console.log("Failed to refresh products after image upload", refreshError);
      }
    } catch (error) {
      toast.error(
        error?.response?.data?.description ||
          error?.response?.data?.message ||
          "Product image upload failed",
      );
    } finally {
      setLoader(false);
    }
  };

export const getAllCategoriesDashboard = () => async (dispatch) => {
  dispatch({ type: "CATEGORY_LOADER" });

  try {
    const { data } = await api.get(`/categories`);

    // ✅ Transform flat list → root categories only
    const rootCategories = data.filter((cat) => cat.parentId === null);

    dispatch({
      type: "FETCH_CATEGORIES",
      payload: rootCategories, // ✅ clean tree root
      totalElements: rootCategories.length,
      totalPages: 1,
      pageNumber: 0,
      pageSize: rootCategories.length,
      lastPage: true,
    });

    dispatch({ type: "CATEGORY_SUCCESS" });
  } catch (err) {
    console.log(err);
    dispatch({
      type: "IS_ERROR",
      payload: err?.response?.data?.message || "Failed to fetch categories",
    });
  }
};

export const getCategoriesPageForDashboard =
  (queryString) => async (dispatch, getState) => {
    dispatch({ type: "CATEGORY_LOADER" });

    try {
      let resolvedQueryString = queryString;
      if (!resolvedQueryString) {
        const pageNumber = getState()?.products?.pagination?.pageNumber ?? 0;
        const pageSize = getState()?.products?.pagination?.pageSize ?? 10;
        const params = new URLSearchParams();
        params.set("pageNumber", pageNumber);
        params.set("pageSize", pageSize);
        resolvedQueryString = params.toString();
      }

      const { data } = await api.get(`/categories/page?${resolvedQueryString}`);

      dispatch({
        type: "FETCH_CATEGORIES",
        payload: data?.content ?? [],
        pageNumber: data?.pageNumber ?? 0,
        pageSize: data?.pageSize ?? 10,
        totalElements: data?.totalElements ?? 0,
        totalPages: data?.totalPages ?? 0,
        lastPage: Boolean(data?.lastPage),
      });

      dispatch({ type: "CATEGORY_SUCCESS" });
    } catch (err) {
      console.log(err);
      dispatch({
        type: "IS_ERROR",
        payload: err?.response?.data?.message || "Failed to fetch categories",
      });
    }
  };

export const createCategoryDashboardAction =
  (sendData, setOpen, reset, toast) => async (dispatch, getState) => {
    try {
      dispatch({ type: "CATEGORY_LOADER" });
      const auth = JSON.parse(localStorage.getItem("auth"));
      console.log(auth);
      console.log("TOKEN:", auth?.accessToken);
      await api.post("/categories", sendData, {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      });
      dispatch({ type: "CATEGORY_SUCCESS" });
      reset();
      toast.success("Category Created Successful");
      setOpen(false);
      await dispatch(getCategoriesPageForDashboard());
    } catch (err) {
      console.log(err);
      toast.error(
        err?.response?.data?.categoryName || "Failed to create new category",
      );

      dispatch({
        type: "IS_ERROR",
        payload: err?.response?.data?.message || "Internal Server Error",
      });
    }
  };

export const updateCategoryDashboardAction =
  (sendData, setOpen, categoryID, reset, toast) =>
  async (dispatch, getState) => {
    try {
      dispatch({ type: "CATEGORY_LOADER" });
      const auth = JSON.parse(localStorage.getItem("auth"));
      await api.put(`/categories/${categoryID}`, sendData, {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      });

      dispatch({ type: "CATEGORY_SUCCESS" });

      reset();
      toast.success("Category Update Successful");
      setOpen(false);
      await dispatch(getCategoriesPageForDashboard());
    } catch (err) {
      console.log(err);
      toast.error(
        err?.response?.data?.categoryName || "Failed to update category",
      );

      dispatch({
        type: "IS_ERROR",
        payload: err?.response?.data?.message || "Internal Server Error",
      });
    }
  };

export const deleteCategoryDashboardAction =
  (setOpen, categoryID, toast) => async (dispatch, getState) => {
    try {
      dispatch({ type: "CATEGORY_LOADER" });
      const auth = JSON.parse(localStorage.getItem("auth"));

      await api.delete(`categories/${categoryID}`, {
        headers: {
          Authorization: `Bearer ${auth?.accessToken}`,
        },
      });

      dispatch({ type: "CATEGORY_SUCCESS" });

      toast.success("Category Delete Successful");
      setOpen(false);
      await dispatch(getCategoriesPageForDashboard());
    } catch (err) {
      console.log(err);
      toast.error(err?.response?.data?.message || "Failed to delete category");
      dispatch({
        type: "IS_ERROR",
        payload: err?.response?.data?.message || "Internal Server Error",
      });
    }
  };

