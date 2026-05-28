import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Typography, Tag, Select, Form, Input, InputNumber, Modal, App } from 'antd';
import { SafetyCertificateOutlined, PlusOutlined, FilterOutlined } from '@ant-design/icons';
import { adminGatewayApi, VendorListItem, MasterKeyListItem } from '../api/llmGatewayApi';

const { Text } = Typography;
const { Option } = Select;

export const MasterKeyManage: React.FC = () => {
    const { message } = App.useApp();
    const [keys, setKeys] = useState<MasterKeyListItem[]>([]);
    const [vendors, setVendors] = useState<VendorListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    // --- 统一的复合查询与分页状态 ---
    const [filterVendorId, setFilterVendorId] = useState<number | undefined>(undefined);
    const [filterStatus, setFilterStatus] = useState<number | undefined>(undefined);
    const [pageNum, setPageNum] = useState<number>(1);
    const [pageSize, setPageSize] = useState<number>(10);
    const [total, setTotal] = useState<number>(0);

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [form] = Form.useForm();

    const cardStyle = { backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #d0d7de' };
    const textSecondary = '#656d76';

    // 1. 仅在组件挂载时加载一次服务商下拉列表
    useEffect(() => {
        const loadVendors = async () => {
            try {
                const res = await adminGatewayApi.listVendors();
                if (res.success && res.data) {
                    setVendors(res.data);
                }
            } catch {
                // 静默失败或提示
            }
        };
        loadVendors();
    }, []);

    // 2. 核心拉取列表方法：不再接收参数，直接安全地读取最新的 React State
    const loadKeys = async () => {
        setLoading(true);
        try {
            const res = await adminGatewayApi.listMasterKeys({
                pageNum,
                pageSize,
                vendorId: filterVendorId,
                status: filterStatus
            });

            if (res.success && res.data) {
                setKeys(res.data.list || []);
                setTotal(res.data.total || 0);
            } else {
                message.error(res.message || '获取主密钥池数据失败');
            }
        } catch {
            message.error('获取主密钥池请求异常');
        } finally {
            setLoading(false);
        }
    };

    // 3. 核心修复点：监听所有筛选和分页状态，状态一变自动请求
    useEffect(() => {
        loadKeys();
    }, [pageNum, pageSize, filterVendorId, filterStatus]);

    // --- 4. 事件交互处理：只负责修改状态 ---
    const handleTableChange = (pagination: any) => {
        setPageNum(pagination.current);
        setPageSize(pagination.pageSize);
    };

    const handleVendorFilterChange = (val: number | undefined) => {
        setFilterVendorId(val);
        setPageNum(1); // 切换筛选条件时，重置回第一页
    };

    const handleStatusFilterChange = (val: number | undefined) => {
        setFilterStatus(val);
        setPageNum(1); // 切换筛选条件时，重置回第一页
    };

    const handleCreateKey = async (values: any) => {
        try {
            await adminGatewayApi.createMasterKey({ ...values, status: 1 });
            message.success('真实底层账单主密钥已录入池中');
            setIsModalOpen(false);
            form.resetFields();
            // 手动调用一次刷新当前页数据
            loadKeys();
        } catch {
            message.error('录入主密钥失败，请检查参数或权限');
        }
    };

    const columns = [
        { title: '供应商', dataIndex: 'vendorName', key: 'vendorName', render: (t: string) => <Tag color="cyan" style={{ border: 'none', fontWeight: 500 }}>{t}</Tag> },
        {
            title: '密钥信息',
            dataIndex: 'keyInfo',
            key: 'keyInfo',
            render: (text: string) => <Text code style={{ backgroundColor: '#f6f8fa', color: '#1f2328' }}>{text}</Text>
        },
        {
            title: '负载轮询权重 (Weight)',
            dataIndex: 'weight',
            key: 'weight',
            render: (w: number) => <Text strong style={{ color: '#0969da' }}>{w}</Text>
        },
        {
            title: '通信链路状态',
            dataIndex: 'status',
            key: 'status',
            render: (s: number) => s === 1 ? <Tag color="success" style={{ border: 'none' }}>链路健康</Tag> : <Tag color="error" style={{ border: 'none' }}>异常/封控</Tag>
        },
        {
            title: '最近检测时间',
            dataIndex: 'lastCheckedAt',
            key: 'lastCheckedAt',
            render: (lt: string) => <Text style={{ color: textSecondary }}>{lt || '-'}</Text>
        },
        {
            title: '创建时间',
            dataIndex: 'createdTime',
            key: 'createdTime',
            render: (t: string) => <Text style={{ color: textSecondary }}>{t || '未记录'}</Text>
        },
    ];

    return (
        <div style={{ backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px' }}>
            <Card
                bordered={false}
                style={cardStyle}
                title={<Space><SafetyCertificateOutlined style={{ color: '#1f2328' }} /><span style={{ fontWeight: 600, color: '#1f2328' }}>主密钥池管控 (Master Keys)</span></Space>}
                extra={<Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>增加新厂商密钥</Button>}
            >
                {/* 顶部过滤区 */}
                <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 16 }}>
                    <Space>
                        <FilterOutlined style={{ color: '#656d76' }} />
                        <Text style={{ color: '#656d76' }}>复合筛选：</Text>
                    </Space>
                    <Select
                        placeholder="模型供应商"
                        style={{ width: 220 }}
                        allowClear
                        onChange={handleVendorFilterChange}
                        value={filterVendorId}
                    >
                        {vendors.map(v => <Option key={v.id} value={v.id}>{v.name}</Option>)}
                    </Select>

                    <Select
                        placeholder="通信链路状态"
                        style={{ width: 150 }}
                        allowClear
                        onChange={handleStatusFilterChange}
                        value={filterStatus}
                    >
                        <Option value={1}>链路健康 (1)</Option>
                        <Option value={2}>异常/封控 (2)</Option>
                    </Select>
                </div>

                {/* 表格 */}
                <Table
                    columns={columns}
                    dataSource={keys}
                    rowKey={(record) => `${record.vendorId}-${record.keyInfo}`}
                    loading={loading}
                    onChange={handleTableChange}
                    pagination={{
                        current: pageNum,
                        pageSize: pageSize,
                        total: total,
                        showSizeChanger: true,
                        showTotal: (total) => `共 ${total} 个密钥`,
                        pageSizeOptions: ['10', '20', '50', '100']
                    }}
                />

                {/* 录入主密钥弹窗 */}
                <Modal
                    title={<span style={{ color: '#cf222e' }}>注入高敏感底层厂商密钥</span>}
                    open={isModalOpen}
                    onCancel={() => setIsModalOpen(false)}
                    onOk={() => form.submit()}
                    okText="确认"
                    okButtonProps={{ danger: true }}
                >
                    <div style={{ padding: '12px 16px', backgroundColor: '#ffebe9', border: '1px solid #ff8182', borderRadius: 6, marginBottom: 20 }}>
                        <Text type="danger" style={{ fontSize: 13 }}>
                            安全警告：您正在录入直接关联企业计费账单的核心 API Key。一旦泄露将造成严重财产损失，请确保网络环境安全。
                        </Text>
                    </div>
                    <Form form={form} layout="vertical" onFinish={handleCreateKey}>
                        <Form.Item name="vendorId" label="所属底层服务供应商 (Vendor)" rules={[{ required: true, message: '请选择供应商' }]}>
                            <Select placeholder="选择该密钥对应的计费厂商">
                                {vendors.map(v => <Option key={v.id} value={v.id}>{v.name}</Option>)}
                            </Select>
                        </Form.Item>
                        <Form.Item name="apiKey" label="真实 API Key 明文录入" rules={[{ required: true, message: '请输入密钥明文' }]}>
                            <Input.Password placeholder="例如: sk-xxxxxxxxxxxx" />
                        </Form.Item>
                        <Form.Item name="weight" label="网关请求轮询权重 (Weight)" initialValue={10}>
                            <InputNumber min={1} max={100} style={{ width: '100%' }} placeholder="权重越高，请求分配的概率越大 (1-100)" />
                        </Form.Item>
                    </Form>
                </Modal>
            </Card>
        </div>
    );
};
