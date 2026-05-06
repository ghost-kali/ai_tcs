import { Button, FormControl, InputLabel, MenuItem, Select, Tooltip } from "@mui/material";
import { useEffect, useState } from "react";
import { FiArrowDown, FiArrowUp, FiRefreshCw, FiSearch } from "react-icons/fi";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import api from "../../api/api";

const Filter = ({ categories }) => {
    const [searchParams] = useSearchParams();
    const params = new URLSearchParams(searchParams);
    const pathname = useLocation().pathname;
    const navigate = useNavigate();
    
    const [category, setCategory] = useState("all");
    const [sortOrder, setSortOrder] = useState("asc");
    const [searchTerm, setSearchTerm] = useState("");
    const [suggestions, setSuggestions] = useState([]);
    const [showSuggestions, setShowSuggestions] = useState(false);
    const [activeSuggestionIndex, setActiveSuggestionIndex] = useState(-1);

    useEffect(() => {
        const currentCategory = searchParams.get("categoryId") || "all";
        const currentSortOrder = searchParams.get("sortby") || "asc";
        const currentSearchTerm = searchParams.get("keyword") || "";

        setCategory(currentCategory);
        setSortOrder(currentSortOrder);
        setSearchTerm(currentSearchTerm);
    }, [searchParams]);

    useEffect(() => {
        const term = (searchTerm || "").trim();
        if (term.length < 2) {
            setSuggestions([]);
            setActiveSuggestionIndex(-1);
            return;
        }

        const handler = setTimeout(async () => {
            try {
                const res = await api.get(
                    `/products/advanced-search/suggestions?prefix=${encodeURIComponent(term)}`,
                );
                const list = Array.isArray(res.data) ? res.data : [];
                setSuggestions(list.slice(0, 10));
                setActiveSuggestionIndex(-1);
            } catch (e) {
                setSuggestions([]);
                setActiveSuggestionIndex(-1);
            }
        }, 250);

        return () => clearTimeout(handler);
    }, [searchTerm]);

    useEffect(() => { 
        const handler = setTimeout(() => {
            const next = new URLSearchParams(searchParams);
            if (searchTerm && searchTerm.trim()) {
                next.set("keyword", searchTerm.trim());
            } else {
                next.delete("keyword");
            }
            navigate(`${pathname}?${next.toString()}`);
        }, 700); // keep UX: debounce actual search

        return () => {
            clearTimeout(handler);
        };
    }, [searchParams, searchTerm, navigate, pathname]);

    const handleCategoryChange = (event) => {
        const selectedCategory = event.target.value;

        if (selectedCategory === "all") {
            params.delete("categoryId");
        } else {
            params.set("categoryId", selectedCategory);
        }
        navigate(`${pathname}?${params}`);
        setCategory(event.target.value);
    };

    const toggleSortOrder = () => {
        setSortOrder((prevOrder) => {
            const newOrder = (prevOrder === "asc") ?  "desc" : "asc";
            params.set("sortby", newOrder);
            navigate(`${pathname}?${params}`);
            return newOrder;
        })
    };

    const handleClearFilters = () => {
        navigate({ pathname : window.location.pathname });
    };

    const applySuggestion = (value) => {
        const v = (value || "").trim();
        if (!v) return;
        setSearchTerm(v);
        setShowSuggestions(false);
        setSuggestions([]);
        setActiveSuggestionIndex(-1);

        const next = new URLSearchParams(searchParams);
        next.set("keyword", v);
        navigate(`${pathname}?${next.toString()}`);
    };

    const onSearchKeyDown = (e) => {
        if (!showSuggestions || suggestions.length === 0) return;

        if (e.key === "ArrowDown") {
            e.preventDefault();
            setActiveSuggestionIndex((prev) => Math.min(prev + 1, suggestions.length - 1));
        } else if (e.key === "ArrowUp") {
            e.preventDefault();
            setActiveSuggestionIndex((prev) => Math.max(prev - 1, 0));
        } else if (e.key === "Enter") {
            if (activeSuggestionIndex >= 0 && activeSuggestionIndex < suggestions.length) {
                e.preventDefault();
                applySuggestion(suggestions[activeSuggestionIndex]);
            }
        } else if (e.key === "Escape") {
            setShowSuggestions(false);
            setActiveSuggestionIndex(-1);
        }
    };

    return (
        <div className="flex lg:flex-row flex-col-reverse lg:justify-between justify-center items-center gap-4">
            {/* SEARCH BAR */}
            <div className="relative flex items-center 2xl:w-[450px] sm:w-[420px] w-full">
                <input 
                    type="text"
                    placeholder="Search Products"
                    value={searchTerm}
                    onChange={(e) => {
                        setSearchTerm(e.target.value);
                        setShowSuggestions(true);
                    }}
                    onFocus={() => setShowSuggestions(true)}
                    onBlur={() => {
                        // Give click handlers time to run.
                        setTimeout(() => setShowSuggestions(false), 150);
                    }}
                    onKeyDown={onSearchKeyDown}
                    className="border border-gray-400 text-slate-800 rounded-md py-2 pl-10 pr-4 w-full focus:outline-hidden focus:ring-2 focus:ring-[#1976d2]"/>
                <FiSearch className="absolute left-3 text-slate-800 size={20}"/>

                {showSuggestions && suggestions.length > 0 && (
                    <ul className="absolute top-full mt-1 w-full bg-white border border-gray-200 rounded-md shadow-lg z-50 max-h-64 overflow-auto">
                        {suggestions.map((s, idx) => (
                            <li
                                key={`${s}-${idx}`}
                                className={`px-3 py-2 cursor-pointer text-slate-800 hover:bg-slate-100 ${
                                    idx === activeSuggestionIndex ? "bg-slate-100" : ""
                                }`}
                                onMouseDown={(ev) => ev.preventDefault()}
                                onClick={() => applySuggestion(s)}
                                onMouseEnter={() => setActiveSuggestionIndex(idx)}
                            >
                                {s}
                            </li>
                        ))}
                    </ul>
                )}
            </div>

            {/* CATEGORY SELECTION */}
            <div className="flex sm:flex-row flex-col gap-4 items-center">
                <FormControl
                    className="text-slate-800 border-slate-700"
                    variant="outlined"
                    size="small">
                        <InputLabel id="category-select-label">Category</InputLabel>
                        <Select
                            labelId="category-select-label"
                            value={category}
                            onChange={handleCategoryChange}
                            label="Category"
                            className="min-w-[120px] text-slate-800 border-slate-700"
                         >
                            <MenuItem value="all">All</MenuItem>
                            {categories.map((item) => (
                                <MenuItem key={item.categoryId} value={String(item.categoryId)}>
                                    {item.categoryName}
                                </MenuItem>
                            ))}
                         </Select>
                </FormControl>

                {/* SORT BUTTON & CLEAR FILTER */}
                <Tooltip title="Sorted by price: asc">
                    <Button variant="contained" 
                        onClick={toggleSortOrder}
                        color="primary" 
                        className="flex items-center gap-2 h-10">
                        Sort By
                        {sortOrder === "asc" ? (
                            <FiArrowUp size={20} />
                        ) : (
                            <FiArrowDown size={20} />
                        )}
                        
                    </Button>
                </Tooltip>
                <button 
                className="flex items-center gap-2 bg-rose-900 text-white px-3 py-2 rounded-md transition duration-300 ease-in shadow-md focus:outline-hidden"
                onClick={handleClearFilters}
                >
                    <FiRefreshCw className="font-semibold" size={16}/>
                    <span className="font-semibold">Clear Filter</span>
                </button>
            </div>
        </div>
    );
}

export default Filter;
