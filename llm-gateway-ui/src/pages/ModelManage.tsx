import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Tag, Modal, Form, Input, Select, Switch, App, Typography } from 'antd';
import { AppstoreAddOutlined, PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { adminGatewayApi, ModelVendorListItem, VendorListItem } from '../api/llmGatewayApi';

const { Option } = Select;
const { Text } = Typography;

export const ModelManage: React.FC = () => {
    const { message, modal } = App.useApp();
    const [models, setModels] = useState<ModelVendorListItem[]>([]);
    const [vendors, setVendors] = useState<VendorListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [currentModelId, setCurrentModelId] = useState<number | null>(null); // 区分新建与编辑
    const [form] = Form.useForm();

    const cardStyle = { backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #d0d7de' };
    const textPrimary = '#1f2328';

    const fetchData = async () => {
        setLoading(true);
        try {
            const [modelsRes, vendorsRes] = await Promise.all([
                adminGatewayApi.listModelVendors(),
                adminGatewayApi.listVendors()
            ]);

            if (modelsRes.success && modelsRes.data) setModels(modelsRes.data);
            if (vendorsRes.success && vendorsRes.data) setVendors(vendorsRes.data);

        } catch {
            message.error('加载模型路由数据失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    // --- 处理新建或编辑弹窗开启 ---
    const handleOpenModal = (record?: ModelVendorListItem) => {
        if (record) {
            setCurrentModelId(record.id);
            form.setFieldsValue({
                vendorId: record.vendorId,
                modelAlias: record.modelAlias,
                realModelName: record.realModelName,
                billingType: record.billingType,
                active: record.active,
            });
        } else {
            setCurrentModelId(null);
            form.resetFields();
        }
        setIsModalOpen(true);
    };

    // --- 处理表单提交 (新建 / 更新) ---
    const handleCreateOrUpdateModel = async (values: any) => {
        try {
            if (currentModelId) {
                await adminGatewayApi.updateModel(currentModelId, values);
                message.success('模型路由映射更新成功');
            } else {
                await adminGatewayApi.createModel(values);
                message.success('模型路由映射创建成功');
            }
            setIsModalOpen(false);
            form.resetFields();
            setCurrentModelId(null);
            fetchData(); // 刷新列表
        } catch {
            message.error(currentModelId ? '更新模型路由失败' : '创建模型路由失败');
        }
    };

    // --- 处理删除 ---
    const handleDeleteModel = (id: number, alias: string) => {
        modal.confirm({
            title: '确认删除此模型路由映射？',
            content: `您即将删除网关别名为 [${alias}] 的路由配置。警告：删除后，所有使用此别名发起的业务请求将会立刻熔断失败！此操作不可恢复。`,
            okText: '确认强制删除',
            okType: 'danger',
            cancelText: '取消',
            onOk: async () => {
                try {
                    const res = await adminGatewayApi.deleteModel(id);
                    if (res.success) {
                        message.success(`路由映射 [${alias}] 已安全移除`);
                        fetchData();
                    } else {
                        message.error(res.message || '删除失败');
                    }
                } catch (error: any) {
                    message.error(error.response?.data?.message || '删除请求异常');
                }
            }
        });
    };

    const columns = [
        {
            title: '网关对外别名 (Alias)',
            dataIndex: 'modelAlias',
            key: 'modelAlias',
            render: (t: string) => <Text style={{ color: '#0969da', fontWeight: 600 }}>{t}</Text>
        },
        {
            title: '厂商真实底层模型名',
            dataIndex: 'realModelName',
            key: 'realModelName',
            render: (t: string) => <Text code style={{ backgroundColor: '#f6f8fa' }}>{t}</Text>
        },
        {
            title: '所属服务商 (Vendor)',
            dataIndex: 'vendorName',
            key: 'vendorName',
            // 1. 动态读取 vendors 状态生成筛选菜单
            filters: vendors.map(v => ({ text: v.name, value: v.id })),
            // 2. 当用户选择筛选条件时触发本地过滤逻辑
            onFilter: (value: any, record: ModelVendorListItem) => record.vendorId === value,
            render: (t: string) => <Tag color="cyan" style={{ border: 'none' }}>{t || '未知'}</Tag>
        },
        {
            title: '计费策略',
            dataIndex: 'billingType',
            key: 'billingType',
            render: (t: string) => <Tag color={t === 'PAID' ? 'gold' : 'green'} style={{ border: 'none' }}>{t}</Tag>
        },
        {
            title: '路由管控状态',
            dataIndex: 'active',
            key: 'active',
            render: (a: boolean) => a ? <Tag color="success" style={{ border: 'none' }}>已激活</Tag> : <Tag color="error" style={{ border: 'none' }}>停用</Tag>
        },
        {
            title: '管控操作',
            key: 'action',
            render: (_: any, record: ModelVendorListItem) => (
                <Space size="small">
                    <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleOpenModal(record)} style={{ color: '#0969da' }}>
                        编辑
                    </Button>
                    <Button type="text" danger size="small" icon={<DeleteOutlined />} onClick={() => handleDeleteModel(record.id, record.modelAlias)}>
                        移除
                    </Button>
                </Space>
            ),
        }
    ];

    return (
        <div style={{ backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px' }}>
            <Card
                bordered={false}
                style={cardStyle}
                title={
                    <Space>
                        <AppstoreAddOutlined style={{ color: textPrimary }} />
                        <span style={{ fontWeight: 600, color: textPrimary, fontSize: 16 }}>全局大模型路由配置 (Model Routing)</span>
                    </Space>
                }
                extra={
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenModal()}>
                        新增转发映射规则
                    </Button>
                }
            >
                <Table columns={columns} dataSource={models} rowKey="id" loading={loading} />

                <Modal
                    title={<span style={{ color: textPrimary, fontWeight: 600 }}>{currentModelId ? '编辑模型转发路由' : '注册新模型转发路由'}</span>}
                    open={isModalOpen}
                    onCancel={() => { setIsModalOpen(false); setCurrentModelId(null); }}
                    onOk={() => form.submit()}
                    width={550}
                    destroyOnClose
                >
                    <div style={{ marginBottom: 20, padding: 12, backgroundColor: '#ddf4ff', borderRadius: 8, color: '#0969da', fontSize: 13 }}>
                        配置提示：网关将捕获业务端发送的 [对外别名] 请求，并自动替换为厂商可识别的 [真实模型名] 进行底层转发。
                    </div>
                    <Form form={form} layout="vertical" onFinish={handleCreateOrUpdateModel}>
                        <Form.Item name="vendorId" label="关联的供应商" rules={[{ required: true, message: '必须选择依赖的基础设施' }]}>
                            <Select placeholder="请选择该模型隶属的云服务商">
                                {vendors.map(v => <Option key={v.id} value={v.id}>{v.name}</Option>)}
                            </Select>
                        </Form.Item>
                        <Form.Item name="modelAlias" label="网关对外服务别名 (Alias)" rules={[{ required: true, message: '请输入提供给业务侧的别名' }]}>
                            <Input placeholder="例如: gpt-4-enterprise (业务研发调用的名称)" />
                        </Form.Item>
                        <Form.Item name="realModelName" label="厂商真实模型 (Real Name)" rules={[{ required: true, message: '请输入厂商规定的真实模型名' }]}>
                            <Input placeholder="例如: gpt-4o-2024-05-13 (必须与厂商 API 文档一致)" />
                        </Form.Item>
                        <Form.Item name="billingType" label="资源划扣计费类型" initialValue="PAID">
                            <Select>
                                <Option value="FREE">免费配额池 (FREE)</Option>
                                <Option value="PAID">计费配额池 (PAID)</Option>
                            </Select>
                        </Form.Item>
                        <Form.Item name="active" label="模型初始状态" valuePropName="checked" initialValue={true}>
                            <Switch checkedChildren="立即启用" unCheckedChildren="暂存/封禁" />
                        </Form.Item>
                    </Form>
                </Modal>
            </Card>
        </div>
    );
};