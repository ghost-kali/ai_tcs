import React from "react";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";

const Profile = () => {
  const { user } = useSelector((state) => state.auth);

  return (
    <div className="pt-20 pb-10 max-w-4xl mx-auto px-4">
      <h1 className="text-2xl font-bold text-slate-800">My Profile</h1>

      <div className="mt-4 rounded-md border border-slate-200 bg-white p-4">
        <div className="flex flex-col gap-1 text-slate-700">
          <div className="text-sm">
            <span className="font-semibold">Username:</span>{" "}
            <span>{user?.username ?? "—"}</span>
          </div>
          <div className="text-sm">
            <span className="font-semibold">Email:</span>{" "}
            <span>{user?.email ?? user?.principal ?? "—"}</span>
          </div>
          <div className="text-sm">
            <span className="font-semibold">Roles:</span>{" "}
            <span>{Array.isArray(user?.roles) ? user.roles.join(", ") : "—"}</span>
          </div>
        </div>

        <div className="mt-4 flex flex-wrap gap-3">
          <Link
            to="/profile/orders"
            className="px-4 py-2 rounded-xs bg-button-gradient text-white font-semibold"
          >
            View Order History
          </Link>
          <Link
            to="/"
            className="px-4 py-2 rounded-xs border border-slate-300 text-slate-700 font-semibold"
          >
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Profile;

