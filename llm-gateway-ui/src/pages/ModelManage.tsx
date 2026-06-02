import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Tag, Modal, Form, Input, Select, Switch, App, Typography, InputNumber } from 'antd';
import { AppstoreAddOutlined, PlusOutlined, EditOutlined, DeleteOutlined, ProfileOutlined } from '@ant-design/icons';
import { adminGatewayApi, ModelPriceRuleListItem, ModelVendorListItem, VendorListItem } from '../api/llmGatewayApi';

const { Option } = Select;
const { Text } = Typography;

const chargeItemOptions = [
    { label: '普通输入文本', value: 'INPUT_TEXT' },
    { label: '输入缓存命中', value: 'INPUT_CACHE_HIT' },
    { label: '输入缓存未命中', value: 'INPUT_CACHE_MISS' },
    { label: '输入缓存写入', value: 'INPUT_CACHE_WRITE' },
    { label: '输入音频', value: 'INPUT_AUDIO' },
    { label: '输入图片', value: 'INPUT_IMAGE' },
    { label: '普通输出文本', value: 'OUTPUT_TEXT' },
    { label: '输出推理', value: 'OUTPUT_REASONING' },
    { label: '输出音频', value: 'OUTPUT_AUDIO' },
    { label: '已接受预测输出', value: 'OUTPUT_ACCEPTED_PREDICTION' },
    { label: '已拒绝预测输出', value: 'OUTPUT_REJECTED_PREDICTION' },
];

export const ModelManage: React.FC = () => {
    const { message, modal } = App.useApp();
    const [models, setModels] = useState<ModelVendorListItem[]>([]);
    const [vendors, setVendors] = useState<VendorListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [currentModelId, setCurrentModelId] = useState<number | null>(null); // 区分新建与编辑
    const [form] = Form.useForm();
    const [isRuleModalOpen, setIsRuleModalOpen] = useState<boolean>(false);
    const [ruleLoading, setRuleLoading] = useState<boolean>(false);
    const [priceRules, setPriceRules] = useState<ModelPriceRuleListItem[]>([]);
    const [selectedModel, setSelectedModel] = useState<ModelVendorListItem | null>(null);
    const [isRuleEditModalOpen, setIsRuleEditModalOpen] = useState<boolean>(false);
    const [currentRuleId, setCurrentRuleId] = useState<number | null>(null);
    const [ruleForm] = Form.useForm();

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

    const fetchModelPriceRules = async (record: ModelVendorListItem) => {
        setRuleLoading(true);
        try {
            const res = await adminGatewayApi.listModelPriceRules({ modelId: record.id, vendorId: record.vendorId });
            if (res.success && res.data) setPriceRules(res.data);
            else message.error(res.message || '加载模型计费规则失败');
        } catch (error: any) {
            message.error(error.response?.data?.message || '加载模型计费规则失败');
        } finally {
            setRuleLoading(false);
        }
    };

    const handleOpenRuleModal = async (record: ModelVendorListItem) => {
        setSelectedModel(record);
        setPriceRules([]);
        setIsRuleModalOpen(true);
        await fetchModelPriceRules(record);
    };

    const handleOpenRuleEditModal = (record: ModelPriceRuleListItem) => {
        setCurrentRuleId(record.id);
        ruleForm.setFieldsValue({
            chargeItem: record.chargeItem,
            priceCnyPerMillion: record.priceCnyPerMillion,
            currency: record.currency,
            active: record.active,
        });
        setIsRuleEditModalOpen(true);
    };

    const handleOpenRuleCreateModal = () => {
        if (!selectedModel) return;
        setCurrentRuleId(null);
        ruleForm.resetFields();
        ruleForm.setFieldsValue({
            currency: 'CNY',
            active: true,
        });
        setIsRuleEditModalOpen(true);
    };

    const handleSaveModelPriceRule = async (values: any) => {
        if (!selectedModel) return;
        try {
            const res = currentRuleId
                ? await adminGatewayApi.updateModelPriceRule(currentRuleId, values)
                : await adminGatewayApi.createModelPriceRule({ ...values, modelId: selectedModel.id });
            if (!res.success) {
                message.error(res.message || '保存模型计费规则失败');
                return;
            }
            message.success(currentRuleId ? '模型计费规则已保存' : '模型计费规则已新增');
            setIsRuleEditModalOpen(false);
            setCurrentRuleId(null);
            ruleForm.resetFields();
            await fetchModelPriceRules(selectedModel);
        } catch (error: any) {
            message.error(error.response?.data?.message || '保存模型计费规则失败');
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
            title: '厂商真实模型名',
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
            title: '状态',
            dataIndex: 'active',
            key: 'active',
            render: (a: boolean) => a ? <Tag color="success" style={{ border: 'none' }}>已激活</Tag> : <Tag color="error" style={{ border: 'none' }}>停用</Tag>
        },
        {
            title: '操作',
            key: 'action',
            render: (_: any, record: ModelVendorListItem) => (
                <Space size="small">
                    <Button type="text" size="small" icon={<ProfileOutlined />} onClick={() => handleOpenRuleModal(record)} style={{ color: '#1f6feb' }}>
                        计费规则
                    </Button>
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

    const ruleColumns = [
        {
            title: '计费项',
            dataIndex: 'chargeItem',
            key: 'chargeItem',
            render: (value: string) => <Text code style={{ backgroundColor: '#f6f8fa' }}>{value}</Text>
        },
        {
            title: '单价/百万Token',
            dataIndex: 'priceCnyPerMillion',
            key: 'priceCnyPerMillion',
            render: (value: number) => `¥${Number(value || 0).toFixed(8)}`
        },
        {
            title: '币种',
            dataIndex: 'currency',
            key: 'currency',
            render: (value: string) => <Tag style={{ border: 'none' }}>{value || 'CNY'}</Tag>
        },
        {
            title: '状态',
            dataIndex: 'active',
            key: 'active',
            render: (value: boolean) => value ? <Tag color="success" style={{ border: 'none' }}>启用</Tag> : <Tag color="error" style={{ border: 'none' }}>停用</Tag>
        },
        {
            title: '更新时间',
            dataIndex: 'updatedAt',
            key: 'updatedAt',
            render: (value: string) => value || '-'
        },
        {
            title: '操作',
            key: 'action',
            render: (_: any, record: ModelPriceRuleListItem) => (
                <Button type="text" size="small" icon={<EditOutlined />} onClick={() => handleOpenRuleEditModal(record)} style={{ color: '#0969da' }}>
                    编辑
                </Button>
            )
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

                <Modal
                    title={<span style={{ color: textPrimary, fontWeight: 600 }}>{selectedModel ? `${selectedModel.modelAlias} 计费规则` : '模型计费规则'}</span>}
                    open={isRuleModalOpen}
                    onCancel={() => { setIsRuleModalOpen(false); setSelectedModel(null); setPriceRules([]); }}
                    footer={null}
                    width={820}
                    destroyOnClose
                >
                    <div style={{ marginBottom: 12, display: 'flex', justifyContent: 'flex-end' }}>
                        <Button type="primary" size="small" icon={<PlusOutlined />} onClick={handleOpenRuleCreateModal}>
                            新增规则
                        </Button>
                    </div>
                    <Table
                        columns={ruleColumns}
                        dataSource={priceRules}
                        rowKey="id"
                        loading={ruleLoading}
                        size="small"
                        pagination={false}
                    />
                </Modal>

                <Modal
                    title={<span style={{ color: textPrimary, fontWeight: 600 }}>{currentRuleId ? '编辑模型计费规则' : '新增模型计费规则'}</span>}
                    open={isRuleEditModalOpen}
                    onCancel={() => { setIsRuleEditModalOpen(false); setCurrentRuleId(null); ruleForm.resetFields(); }}
                    onOk={() => ruleForm.submit()}
                    okText="保存"
                    cancelText="取消"
                    width={460}
                    destroyOnClose
                >
                    <Form form={ruleForm} layout="vertical" onFinish={handleSaveModelPriceRule}>
                        <Form.Item name="chargeItem" label="计费项" rules={[{ required: true, message: '请选择计费项' }]}>
                            <Select placeholder="请选择计费项" disabled={!!currentRuleId}>
                                {chargeItemOptions.map(item => (
                                    <Option
                                        key={item.value}
                                        value={item.value}
                                        disabled={!currentRuleId && priceRules.some(rule => rule.chargeItem === item.value)}
                                    >
                                        {item.label} ({item.value})
                                    </Option>
                                ))}
                            </Select>
                        </Form.Item>
                        <Form.Item
                            name="priceCnyPerMillion"
                            label="单价（元/百万Token）"
                            rules={[{ required: true, message: '请输入计费单价' }]}
                        >
                            <InputNumber min={0} precision={8} style={{ width: '100%' }} placeholder="例如: 18.00000000" />
                        </Form.Item>
                        <Form.Item name="currency" label="币种" rules={[{ required: true, message: '请输入币种' }]}>
                            <Input maxLength={16} placeholder="例如: CNY" />
                        </Form.Item>
                        <Form.Item name="active" label="规则状态" valuePropName="checked">
                            <Switch checkedChildren="启用" unCheckedChildren="停用" />
                        </Form.Item>
                    </Form>
                </Modal>
            </Card>
        </div>
    );
};
