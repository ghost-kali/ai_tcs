import { Button, Step, StepLabel, Stepper } from '@mui/material';
import React, { useEffect, useState } from 'react'
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import Skeleton from '../shared/Skeleton';
import ErrorPage from '../shared/ErrorPage';
import OrderSummary from './OrderSummary';
import { createPayPalOrder, createUserCart } from '../../store/actions';

const Checkout = () => {
    const [activeStep, setActiveStep] = useState(0);
    const [orderPlacing, setOrderPlacing] = useState(false);
    const dispatch = useDispatch();
    const { isLoading, errorMessage } = useSelector((state) => state.errors);
    const { cart, totalPrice, cartId } = useSelector((state) => state.carts);

    const steps = [
        "Order Summary",
        "Confirmation",
    ];

    // Sync cart to backend if not already synced
    useEffect(() => {
        if (cart.length > 0 && !cartId && !errorMessage) {
            const sendCartItems = cart.map((item) => ({
                productId: item.productId,
                quantity: item.quantity,
            }));
            dispatch(createUserCart(sendCartItems));
        }
    }, [dispatch, cartId]);

    const handlePlaceOrder = async () => {
        if (!cart || cart.length === 0) {
            toast.error("Your cart is empty");
            return;
        }

        const selectedAddress = JSON.parse(localStorage.getItem("CHECKOUT_ADDRESS"));
        const orderData = {
            items: cart.map((item) => ({
                productId: item.productId,
                productName: item.productName,
                image: item.image,
                quantity: item.quantity,
                price: item.specialPrice || item.price,
            })),
            totalAmount: totalPrice || cart.reduce(
                (acc, cur) => acc + Number(cur?.specialPrice || cur?.price) * Number(cur?.quantity), 0
            ),
            addressId: selectedAddress?.addressId,
            paymentMethod: "PAYPAL",
        };

        localStorage.setItem("PENDING_PAYPAL_ORDER", JSON.stringify(orderData));

        const payPalOrder = await dispatch(createPayPalOrder({
            amount: orderData.totalAmount,
            currency: "USD",
            description: "E-commerce order checkout",
        }, toast, setOrderPlacing));

        if (payPalOrder?.approveUrl) {
            window.location.href = payPalOrder.approveUrl;
            return;
        }

        localStorage.removeItem("PENDING_PAYPAL_ORDER");
    };

  return (
    <div className='py-14 min-h-[calc(100vh-100px)]'>
        <Stepper activeStep={activeStep} alternativeLabel>
            {steps.map((label, index) => (
                <Step key={index}>
                    <StepLabel>{label}</StepLabel>
                </Step>
            ))}
        </Stepper>

        {isLoading ? (
            <div className='lg:w-[80%] mx-auto py-5'>
                <Skeleton />
            </div>
        ) : (
            <div className='mt-5'>
                {activeStep === 0 && <OrderSummary 
                                        totalPrice={totalPrice || cart.reduce(
                                            (acc, cur) => acc + Number(cur?.specialPrice || cur?.price) * Number(cur?.quantity), 0
                                        )}
                                        cart={cart} />}
            </div>
        )}

        <div
            className='flex justify-between items-center px-4 fixed z-50 h-24 bottom-0 bg-white left-0 w-full py-4 border-slate-200'
            style={{ boxShadow: "0 -2px 4px rgba(100, 100, 100, 0.15)" }}>
            
            <div></div>

            <button
                disabled={orderPlacing || !cart || cart.length === 0}
                className={`bg-custom-blue font-semibold px-8 h-12 rounded-md text-white
                   ${orderPlacing || !cart || cart.length === 0 ? "opacity-60" : "hover:opacity-90"}`}
                onClick={handlePlaceOrder}>
                {orderPlacing ? "Redirecting to PayPal..." : "Pay with PayPal"}
            </button>
        </div>
        
        {errorMessage && <ErrorPage message={errorMessage} />}
    </div>
  );
}

export default Checkout;
