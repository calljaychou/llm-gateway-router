import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Tag, Modal, Form, Input, App, Typography } from 'antd';
import { CloudServerOutlined, PlusOutlined } from '@ant-design/icons';
import { adminGatewayApi, VendorListItem } from '../api/llmGatewayApi';

const { Text } = Typography;

export const VendorManage: React.FC = () => {
    const { message } = App.useApp();
    const [vendors, setVendors] = useState<VendorListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [form] = Form.useForm();

    const cardStyle = { backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #d0d7de' };
    const textPrimary = '#1f2328';
    const textSecondary = '#656d76';

    const fetchVendors = async () => {
        setLoading(true);
        try {
            const res = await adminGatewayApi.listVendors();
            if (res.success && res.data) {
                setVendors(res.data);
            }
        } catch {
            message.error('无法获取基础模型供应商列表');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchVendors();
    }, []);

    const handleCreateSubmit = async (values: any) => {
        try {
            // 默认下发启用状态
            await adminGatewayApi.createVendor({ ...values, status: 1 });
            message.success('新供应商接入成功');
            setIsModalOpen(false);
            form.resetFields();
            fetchVendors();
        } catch {
            message.error('供应商接入失败');
        }
    };

    const columns = [
        {
            title: '提供商名称',
            dataIndex: 'name',
            key: 'name',
            render: (t: string) => <Text style={{ color: textPrimary, fontWeight: 600 }}>{t}</Text>
        },
        {
            title: 'API 网关地址 (Base URL)',
            dataIndex: 'baseUrl',
            key: 'baseUrl',
            render: (t: string) => <Text code style={{ backgroundColor: '#f6f8fa', color: textSecondary }}>{t}</Text>
        },
        {
            title: '接入时间',
            dataIndex: 'createdTime',
            key: 'createdTime',
            render: (t: string) => <Text style={{ color: textSecondary }}>{t || '未记录'}</Text>
        },
        {
            title: '链路状态',
            dataIndex: 'status',
            key: 'status',
            render: (s: number) => s === 1 ? <Tag color="success" style={{ border: 'none' }}>服务中</Tag> : <Tag color="error" style={{ border: 'none' }}>已停用</Tag>
        },
    ];

    return (
        <div style={{ backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px' }}>
            <Card
                bordered={false}
                style={cardStyle}
                title={
                    <Space>
                        <CloudServerOutlined style={{ color: textPrimary }} />
                        <span style={{ fontWeight: 600, color: textPrimary, fontSize: 16 }}>模型供应商管理 (Vendors)</span>
                    </Space>
                }
                extra={
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsModalOpen(true)}>
                        接入新供应商
                    </Button>
                }
            >
                <Table columns={columns} dataSource={vendors} rowKey="vendorId" loading={loading} />

                <Modal
                    title={<span style={{ color: textPrimary, fontWeight: 600 }}>注册大模型服务商</span>}
                    open={isModalOpen}
                    onCancel={() => setIsModalOpen(false)}
                    onOk={() => form.submit()}
                >
                    <Form form={form} layout="vertical" onFinish={handleCreateSubmit} style={{ marginTop: 24 }}>
                        <Form.Item name="name" label="服务商简称 (Vendor Name)" rules={[{ required: true, message: '必须提供机构或产品名称' }]}>
                            <Input placeholder="例如: OpenAI / 智谱AI / 阿里通义千问" />
                        </Form.Item>
                        <Form.Item name="baseUrl" label="API 根路由地址 (Base URL)" rules={[{ required: true, message: '这是底层转发必须依赖的基础地址' }]}>
                            <Input placeholder="例如: https://api.openai.com/v1" />
                        </Form.Item>
                    </Form>
                </Modal>
            </Card>
        </div>
    );
};