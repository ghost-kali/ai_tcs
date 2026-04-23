const initialState = {
    user: null,
    address: [],
    selectedUserCheckoutAddress: JSON.parse(localStorage.getItem("CHECKOUT_ADDRESS")),
}

export const authReducer = (state = initialState, action) => {
    switch (action.type) {
        case "LOGIN_USER":
            return { ...state, user: action.payload };
        case "USER_ADDRESS":
            return { ...state, address: action.payload };
        case "SELECT_CHECKOUT_ADDRESS":
            return { ...state, selectedUserCheckoutAddress: action.payload };
        case "REMOVE_CHECKOUT_ADDRESS":
            return { ...state, selectedUserCheckoutAddress: null };
        case "LOG_OUT":
            return { 
                user: null,
                address: null,
                selectedUserCheckoutAddress: null,
             };
             
        default:
            return state;
    }
};
