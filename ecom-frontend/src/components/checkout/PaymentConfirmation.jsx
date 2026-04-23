import React, { useEffect, useState } from 'react'
import { FaCheckCircle } from 'react-icons/fa';
import { useDispatch } from 'react-redux';
import { Link, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { capturePayPalOrder, finalizePayPalOrder } from '../../store/actions';

const PaymentConfirmation = () => {
    const dispatch = useDispatch();
    const location = useLocation();
    const [processing, setProcessing] = useState(false);
    const [orderData, setOrderData] = useState(location.state?.orderData || null);
    const [paymentStatus, setPaymentStatus] = useState("");

    useEffect(() => {
        const params = new URLSearchParams(location.search);
        const payPalToken = params.get("token");
        const pendingOrder = JSON.parse(localStorage.getItem("PENDING_PAYPAL_ORDER"));

        if (!payPalToken || !pendingOrder || orderData) {
            return;
        }

        const finalize = async () => {
            try {
                setProcessing(true);
                const captureData = await capturePayPalOrder(payPalToken);
                setPaymentStatus(captureData?.status || "COMPLETED");

                const placedOrder = await dispatch(
                    finalizePayPalOrder(pendingOrder, toast, null, setProcessing)
                );

                if (placedOrder) {
                    setOrderData(placedOrder);
                }
            } catch (error) {
                console.error(error);
                toast.error("PayPal payment capture failed");
                setProcessing(false);
            }
        };

        finalize();
    }, [dispatch, location.search, orderData]);

  return (
    <div className='min-h-screen flex items-center justify-center'>
        <div className="p-8 rounded-lg shadow-lg text-center max-w-md mx-auto border border-gray-200">
            <div className="text-green-500 mb-4 flex justify-center">    
                <FaCheckCircle size={64} />
            </div>
            <h2 className='text-3xl font-bold text-gray-800 mb-2'>
                {processing ? "Finalizing Payment..." : "Order Placed!"}
            </h2>
            <p className="text-gray-600 mb-4">
                {processing
                    ? "We are confirming your PayPal payment and creating your order."
                    : "Thank you for your purchase! Your order has been confirmed and is being processed."}
            </p>

            {orderData && (
                <div className="bg-gray-50 p-4 rounded-md mb-4 text-left">
                    <p className="text-sm text-gray-600">
                        <strong>Order ID:</strong> #{orderData.orderId}
                    </p>
                    <p className="text-sm text-gray-600">
                        <strong>Payment ID:</strong> {orderData.payment?.paymentId || "Pending"}
                    </p>
                    <p className="text-sm text-gray-600">
                        <strong>Total:</strong> ${Number(orderData.totalAmount).toFixed(2)}
                    </p>
                    <p className="text-sm text-gray-600">
                        <strong>Status:</strong>{' '}
                        <span className="text-green-600 font-semibold">
                            {orderData.orderStatus || paymentStatus || "Completed"}
                        </span>
                    </p>
                </div>
            )}

            <Link to="/products"
                className={`inline-block bg-custom-blue text-white font-semibold px-6 py-2 rounded-md hover:opacity-90 transition ${processing ? "pointer-events-none opacity-50" : ""}`}>
                Continue Shopping
            </Link>
        </div>
    </div>
  )
}

export default PaymentConfirmation
