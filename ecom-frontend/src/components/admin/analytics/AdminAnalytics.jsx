import React, { useEffect, useMemo } from "react";
import { useDispatch, useSelector } from "react-redux";
import { FaBoxOpen, FaDollarSign, FaShoppingCart } from "react-icons/fa";
import { MdPaid, MdPendingActions } from "react-icons/md";
import { LineChart } from "@mui/x-charts/LineChart";

import {
  fetchProducts,
  getOrdersForDashboard,
} from "../../../store/actions";

import { Loader } from "../../../ui";
import ErrorPage from "../../shared/ErrorPage";

const currency = (value) => {
  const num = Number(value ?? 0);

  const safe = Number.isFinite(num) ? num : 0;

  return safe.toLocaleString(undefined, {
    style: "currency",
    currency: "USD",
  });
};

const toDayKey = (value) => {
  if (!value) return null;

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return date.toISOString().slice(0, 10);
};

const KPI = ({ title, value, Icon, subtle }) => (
  <div className="flex items-center gap-4 rounded-xl border border-slate-200 bg-white p-5 shadow-sm">

    <div
      className={`rounded-lg p-3 ${
        subtle ? "bg-slate-50" : "bg-blue-50"
      }`}
    >
      <Icon className="text-2xl text-slate-800" />
    </div>

    <div className="min-w-0">
      <div className="text-sm text-slate-500">
        {title}
      </div>

      <div className="truncate text-2xl font-bold text-slate-800">
        {value}
      </div>
    </div>

  </div>
);

export default function AdminAnalytics() {

  const dispatch = useDispatch();

  const { isLoading, errorMessage } = useSelector(
    (state) => state.errors
  );

  const { adminOrder } = useSelector(
    (state) => state.order
  );

  const productTotalElements = useSelector(
    (state) => state?.products?.pagination?.totalElements
  );

  useEffect(() => {

    dispatch(
      getOrdersForDashboard(
        "pageNumber=0&pageSize=100",
        true
      )
    );

    dispatch(fetchProducts("page=0&size=1"));

  }, [dispatch]);

  const derived = useMemo(() => {

    const orders = Array.isArray(adminOrder)
      ? adminOrder
      : [];

    const orderStatus = (o) =>
      (o?.orderStatus || "")
        .toString()
        .toUpperCase();

    const isCancelled = (o) =>
      orderStatus(o) === "CANCELLED";

    const isPending = (o) =>
      ["PENDING", "PROCESSING", "ACCEPTED"].includes(
        orderStatus(o)
      );

    const pendingOrders = orders.filter(
      (o) => !isCancelled(o) && isPending(o)
    );

    const nonCancelledOrders = orders.filter(
      (o) => !isCancelled(o)
    );

    const revenue = nonCancelledOrders.reduce(
      (sum, o) =>
        sum + Number(o?.totalAmount ?? 0),
      0
    );

    const aov = nonCancelledOrders.length
      ? revenue / nonCancelledOrders.length
      : 0;

    // TODAY

    const today = new Date();

    today.setHours(0, 0, 0, 0);

    // LAST 7 DAYS START

    const sevenDaysAgo = new Date(today);

    sevenDaysAgo.setDate(
      sevenDaysAgo.getDate() - 6
    );

    // FILTER ORDERS ONLY FROM LAST 7 DAYS

    const recentOrders = nonCancelledOrders.filter(
      (order) => {

        if (!order?.orderDate) {
          return false;
        }

        const orderDate = new Date(
          order.orderDate
        );

        orderDate.setHours(0, 0, 0, 0);

        return (
          orderDate >= sevenDaysAgo &&
          orderDate <= today
        );
      }
    );

    // GROUP REVENUE BY DATE

    const byDay = new Map();

    for (const order of recentOrders) {

      const key = toDayKey(order?.orderDate);

      if (!key) continue;

      byDay.set(
        key,
        (byDay.get(key) ?? 0) +
          Number(order?.totalAmount ?? 0)
      );
    }

    // ALWAYS CREATE EXACTLY 7 DAYS

    const last7Days = [];

    for (let i = 0; i < 7; i++) {

      const currentDate = new Date(sevenDaysAgo);

      currentDate.setDate(
        sevenDaysAgo.getDate() + i
      );

      const key = currentDate
        .toISOString()
        .slice(0, 10);

      last7Days.push({
        day: key,
        revenue: byDay.get(key) ?? 0,
      });
    }

    return {

      totalLoadedOrders: orders.length,

      paidCount: nonCancelledOrders.length,

      pendingCount: pendingOrders.length,

      revenue,

      aov,

      last7Days,
    };

  }, [adminOrder]);

  if (isLoading) {
    return <Loader />;
  }

  if (errorMessage) {
    return (
      <ErrorPage message={errorMessage} />
    );
  }

  return (
    <div className="space-y-6">

      <div>

        <h1 className="text-2xl font-bold text-slate-800">
          Analytics
        </h1>

        <p className="text-sm text-slate-500">
          Basic KPIs based on orders and payments.
        </p>

      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">

        <KPI
          title="Total Products"
          value={productTotalElements ?? 0}
          Icon={FaBoxOpen}
        />

        <KPI
          title="Total Orders"
          value={derived.totalLoadedOrders ?? 0}
          Icon={FaShoppingCart}
        />

        <KPI
          title="Total Revenue"
          value={currency(derived.revenue ?? 0)}
          Icon={FaDollarSign}
        />

        <KPI
          title="Paid Orders (Loaded)"
          value={derived.paidCount}
          Icon={MdPaid}
          subtle
        />

        <KPI
          title="Pending Orders (By Status)"
          value={derived.pendingCount}
          Icon={MdPendingActions}
          subtle
        />

        <KPI
          title="Avg Order Value (Paid)"
          value={currency(derived.aov)}
          Icon={FaDollarSign}
          subtle
        />

      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">

        <div className="mb-4 flex items-baseline justify-between">

          <div>

            <h2 className="text-lg font-semibold text-slate-800">
              Last 7 Days Revenue
            </h2>

            <p className="text-xs text-slate-500 mt-1">
              Missing days are shown as $0
            </p>

          </div>

          <div className="text-xs text-slate-500">
            Based on loaded orders
          </div>

        </div>

        <div className="h-[280px]">

          <LineChart
            xAxis={[
              {
                scaleType: "point",
                data: derived.last7Days.map(
                  (d) => d.day
                ),
              },
            ]}
            series={[
              {
                data: derived.last7Days.map(
                  (d) => Number(d.revenue ?? 0)
                ),
                label: "Revenue",
              },
            ]}
            height={280}
            margin={{
              left: 60,
              right: 20,
              top: 20,
              bottom: 40,
            }}
          />

        </div>

      </div>

    </div>
  );
}