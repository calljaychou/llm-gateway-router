import React, { useEffect, useState } from 'react';
import { Table, Button, Form, Input, Modal, Tag, Space, App, Card, Typography } from 'antd';
import { KeyOutlined, PlusOutlined, DeleteOutlined, CopyOutlined } from '@ant-design/icons';
import { virtualKeyApi, VirtualKeyListItem } from '../api/llmGatewayApi';

const { Text } = Typography;

export const VirtualKeyManage: React.FC = () => {
    const { message, modal } = App.useApp();
    const [keys, setKeys] = useState<VirtualKeyListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [isCreateOpen, setIsCreateOpen] = useState<boolean>(false);
    const [form] = Form.useForm();

    const fetchKeys = async () => {
        setLoading(true);
        try {
            const res = await virtualKeyApi.listKeys();
            if (res.success) setKeys(res.data.keys);
        } catch {
            message.error('获取密钥列表失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchKeys();
    }, []);

    const handleCreate = async (values: { name: string }) => {
        try {
            const res = await virtualKeyApi.createKey(values);
            if (res.success) {
                setIsCreateOpen(false);
                form.resetFields();
                fetchKeys();

                // 关键凭证安全提示：仅首次展示
                Modal.success({
                    title: '虚拟应用密钥创建成功',
                    width: 550,
                    content: (
                        <div style={{ marginTop: 15 }}>
                            <p style={{ color: '#ff4d4f', fontWeight: 'bold' }}>请立即复制并妥善保存此密钥！出于安全考虑，系统将不再二次明文展示：</p>
                            <Card size="small" bg-color="#f5f5f5" style={{ display: 'flex', justifyContent: 'between', alignItems: 'center' }}>
                                <Text code copyable>{res.data.apiKey}</Text>
                            </Card>
                        </div>
                    ),
                });
            }
        } catch {
            message.error('创建失败，请重试');
        }
    };

    const handleRevoke = (id: number, name: string) => {
        modal.confirm({
            title: '高危操作提示',
            content: `确定要吊销虚拟密钥 [${name}] 吗？吊销后关联的下游大模型业务请求将立即失效且不可逆！`,
            okText: '确认吊销',
            okType: 'danger',
            cancelText: '取消',
            onOk: async () => {
                try {
                    const res = await virtualKeyApi.revokeKey(id);
                    if (res.success) {
                        message.success('密钥已安全吊销');
                        fetchKeys();
                    }
                } catch {
                    message.error('吊销操作失败');
                }
            },
        });
    };

    const columns = [
        { title: '密钥名称', dataIndex: 'name', key: 'name' },
        {
            title: '密钥',
            dataIndex: 'keyPrefix',
            key: 'keyPrefix',
            render: (text: string) => <Text code>{text}</Text>
        },
        {
            title: '可用状态',
            dataIndex: 'status',
            key: 'status',
            render: (status: number) => status === 1 ? <Tag color="success">生效中</Tag> : <Tag color="error">已吊销</Tag>
        },
        { title: '到期时间', dataIndex: 'expiresTime', key: 'expiresTime', render: (t: string) => t || '永久有效' },
        {
            title: '操作',
            key: 'action',
            render: (_: any, record: VirtualKeyListItem) => (
                <Button
                    type="text"
                    danger
                    icon={<DeleteOutlined />}
                    disabled={record.status === 0}
                    onClick={() => handleRevoke(record.keyId, record.name)}
                >
                    吊销
                </Button>
            ),
        },
    ];

    return (
        <Card title={<Space><KeyOutlined />应用虚拟密钥配置中心</Space>} extra={
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setIsCreateOpen(true)}>新建应用密钥</Button>
        }>
            <Table columns={columns} dataSource={keys} rowKey="keyId" loading={loading} pagination={{ pageSize: 10 }} />

            <Modal title="生成新虚拟密钥" open={isCreateOpen} onCancel={() => setIsCreateOpen(false)} onOk={() => form.submit()}>
                <Form form={form} layout="vertical" onFinish={handleCreate}>
                    <Form.Item name="name" label="客户端应用/业务系统名称" rules={[{ required: true, message: '请输入名称以标识密钥用途' }]}>
                        <Input placeholder="例如: 财务报表自动化Agent / 数据分析团队" maxLength={50} />
                    </Form.Item>
                </Form>
            </Modal>
        </Card>
    );
};