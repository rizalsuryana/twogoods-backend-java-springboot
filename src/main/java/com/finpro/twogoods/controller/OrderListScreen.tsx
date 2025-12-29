import React, { useEffect, useState, useCallback } from "react";
import {
    View,
    Text,
    TouchableOpacity,
    FlatList,
    RefreshControl,
    Modal,
    Image,
    TextInput,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { getMerchantOrders } from "../../services/merchantOrder.service";
import { TransactionResponse } from "../../types/transaction";
import { useFocusEffect, useNavigation } from "@react-navigation/native";

const ORDER_STATUS = [
    "ALL",
    "PENDING",
    "PAID",
    "PACKING",
    "SHIPPED",
    "DELIVERING",
    "COMPLETED",
    "CANCELED",
    "RETURNED",
    "WAITING_CANCEL",
    "WAITING_RETURN",
];

const getStatusBadge = (status: string) => {
    if (status === "COMPLETED")
        return "bg-green-100 text-green-700 border border-green-200";
    if (status === "PENDING")
        return "bg-gray-100 text-gray-700 border border-gray-200";
    if (status === "CANCELED" || status === "RETURNED")
        return "bg-red-100 text-red-700 border border-red-200";
    return "bg-yellow-100 text-yellow-700 border border-yellow-200";
};

const getStatusIcon = (status: string) => {
    switch (status) {
        case "PENDING":
            return "time-outline";
        case "PAID":
            return "card-outline";
        case "PACKING":
            return "cube-outline";
        case "SHIPPED":
            return "boat-outline";
        case "DELIVERING":
            return "bicycle-outline";
        case "COMPLETED":
            return "checkmark-done-outline";
        case "CANCELED":
            return "close-circle-outline";
        case "RETURNED":
            return "return-down-back-outline";
        case "WAITING_CANCEL":
            return "alert-circle-outline";
        case "WAITING_RETURN":
            return "refresh-circle-outline";
        default:
            return "list-outline";
    }
};

const getRequestBackground = (order: TransactionResponse) => {
    if (order.customerCancelRequest && order.status !== "CANCELED")
        return "bg-red-50 border-red-100";
    if (order.customerReturnRequest && order.status !== "RETURNED")
        return "bg-yellow-50 border-yellow-100";
    return "bg-white border-gray-100";
};

export default function OrderListScreen() {
    const navigation: any = useNavigation();

    const [orders, setOrders] = useState<TransactionResponse[]>([]);
    const [filtered, setFiltered] = useState<TransactionResponse[]>([]);
    const [selectedStatus, setSelectedStatus] = useState("ALL");
    const [refreshing, setRefreshing] = useState(false);
    const [dropdownVisible, setDropdownVisible] = useState(false);

    const [page, setPage] = useState(0);
    const [search, setSearch] = useState("");
    const [paging, setPaging] = useState<any>(null);

    const loadOrders = async () => {
        try {
            const backendStatus =
                selectedStatus === "WAITING_CANCEL" ||
                selectedStatus === "WAITING_RETURN" ||
                selectedStatus === "ALL"
                    ? ""
                    : selectedStatus;

            const res = await getMerchantOrders({
                page,
                size: 8,
                search,
                status: backendStatus,
            });

            setOrders(res.data);
            setPaging(res.paging);
            applyFilter(selectedStatus, res.data);
        } catch (err) {
            console.log("Error loading orders:", err);
        }
    };

    // FIXED FILTER
    const applyFilter = (status: string, source = orders) => {
        if (status === "ALL") return setFiltered(source);

        if (status === "WAITING_CANCEL")
            return setFiltered(
                source.filter((o) => o.customerCancelRequest && o.status !== "CANCELED")
            );

        if (status === "WAITING_RETURN")
            return setFiltered(
                source.filter((o) => o.customerReturnRequest && o.status !== "RETURNED")
            );

        setFiltered(source.filter((o) => o.status === status));
    };

    const onRefresh = async () => {
        setRefreshing(true);
        await loadOrders();
        setRefreshing(false);
    };

    useFocusEffect(
        useCallback(() => {
            loadOrders();
        }, [page])
    );

    useEffect(() => {
        loadOrders();
    }, [selectedStatus]);

    useEffect(() => {
        const delay = setTimeout(() => {
            setPage(0);
            loadOrders();
        }, 400);
        return () => clearTimeout(delay);
    }, [search]);

    return (
        <View className="flex-1 bg-primary-bgmain px-4 pt-12">
            <View className="mb-6">
                <Text className="text-3xl font-bold text-primary-main tracking-tight">
                    Orders
                </Text>
                <Text className="text-gray-500 text-xs mt-1">
                    Manage your merchant transactions
                </Text>
            </View>

            {/* Search Bar */}
            <View className="flex-row items-center bg-white border border-gray-100 rounded-2xl px-4 py-3.5 mb-4 shadow-sm shadow-gray-200/50">
                <Ionicons
                    name="search-outline"
                    size={18}
                    color="#94A3B8"
                    style={{ marginRight: 10 }}
                />
                <TextInput
                    placeholder="Search orders..."
                    value={search}
                    onChangeText={setSearch}
                    className="flex-1 text-primary-main font-medium"
                    placeholderTextColor="#94A3B8"
                />
            </View>

            {/* Filter Dropdown */}
            <TouchableOpacity
                activeOpacity={0.7}
                onPress={() => setDropdownVisible(true)}
                className="flex-row items-center justify-between bg-white border border-gray-100 rounded-2xl px-4 py-4 mb-4 shadow-sm shadow-gray-200/50"
            >
                <View className="flex-row items-center">
                    <Ionicons name="filter-outline" size={16} color="#6B7280" />
                    <Text className="text-primary-main font-bold ml-2">
                        {selectedStatus}
                    </Text>
                </View>
                <Ionicons name="chevron-down-outline" size={18} color="#6B7280" />
            </TouchableOpacity>

            {/* Dropdown Modal */}
            <Modal visible={dropdownVisible} transparent animationType="fade">
                <TouchableOpacity
                    className="flex-1 bg-black/40 justify-center px-8"
                    activeOpacity={1}
                    onPress={() => setDropdownVisible(false)}
                >
                    <View className="bg-white rounded-3xl p-5 shadow-xl border border-gray-50">
                        <Text className="text-lg font-bold text-primary-main mb-4 px-2">
                            Filter Status
                        </Text>

                        {ORDER_STATUS.map((status) => {
                            const active = selectedStatus === status;
                            return (
                                <TouchableOpacity
                                    key={status}
                                    onPress={() => {
                                        setSelectedStatus(status);
                                        setDropdownVisible(false);
                                    }}
                                    className={`flex-row items-center px-4 py-3.5 rounded-xl mb-1.5 ${
                                        active ? "bg-primary-main" : "bg-gray-50"
                                    }`}
                                >
                                    <Ionicons
                                        name={getStatusIcon(status)}
                                        size={18}
                                        color={active ? "white" : "#6B7280"}
                                        style={{ marginRight: 12 }}
                                    />
                                    <Text
                                        className={`text-sm font-bold ${
                                            active ? "text-white" : "text-gray-700"
                                        }`}
                                    >
                                        {status}
                                    </Text>
                                </TouchableOpacity>
                            );
                        })}
                    </View>
                </TouchableOpacity>
            </Modal>

            {/* Order List */}
            <FlatList
                data={filtered}
                keyExtractor={(item) => item.id.toString()}
                showsVerticalScrollIndicator={false}
                contentContainerStyle={{ paddingBottom: 20 }}
                refreshControl={
                    <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
                }
                renderItem={({ item }) => (
                    <TouchableOpacity
                        activeOpacity={0.8}
                        onPress={() => navigation.navigate("OrderDetail", { id: item.id })}
                        className={`${getRequestBackground(
                            item
                        )} rounded-[24px] p-5 mb-4 border shadow-sm shadow-gray-200/50`}
                    >
                        <View className="flex-row items-start">
                            <Image
                                source={{
                                    uri: item.items[0]?.productImage
                                        ? item.items[0].productImage
                                        : "https://via.placeholder.com/80",
                                }}
                                className="w-16 h-16 rounded-2xl bg-gray-100 mr-4"
                            />

                            <View className="flex-1">
                                <View className="flex-row justify-between items-start mb-1">
                                    <Text
                                        className="text-base font-bold text-primary-main flex-1 mr-2"
                                        numberOfLines={1}
                                    >
                                        {item.items[0]?.productName}
                                    </Text>

                                    <View
                                        className={`px-2 py-0.5 rounded-md ${getStatusBadge(
                                            item.status
                                        )}`}
                                    >
                                        <Text className="text-[9px] font-bold uppercase">
                                            {item.status}
                                        </Text>
                                    </View>
                                </View>

                                <Text className="text-primary-main font-black text-lg">
                                    Rp{item.totalPrice.toLocaleString("id-ID")}
                                </Text>

                                <View className="flex-row items-center mt-2">
                                    <Ionicons name="time-outline" size={12} color="#94A3B8" />
                                    <Text className="text-gray-400 text-[11px] font-medium ml-1">
                                        {new Date(item.createdAt).toLocaleString("id-ID", {
                                            day: "numeric",
                                            month: "short",
                                            hour: "2-digit",
                                            minute: "2-digit",
                                        })}
                                    </Text>
                                </View>
                            </View>

                            <View className="justify-center h-16 ml-2">
                                <Ionicons name="chevron-forward" size={18} color="#CBD5E1" />
                            </View>
                        </View>

                        {/* CANCEL REQUEST INDICATOR */}
                        {item.customerCancelRequest && item.status !== "CANCELED" && (
                            <View className="flex-row items-center mt-4 px-3 py-2 rounded-xl border bg-red-100/30 border-red-100">
                                <Ionicons name="alert-circle" size={14} color="#b91c1c" />
                                <Text className="text-[10px] font-bold ml-2 text-red-700">
                                    Waiting Cancel Confirmation
                                </Text>
                            </View>
                        )}

                        {/* RETURN REQUEST INDICATOR */}
                        {item.customerReturnRequest && item.status !== "RETURNED" && (
                            <View className="flex-row items-center mt-4 px-3 py-2 rounded-xl border bg-yellow-100/30 border-yellow-100">
                                <Ionicons name="refresh-circle" size={14} color="#b45309" />
                                <Text className="text-[10px] font-bold ml-2 text-yellow-700">
                                    Waiting Return Confirmation
                                </Text>
                            </View>
                        )}
                    </TouchableOpacity>
                )}
            />

            {/* Pagination */}
            {paging && (
                <View className="py-4 bg-primary-bgmain">
                    <View className="flex-row justify-between items-center bg-white p-2 rounded-2xl border border-gray-100 shadow-sm">
                        <TouchableOpacity
                            disabled={!paging.hasPrevious}
                            onPress={() => setPage(page - 1)}
                            className={`w-10 h-10 items-center justify-center rounded-xl ${
                                paging.hasPrevious ? "bg-primary-main" : "bg-gray-100"
                            }`}
                        >
                            <Ionicons name="chevron-back" size={20} color="white" />
                        </TouchableOpacity>

                        <View className="items-center">
                            <Text className="text-primary-main font-bold text-sm">
                                Page {paging.page + 1}{" "}
                                <Text className="text-gray-400 font-medium">
                                    / {paging.totalPages}
                                </Text>
                            </Text>
                        </View>

                        <TouchableOpacity
                            disabled={!paging.hasNext}
                            onPress={() => setPage(page + 1)}
                            className={`w-10 h-10 items-center justify-center rounded-xl ${
                                paging.hasNext ? "bg-primary-main" : "bg-gray-100"
                            }`}
                        >
                            <Ionicons name="chevron-forward" size={20} color="white" />
                        </TouchableOpacity>
                    </View>
                </View>
            )}
        </View>
    );
}
