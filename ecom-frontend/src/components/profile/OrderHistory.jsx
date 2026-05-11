import React, { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Disclosure } from "@headlessui/react";
import { BiChevronDown } from "react-icons/bi";
import { FaShoppingCart } from "react-icons/fa";
import api from "../../api/api";

const safeDateLabel = (value) => {
  if (!value) return "-";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return String(value);
  return d.toLocaleString();
};

const statusClasses = (status) => {
  const s = String(status ?? "").toUpperCase();
  if (s === "DELIVERED") return "bg-green-100 text-green-800";
  if (s === "CANCELLED") return "bg-red-100 text-red-800";
  if (s === "SHIPPED") return "bg-blue-100 text-blue-800";
  if (s === "PROCESSING") return "bg-yellow-100 text-yellow-800";
  if (s === "ACCEPTED" || s === "PENDING") {
    return "bg-slate-100 text-slate-800";
  }
  return "bg-slate-100 text-slate-800";
};

const OrderHistory = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState({
    pageNumber: 0,
    totalPages: 0,
    lastPage: true,
  });

  const backEndUrl =
    import.meta.env.VITE_BACK_END_URL ?? "http://localhost:8080";

  const currentPage = useMemo(() => {
    const p = Number(searchParams.get("page") ?? 1);
    return Number.isFinite(p) && p > 0 ? p : 1;
  }, [searchParams]);

  useEffect(() => {
    let cancelled = false;

    const fetchMyOrders = async () => {
      try {
        setLoading(true);
        const { data } = await api.get("/orders/my", {
          params: { pageNumber: currentPage - 1 },
        });
        if (cancelled) return;
        setOrders(Array.isArray(data?.content) ? data.content : []);
        setPage({
          pageNumber: data?.pageNumber ?? 0,
          totalPages: data?.totalPages ?? 0,
          lastPage: Boolean(data?.lastPage),
        });
      } catch (err) {
        if (cancelled) return;
        setOrders([]);
        setPage({ pageNumber: 0, totalPages: 0, lastPage: true });
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    fetchMyOrders();
    return () => {
      cancelled = true;
    };
  }, [currentPage]);

  const emptyOrders = !loading && (!orders || orders.length === 0);

  const goToPage = (p) => {
    const next = Math.max(1, p);
    setSearchParams({ page: String(next) });
  };

  return (
    <div className="pt-20 pb-10 max-w-5xl mx-auto px-4">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-800">My Orders</h1>
        <div className="text-sm text-slate-600">
          Page {page.pageNumber + 1}
          {page.totalPages ? ` / ${page.totalPages}` : ""}
        </div>
      </div>

      {loading && <div className="mt-6 text-slate-600">Loading orders...</div>}

      {emptyOrders && (
        <div className="flex flex-col items-center justify-center text-gray-600 py-10">
          <FaShoppingCart size={50} className="mb-3" />
          <h2 className="text-2xl font-semibold">No orders yet</h2>
          <p className="text-sm mt-2">
            Your order history will show up here after checkout.
          </p>
        </div>
      )}

      {!loading && orders?.length > 0 && (
        <div className="mt-6 flex flex-col gap-4">
          {orders.map((order) => (
            <div
              key={order.orderId}
              className="rounded-md border border-slate-200 bg-white"
            >
              <div className="p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                <div className="flex flex-col">
                  <div className="text-slate-800 font-bold">
                    Order #{order.orderId}
                  </div>
                  <div className="text-xs text-slate-500">
                    Placed: {safeDateLabel(order.orderDate)}
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span
                    className={`text-xs font-semibold px-2 py-1 rounded-full ${statusClasses(
                      order.orderStatus,
                    )}`}
                  >
                    {order.orderStatus}
                  </span>
                  <div className="text-slate-800 font-semibold">
                    ${Number(order.totalAmount ?? 0).toFixed(2)}
                  </div>
                </div>
              </div>

              <Disclosure>
                {({ open }) => (
                  <>
                    <Disclosure.Button className="w-full px-4 pb-4 -mt-1 flex items-center justify-between text-sm text-slate-700">
                      <span>
                        Items (
                        {Array.isArray(order.orderItems)
                          ? order.orderItems.length
                          : 0}
                        )
                      </span>
                      <BiChevronDown
                        className={`text-2xl transition-transform ${open ? "rotate-180" : ""}`}
                      />
                    </Disclosure.Button>
                    <Disclosure.Panel className="px-4 pb-4">
                      <div className="overflow-x-auto">
                        <table className="min-w-full text-sm">
                          <thead>
                            <tr className="text-left text-slate-500 border-b">
                              <th className="py-2 pr-4 font-semibold">
                                Product
                              </th>
                              <th className="py-2 pr-4 font-semibold">Qty</th>
                              <th className="py-2 pr-4 font-semibold">Price</th>
                            </tr>
                          </thead>
                          <tbody>
                            {(order.orderItems ?? []).map((it) => (
                              <tr
                                key={
                                  it.orderItemId ??
                                  `${order.orderId}-${it.productId}`
                                }
                                className="border-b last:border-b-0"
                              >
                                <td className="py-2 pr-4 text-slate-800">
                                  <div className="flex items-center gap-3">
                                    {it.image ? (
                                      <img
                                        src={`${backEndUrl}/images/${it.image}`}
                                        alt={it.productName ?? "Product"}
                                        className="w-10 h-10 rounded object-cover border"
                                      />
                                    ) : (
                                      <div className="w-10 h-10 rounded bg-slate-100 border" />
                                    )}
                                    <div className="flex flex-col">
                                      <span className="font-semibold">
                                        {it.productName ?? "-"}
                                      </span>
                                      <span className="text-xs text-slate-500">
                                        Product ID: {it.productId ?? "-"}
                                      </span>
                                    </div>
                                  </div>
                                </td>
                                <td className="py-2 pr-4 text-slate-700">
                                  {it.quantity ?? "-"}
                                </td>
                                <td className="py-2 pr-4 text-slate-700">
                                  ${it.price ?? "-"}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </Disclosure.Panel>
                  </>
                )}
              </Disclosure>
            </div>
          ))}
        </div>
      )}

      {!loading && (page.totalPages ?? 0) > 1 && (
        <div className="mt-6 flex items-center justify-center gap-3">
          <button
            className="px-4 py-2 rounded-md border border-slate-300 text-slate-700 disabled:opacity-50"
            onClick={() => goToPage(currentPage - 1)}
            disabled={currentPage <= 1}
          >
            Prev
          </button>
          <button
            className="px-4 py-2 rounded-md border border-slate-300 text-slate-700 disabled:opacity-50"
            onClick={() => goToPage(currentPage + 1)}
            disabled={page.lastPage}
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default OrderHistory;

