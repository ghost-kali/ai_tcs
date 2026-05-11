import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { DataGrid } from "@mui/x-data-grid";
import { FaFolderOpen, FaThList } from "react-icons/fa";
import toast from "react-hot-toast";

import { Modal } from "../../../ui";
import AddCategoryForm from "./AddCategoryForm";
import { Loader } from "../../../ui";
import { DeleteModal } from "../../../ui";
import ErrorPage from "../../shared/ErrorPage";
import { getCategoriesPageForDashboard } from "../../../store/actions";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { deleteCategoryDashboardAction } from "../../../store/actions";

const Category = () => {
  const dispatch = useDispatch();
  const [searchParams] = useSearchParams();
  const params = new URLSearchParams(searchParams);
  const pathname = useLocation().pathname;
  const navigate = useNavigate();

  const [openModal, setOpenModal] = useState(false);
  const [openUpdateModal, setOpenUpdateModal] = useState(false);
  const [openDeleteModal, setOpenDeleteModal] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);

  const { categoryLoader, errorMessage } = useSelector((state) => state.errors);
  const { categories, pagination } = useSelector((state) => state.products);
  const [currentPage, setCurrentPage] = useState(
    pagination?.pageNumber + 1 || 1,
  );

  React.useEffect(() => {
    const qp = new URLSearchParams();
    const page = searchParams.get("page") ? Number(searchParams.get("page")) : 1;
    setCurrentPage(page);
    qp.set("pageNumber", page - 1);
    qp.set("pageSize", 5);
    dispatch(getCategoriesPageForDashboard(qp.toString()));
  }, [dispatch, searchParams]);

  const tableRecords = (categories ?? []).map((cat) => ({
    id: cat.categoryId,
    categoryName: cat.categoryName,
    parentName: cat.parentName ?? "-",
    active: cat.active,
  }));

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
        toast,
      ),
    );
  };

  const columns = [
    { field: "id", headerName: "Category ID", width: 160 },
    {
      field: "categoryName",
      headerName: "Category Name",
      flex: 1,
    },
  ,
    {
      field: "actions",
      headerName: "Action",
      width: 240,
      sortable: false,
      renderCell: (params) => (
        <div className="flex gap-2">
          <button
            onClick={() => handleEdit(params.row)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded"
          >
            Edit
          </button>
          <button
            onClick={() => handleDelete(params.row)}
            className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded"
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
            <div className="w-full overflow-x-auto">
              <div className="min-w-[700px]">
                <DataGrid
                  rows={tableRecords}
                  columns={columns}
                  paginationMode="server"
                  rowCount={pagination?.totalElements || 0}
                  pageSizeOptions={[pagination?.pageSize || 10]}
                  initialState={{
                    pagination: {
                      paginationModel: {
                        pageSize: pagination?.pageSize || 10,
                        page: currentPage - 1,
                      },
                    },
                  }}
                  onPaginationModelChange={(paginationModel) => {
                    const page = paginationModel.page + 1;
                    setCurrentPage(page);
                    params.set("page", page.toString());
                    navigate(`${pathname}?${params}`);
                  }}
                  disableRowSelectionOnClick
                  disableColumnResize
                  pagination
                />
              </div>
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
