import React from 'react'
import { formatPriceCalculation } from '../../utils/formatPrice'

const OrderSummary = ({ totalPrice, cart }) => {
  return (
    <div className="container mx-auto px-4 mb-8">
     <div className="flex flex-wrap">
      <div className="w-full lg:w-8/12 pr-4">
       <div className="space-y-4">
        <div className='pb-4 border rounded-lg shadow-xs mb-6'>
            <h2 className='text-2xl font-semibold mb-2 p-4'>Order Items</h2>
            <div className='space-y-2'>
                {cart?.map((item) => (
                    <div key={item?.productId} className='flex items-center px-4 py-2'>
                        <img src={`${import.meta.env.VITE_BACK_END_URL}/images/${
                            item?.image
                        }`}
                        alt='Product'
                        className='w-12 h-12 rounded-sm'></img>
                    <div className='text-gray-500 ml-3'>
                        <p className='font-medium text-gray-700'>{item?.productName}</p>
                        <p>
                {item?.quantity} x ${item?.specialPrice || item?.price} = ${
                    formatPriceCalculation(item?.quantity, item?.specialPrice || item?.price)
                }
                        </p>
                    </div>
                    </div>
                ))}
            </div>
        </div>
       </div>
      </div>

      <div className="w-full lg:w-4/12 mt-4 lg:mt-0">
          <div className="border rounded-lg shadow-xs p-4 space-y-4">
            <h2 className="text-2xl font-semibold mb-2">Order Summary</h2>

            <div className="space-y-2">
              <div className="flex justify-between">
                <span>Products</span>
                <span>${formatPriceCalculation(totalPrice, 1)}</span>
              </div>
              <div className="flex justify-between">
                <span>Tax (0%)</span>
                <span>$0.00</span>
              </div>
              <div className="flex justify-between font-semibold text-lg border-t pt-2">
                <span>Total</span>
                <span>${formatPriceCalculation(totalPrice, 1)}</span>
              </div>
            </div>

            <div className="p-3 bg-green-50 rounded-md border border-green-200">
              <p className="text-green-700 text-sm font-medium">✅ Dummy Payment — No real charges</p>
            </div>
        </div>
        </div>
    </div>

    </div>
  )
}

export default OrderSummary