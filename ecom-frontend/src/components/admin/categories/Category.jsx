import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { DataGrid } from "@mui/x-data-grid";
import { FaFolderOpen, FaThList } from "react-icons/fa";
import toast from "react-hot-toast";

import Modal from "../../shared/Modal";
import AddCategoryForm from "./AddCategoryForm";
import Loader from "../../shared/Loader";
import { DeleteModal } from "../../../components/shared/DeleteModal";
import useCategoryFilter from "../../../hooks/useCategoryFilter";
import ErrorPage from "../../shared/ErrorPage";
import { deleteCategoryDashboardAction } from "../../../store/actions";

const Category = () => {
  const [searchParams] = useSearchParams();
  const pathname = useLocation().pathname;
  const params = new URLSearchParams(searchParams);
  const navigate = useNavigate();

  const dispatch = useDispatch();
  const [openModal, setOpenModal] = useState(false);
  const [openUpdateModal, setOpenUpdateModal] = useState(false);
  const [openDeleteModal, setOpenDeleteModal] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);

  const { categoryLoader, errorMessage } = useSelector((state) => state.errors);
  const { categories } = useSelector((state) => state.products);

  useCategoryFilter();

  // ✅ FILTER ONLY ROOT CATEGORIES (avoid duplicate children)
  const rootCategories = categories?.filter(
    (cat) => cat.parentId === null
  );

  // ✅ BUILD TREE STRUCTURE
  const tableRecords = [];

  rootCategories?.forEach((parent) => {
    tableRecords.push({
      id: parent.categoryId,
      categoryName: parent.categoryName,
      isChild: false,
    });

    parent.children?.forEach((child) => {
      tableRecords.push({
        id: child.categoryId,
        categoryName: "↳ " + child.categoryName,
        isChild: true,
      });
    });
  });

  const handleEdit = (category) => {
    setOpenUpdateModal(true);
    setSelectedCategory(category);
  };

  const handleDelete = (category) => {
    setSelectedCategory(category);
    setOpenDeleteModal(true);
  };

  const onDeleteHandler = () => {
    dispatch(
      deleteCategoryDashboardAction(
        setOpenDeleteModal,
        selectedCategory?.id,
        toast
      )
    );
  };

  const columns = [
    {
      field: "id",
      headerName: "Category ID",
      width: 150,
    },
    {
      field: "categoryName",
      headerName: "Category Name",
      flex: 1,
      renderCell: (params) => (
        <span
          style={{
            paddingLeft: params.row.isChild ? "20px" : "0px",
            fontWeight: params.row.isChild ? "normal" : "bold",
          }}
        >
          {params.value}
        </span>
      ),
    },
    {
      field: "actions",
      headerName: "Action",
      width: 250,
      renderCell: (params) => (
        <div className="flex gap-2">
          <button
            onClick={() => handleEdit(params.row)}
            className="bg-blue-500 text-white px-3 py-1 rounded"
          >
            Edit
          </button>
          <button
            onClick={() => handleDelete(params.row)}
            className="bg-red-500 text-white px-3 py-1 rounded"
          >
            Delete
          </button>
        </div>
      ),
    },
  ];

  const emptyCategories = !categories || categories?.length === 0;

  if (errorMessage) return <ErrorPage message={errorMessage} />;

  return (
    <div>
      <div className="pt-6 pb-10 flex justify-end">
        <button
          onClick={() => setOpenModal(true)}
          className="bg-custom-blue hover:bg-blue-800 text-white font-semibold py-2 px-4 flex items-center gap-2 rounded-md shadow-md"
        >
          <FaThList className="text-xl" />
          Add Category
        </button>
      </div>

      {!emptyCategories && (
        <h1 className="text-slate-800 text-3xl text-center font-bold pb-6 uppercase">
          All Categories
        </h1>
      )}

      {categoryLoader ? (
        <Loader />
      ) : (
        <>
          {emptyCategories ? (
            <div className="flex flex-col items-center justify-center text-gray-600 py-10">
              <FaFolderOpen size={50} className="mb-3" />
              <h2 className="text-2xl font-semibold">
                No Categories Created Yet
              </h2>
            </div>
          ) : (
            <div className="max-w-fit mx-auto">
              <DataGrid
                rows={tableRecords}
                columns={columns}
                paginationMode="client"   // ✅ frontend pagination
                pageSizeOptions={[2, 5, 10]}
                initialState={{
                  pagination: {
                    paginationModel: {
                      pageSize: 2,
                      page: 0,
                    },
                  },
                }}
                disableRowSelectionOnClick
                pagination
              />
            </div>
          )}
        </>
      )}

      <Modal
        open={openUpdateModal || openModal}
        setOpen={openUpdateModal ? setOpenUpdateModal : setOpenModal}
        title={openUpdateModal ? "Update Category" : "Add Category"}
      >
        <AddCategoryForm
          setOpen={openUpdateModal ? setOpenUpdateModal : setOpenModal}
          open={categoryLoader}
          category={selectedCategory}
          update={openUpdateModal}
        />
      </Modal>

      <DeleteModal
        open={openDeleteModal}
        loader={categoryLoader}
        setOpen={setOpenDeleteModal}
        title="Are you want to delete this category"
        onDeleteHandler={onDeleteHandler}
      />
    </div>
  );
};

export default Category;