import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'

import Navbar from '../components/shared/Navbar'
import PrivateRoute from '../components/PrivateRoute'

import Home from '../components/home/Home'
import Products from '../components/products/Products'
import About from '../components/About'
import Contact from '../components/Contact'
import Cart from '../components/cart/Cart'

import LogIn from '../components/auth/LogIn'
import Register from '../components/auth/Register'

import Checkout from '../components/checkout/Checkout'
import PaymentConfirmation from '../components/checkout/PaymentConfirmation'

import Profile from '../components/profile/Profile'
import OrderHistory from '../components/profile/OrderHistory'

import AdminLayout from '../components/admin/AdminLayout'
import AdminProducts from '../components/admin/products/AdminProducts'
import Category from '../components/admin/categories/Category'
import Orders from '../components/admin/orders/Orders'
import AdminAnalytics from '../components/admin/analytics/AdminAnalytics'

export default function AppRouter() {
  return (
    <>
      <Router>
        <Navbar />
        <Routes>
          <Route path='/' element={<Home />} />
          <Route path='/products' element={<Products />} />
          <Route path='/about' element={<About />} />
          <Route path='/contact' element={<Contact />} />
          <Route path='/cart' element={<Cart />} />

          <Route element={<PrivateRoute />}>
            <Route path='/checkout' element={<Checkout />} />
            <Route path='/order-confirm' element={<PaymentConfirmation />} />
            <Route path='/profile' element={<Profile />} />
            <Route path='/profile/orders' element={<OrderHistory />} />
          </Route>

          <Route element={<PrivateRoute publicPage />}>
            <Route path='/login' element={<LogIn />} />
            <Route path='/register' element={<Register />} />
          </Route>

          <Route element={<PrivateRoute adminOnly />}>
              <Route path='/admin' element={<AdminLayout />}>
              <Route index element={<Navigate to="analytics" replace />} />
              <Route path='analytics' element={<AdminAnalytics />} />
              <Route path='products' element={<AdminProducts />} />
              <Route path='orders' element={<Orders />} />
              <Route path='categories' element={<Category />} />
            </Route>
          </Route>
        </Routes>
      </Router>

      <Toaster position='bottom-center' />
    </>
  )
}
