import React, { useEffect, useState } from 'react';
import { App, Button, Card, DatePicker, Input, Select, Space, Table, Tabs, Tag, Typography } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { DatabaseOutlined, SearchOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons';
import dayjs, { type Dayjs } from 'dayjs';
import {
    adminStatisticsUsageApi,
    adminGatewayApi,
    type AdminStatisticsDepartmentUsageItem,
    type AdminStatisticsUsageLogItem,
    type AdminStatisticsUserUsageItem,
    type ModelVendorListItem,
    type VendorListItem
} from '../api/llmGatewayApi';
import { formatAmount } from '../utils/format';

const { Text } = Typography;
const PAGE_SIZE = 10;
const DEFAULT_DATE_RANGE = buildDefaultDateRange();

type StatisticsTabKey = 'logs' | 'departments' | 'users';

export const StatisticsUsage: React.FC = () => {
    const { message } = App.useApp();
    const [activeTab, setActiveTab] = useState<StatisticsTabKey>('logs');

    const [logLoading, setLogLoading] = useState<boolean>(false);
    const [logItems, setLogItems] = useState<AdminStatisticsUsageLogItem[]>([]);
    const [logTotal, setLogTotal] = useState<number>(0);
    const [logPage, setLogPage] = useState<number>(1);
    const [logStartDate, setLogStartDate] = useState<string>(DEFAULT_DATE_RANGE.startDate);
    const [logEndDate, setLogEndDate] = useState<string>(DEFAULT_DATE_RANGE.endDate);
    const [logUserId] = useState<number | undefined>();
    const [logUserKeyword, setLogUserKeyword] = useState<string>('');
    const [logModelId, setLogModelId] = useState<number | undefined>();
    const [logModelKeyword, setLogModelKeyword] = useState<string>('');
    const [logVendorId, setLogVendorId] = useState<number | undefined>();
    const [filterOptionsLoading, setFilterOptionsLoading] = useState<boolean>(false);
    const [modelOptions, setModelOptions] = useState<ModelVendorListItem[]>([]);
    const [vendorOptions, setVendorOptions] = useState<VendorListItem[]>([]);

    const [departmentLoading, setDepartmentLoading] = useState<boolean>(false);
    const [departmentItems, setDepartmentItems] = useState<AdminStatisticsDepartmentUsageItem[]>([]);
    const [departmentTotal, setDepartmentTotal] = useState<number>(0);
    const [departmentPage, setDepartmentPage] = useState<number>(1);
    const [deptName, setDeptName] = useState<string>('');

    const [userLoading, setUserLoading] = useState<boolean>(false);
    const [userItems, setUserItems] = useState<AdminStatisticsUserUsageItem[]>([]);
    const [userTotal, setUserTotal] = useState<number>(0);
    const [userPage, setUserPage] = useState<number>(1);
    const [nickname, setNickname] = useState<string>('');
    const [accountKeyword, setAccountKeyword] = useState<string>('');

    const fetchUsageLogs = async (pageNum = logPage) => {
        setLogLoading(true);
        try {
            const res = await adminStatisticsUsageApi.listUsageLogs({
                pageNum,
                pageSize: PAGE_SIZE,
                startDate: logStartDate,
                endDate: logEndDate,
                userId: logUserId,
                userKeyword: normalizeKeyword(logUserKeyword),
                modelId: logModelId,
                modelKeyword: normalizeKeyword(logModelKeyword),
                vendorId: logVendorId,
            });
            if (res.success) {
                setLogItems(res.data.list || []);
                setLogTotal(res.data.total || 0);
                setLogPage(pageNum);
            }
        } catch {
            message.error('使用日志读取失败');
        } finally {
            setLogLoading(false);
        }
    };

    const fetchFilterOptions = async () => {
        setFilterOptionsLoading(true);
        try {
            const [modelsRes, vendorsRes] = await Promise.all([
                adminGatewayApi.listModelVendors(),
                adminGatewayApi.listVendors(),
            ]);
            if (modelsRes.success && modelsRes.data) setModelOptions(modelsRes.data);
            if (vendorsRes.success && vendorsRes.data) setVendorOptions(vendorsRes.data);
        } catch {
            message.error('模型和供应商筛选项读取失败');
        } finally {
            setFilterOptionsLoading(false);
        }
    };

    const fetchDepartmentUsageStats = async (pageNum = departmentPage) => {
        setDepartmentLoading(true);
        try {
            const res = await adminStatisticsUsageApi.listDepartmentUsageStats({
                pageNum,
                pageSize: PAGE_SIZE,
                deptName: normalizeKeyword(deptName),
            });
            if (res.success) {
                setDepartmentItems(res.data.list || []);
                setDepartmentTotal(res.data.total || 0);
                setDepartmentPage(pageNum);
            }
        } catch {
            message.error('部门使用统计读取失败');
        } finally {
            setDepartmentLoading(false);
        }
    };

    const fetchUserUsageStats = async (pageNum = userPage) => {
        setUserLoading(true);
        try {
            const res = await adminStatisticsUsageApi.listUserUsageStats({
                pageNum,
                pageSize: PAGE_SIZE,
                nickname: normalizeKeyword(nickname),
                accountKeyword: normalizeKeyword(accountKeyword),
            });
            if (res.success) {
                setUserItems(res.data.list || []);
                setUserTotal(res.data.total || 0);
                setUserPage(pageNum);
            }
        } catch {
            message.error('用户使用统计读取失败');
        } finally {
            setUserLoading(false);
        }
    };

    useEffect(() => {
        fetchUsageLogs(1);
        fetchFilterOptions();
    }, []);

    useEffect(() => {
        if (activeTab === 'departments' && departmentItems.length === 0 && departmentTotal === 0) fetchDepartmentUsageStats(1);
        if (activeTab === 'users' && userItems.length === 0 && userTotal === 0) fetchUserUsageStats(1);
    }, [activeTab]);

    const handleLogDateChange = (_dates: unknown, dateStrings: [string, string]) => {
        setLogStartDate(dateStrings[0] || DEFAULT_DATE_RANGE.startDate);
        setLogEndDate(dateStrings[1] || DEFAULT_DATE_RANGE.endDate);
    };

    const usageLogColumns: ColumnsType<AdminStatisticsUsageLogItem> = [
        {
            title: '请求ID',
            dataIndex: 'requestId',
            key: 'requestId',
            width: 320,
            ellipsis: true,
            render: (requestId: string) => <Text code title={requestId}>{requestId || '-'}</Text>,
        },
        {
            title: '用户',
            key: 'user',
            width: 100,
            render: (_: unknown, record) => (
                <Space direction="vertical" size={0}>
                    <Text strong>{record.userName || '-'}</Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>{record.userAccount || `ID ${record.userId}`}</Text>
                </Space>
            ),
        },
        { title: '部门', dataIndex: 'deptName', key: 'deptName', width: 140, ellipsis: true },
        {
            title: '供应商',
            dataIndex: 'vendorName',
            key: 'vendorName',
            width: 120,
            render: (vendorName: string) => vendorName ? <Tag color="blue">{vendorName}</Tag> : '-',
        },
        { title: '模型', dataIndex: 'modelName', key: 'modelName', width: 170, ellipsis: true },
        { title: '输入Token', dataIndex: 'inputTokens', key: 'inputTokens', width: 96, align: 'right', render: formatNumber },
        { title: '输出Token', dataIndex: 'outputTokens', key: 'outputTokens', width: 96, align: 'right', render: formatNumber },
        { title: '缓存Token', dataIndex: 'cachedTokens', key: 'cachedTokens', width: 96, align: 'right', render: formatNumber },
        { title: '总Token', dataIndex: 'totalTokens', key: 'totalTokens', width: 100, align: 'right', render: formatNumber },
        { title: '使用时间', dataIndex: 'usedAt', key: 'usedAt', width: 170, render: (usedAt?: string) => usedAt || '-' },
        { title: '耗时', dataIndex: 'latencyMs', key: 'latencyMs', width: 90, align: 'right', render: (value: number) => `${value || 0} ms` },
    ];

    const departmentColumns: ColumnsType<AdminStatisticsDepartmentUsageItem> = [
        { title: '部门ID', dataIndex: 'deptId', key: 'deptId', width: 120 },
        { title: '部门名称', dataIndex: 'deptName', key: 'deptName', ellipsis: true },
        { title: '使用次数', dataIndex: 'usageCount', key: 'usageCount', width: 140, align: 'right', render: formatNumber },
        { title: '使用Token数', dataIndex: 'totalTokens', key: 'totalTokens', width: 160, align: 'right', render: formatNumber },
    ];

    const userColumns: ColumnsType<AdminStatisticsUserUsageItem> = [
        {
            title: '用户',
            key: 'user',
            width: 190,
            render: (_: unknown, record) => (
                <Space direction="vertical" size={0}>
                    <Text strong>{record.userName || '-'}</Text>
                    <Text type="secondary" style={{ fontSize: 12 }}>ID {record.userId}</Text>
                </Space>
            ),
        },
        { title: '用户名', dataIndex: 'username', key: 'username', width: 140, render: renderNullable },
        { title: '手机号', dataIndex: 'mobile', key: 'mobile', width: 140, render: renderNullable },
        { title: '邮箱', dataIndex: 'email', key: 'email', width: 210, ellipsis: true, render: renderNullable },
        { title: '部门', dataIndex: 'deptName', key: 'deptName', width: 150, ellipsis: true },
        { title: '余额', dataIndex: 'balance', key: 'balance', width: 130, align: 'right', render: (value: number) => formatAmount(value) },
        { title: '使用Token数', dataIndex: 'totalTokens', key: 'totalTokens', width: 150, align: 'right', render: formatNumber },
        { title: '使用次数', dataIndex: 'usageCount', key: 'usageCount', width: 120, align: 'right', render: formatNumber },
    ];

    return (
        <Card title="数据统计" style={{ width: '100%' }}>
            <Tabs
                activeKey={activeTab}
                onChange={(key) => setActiveTab(key as StatisticsTabKey)}
                items={[
                    {
                        key: 'logs',
                        label: <span><DatabaseOutlined />使用日志</span>,
                        children: (
                            <Space direction="vertical" size={16} style={{ width: '100%' }}>
                                <Space wrap>
                                    <DatePicker.RangePicker
                                        value={buildDatePickerValue(logStartDate, logEndDate)}
                                        onChange={handleLogDateChange}
                                    />
                                    <Input
                                        allowClear
                                        placeholder="用户关键字"
                                        style={{ width: 180 }}
                                        value={logUserKeyword}
                                        onChange={(event) => setLogUserKeyword(event.target.value)}
                                        onPressEnter={() => fetchUsageLogs(1)}
                                    />
                                    <Select
                                        allowClear
                                        showSearch
                                        placeholder="选择模型"
                                        style={{ width: 220 }}
                                        value={logModelId}
                                        loading={filterOptionsLoading}
                                        optionFilterProp="label"
                                        onChange={setLogModelId}
                                        options={modelOptions.map(model => ({
                                            value: model.id,
                                            label: `${model.modelAlias} / ${model.realModelName}`,
                                        }))}
                                    />
                                    <Input
                                        allowClear
                                        placeholder="模型关键字"
                                        style={{ width: 180 }}
                                        value={logModelKeyword}
                                        onChange={(event) => setLogModelKeyword(event.target.value)}
                                        onPressEnter={() => fetchUsageLogs(1)}
                                    />
                                    <Select
                                        allowClear
                                        showSearch
                                        placeholder="选择供应商"
                                        style={{ width: 180 }}
                                        value={logVendorId}
                                        loading={filterOptionsLoading}
                                        optionFilterProp="label"
                                        onChange={setLogVendorId}
                                        options={vendorOptions.map(vendor => ({
                                            value: vendor.id,
                                            label: vendor.name,
                                        }))}
                                    />
                                    <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchUsageLogs(1)}>搜索</Button>
                                </Space>
                                <Table<AdminStatisticsUsageLogItem>
                                    columns={usageLogColumns}
                                    dataSource={logItems}
                                    rowKey="usageLogId"
                                    loading={logLoading}
                                    scroll={{ x: 1480 }}
                                    pagination={buildPagination(logPage, logTotal, fetchUsageLogs, '条日志')}
                                />
                            </Space>
                        ),
                    },
                    {
                        key: 'departments',
                        label: <span><TeamOutlined />部门统计</span>,
                        children: (
                            <Space direction="vertical" size={16} style={{ width: '100%' }}>
                                <Space wrap>
                                    <Input
                                        allowClear
                                        placeholder="部门名称模糊筛选"
                                        style={{ width: 240 }}
                                        value={deptName}
                                        onChange={(event) => setDeptName(event.target.value)}
                                        onPressEnter={() => fetchDepartmentUsageStats(1)}
                                    />
                                    <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchDepartmentUsageStats(1)}>搜索</Button>
                                </Space>
                                <Table<AdminStatisticsDepartmentUsageItem>
                                    columns={departmentColumns}
                                    dataSource={departmentItems}
                                    rowKey="deptId"
                                    loading={departmentLoading}
                                    pagination={buildPagination(departmentPage, departmentTotal, fetchDepartmentUsageStats, '个部门')}
                                />
                            </Space>
                        ),
                    },
                    {
                        key: 'users',
                        label: <span><UserOutlined />用户统计</span>,
                        children: (
                            <Space direction="vertical" size={16} style={{ width: '100%' }}>
                                <Space wrap>
                                    <Input
                                        allowClear
                                        placeholder="用户昵称模糊筛选"
                                        style={{ width: 220 }}
                                        value={nickname}
                                        onChange={(event) => setNickname(event.target.value)}
                                        onPressEnter={() => fetchUserUsageStats(1)}
                                    />
                                    <Input
                                        allowClear
                                        placeholder="手机号 / username / 邮箱"
                                        style={{ width: 260 }}
                                        value={accountKeyword}
                                        onChange={(event) => setAccountKeyword(event.target.value)}
                                        onPressEnter={() => fetchUserUsageStats(1)}
                                    />
                                    <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchUserUsageStats(1)}>搜索</Button>
                                </Space>
                                <Table<AdminStatisticsUserUsageItem>
                                    columns={userColumns}
                                    dataSource={userItems}
                                    rowKey="userId"
                                    loading={userLoading}
                                    scroll={{ x: 1180 }}
                                    pagination={buildPagination(userPage, userTotal, fetchUserUsageStats, '个用户')}
                                />
                            </Space>
                        ),
                    },
                ]}
            />
        </Card>
    );
};

function normalizeKeyword(value: string): string | undefined {
    const normalized = value.trim();
    return normalized || undefined;
}

function formatNumber(value?: number): string {
    return Number(value || 0).toLocaleString('zh-CN');
}

function renderNullable(value?: string): string {
    return value || '-';
}

function buildPagination(
    current: number,
    total: number,
    onPageChange: (page: number) => void,
    unit: string,
): TablePaginationConfig {
    return {
        current,
        pageSize: PAGE_SIZE,
        total,
        showSizeChanger: false,
        showTotal: (value) => `共 ${value} ${unit}`,
        onChange: onPageChange,
    };
}

function buildDefaultDateRange() {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setDate(endDate.getDate() - 29);
    return {
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
    };
}

function formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function buildDatePickerValue(startDate: string, endDate: string): [Dayjs, Dayjs] | undefined {
    if (!startDate || !endDate) return undefined;
    return [dayjs(startDate), dayjs(endDate)];
}
