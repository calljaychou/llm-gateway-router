import React, { useEffect, useState } from 'react';
import { App, Button, Form, Input, InputNumber, Modal, Space, Table, Tag, Typography } from 'antd';
import { DeleteOutlined, PlusOutlined, SafetyCertificateOutlined } from '@ant-design/icons';
import { adminRoleApi, RoleListItem } from '../api/llmGatewayApi';

const { Text } = Typography;

interface RoleManageProps {
    onRolesChanged?: () => void;
}

export const RoleManage: React.FC<RoleManageProps> = ({ onRolesChanged }) => {
    const { message, modal } = App.useApp();
    const [roles, setRoles] = useState<RoleListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [form] = Form.useForm();

    const textPrimary = '#1f2328';
    const textSecondary = '#656d76';

    const fetchRoles = async () => {
        setLoading(true);
        try {
            const res = await adminRoleApi.listRoles();
            if (res.success && res.data) {
                setRoles(res.data);
            } else {
                message.error(res.message || '加载角色列表失败');
            }
        } catch {
            message.error('加载角色列表异常');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRoles();
    }, []);

    const handleOpenModal = () => {
        form.resetFields();
        form.setFieldsValue({ roleSort: 10 });
        setIsModalOpen(true);
    };

    const handleCreateRole = async (values: any) => {
        try {
            const res = await adminRoleApi.createRole({
                roleName: values.roleName.trim(),
                roleKey: values.roleKey.trim(),
                roleSort: values.roleSort,
            });
            if (!res.success) {
                throw new Error(res.message || '新增角色失败');
            }
            message.success(`角色 [${res.data.roleKey}] 创建成功`);
            setIsModalOpen(false);
            form.resetFields();
            fetchRoles();
            onRolesChanged?.();
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : '新增角色失败';
            message.error(errorMessage);
        }
    };

    const handleDeleteRole = (record: RoleListItem) => {
        modal.confirm({
            title: '删除角色确认',
            content: `确定删除角色 [${record.roleKey}] 吗？删除后会同步清理该角色与用户的关联关系。`,
            okText: '确认删除',
            okType: 'danger',
            cancelText: '取消',
            onOk: async () => {
                try {
                    const res = await adminRoleApi.deleteRole(record.id);
                    if (!res.success) {
                        throw new Error(res.message || '删除角色失败');
                    }
                    message.success(`角色已删除，清理关联 ${res.data.removedUserRoleRelCount} 条`);
                    fetchRoles();
                    onRolesChanged?.();
                } catch (error) {
                    const errorMessage = error instanceof Error ? error.message : '删除角色失败';
                    message.error(errorMessage);
                }
            },
        });
    };

    const columns = [
        {
            title: '角色ID',
            dataIndex: 'id',
            key: 'id',
            width: 90,
        },
        {
            title: '角色名称',
            dataIndex: 'roleName',
            key: 'roleName',
            render: (roleName: string) => <Text style={{ color: textPrimary, fontWeight: 600 }}>{roleName}</Text>,
        },
        {
            title: '角色标识',
            dataIndex: 'roleKey',
            key: 'roleKey',
            render: (roleKey: string) => <Tag color="blue" style={{ border: 'none' }}>{roleKey}</Tag>,
        },
        {
            title: '排序',
            dataIndex: 'roleSort',
            key: 'roleSort',
            width: 100,
            render: (roleSort: number) => <Text style={{ color: textSecondary }}>{roleSort}</Text>,
        },
        {
            title: '创建人',
            dataIndex: 'createdBy',
            key: 'createdBy',
            render: (createdBy?: string) => <Text>{createdBy || '-'}</Text>,
        },
        {
            title: '创建时间',
            dataIndex: 'createdTime',
            key: 'createdTime',
            render: (createdTime?: string) => <Text style={{ color: textSecondary }}>{createdTime || '-'}</Text>,
        },
        {
            title: '操作',
            key: 'action',
            width: 120,
            render: (_: any, record: RoleListItem) => (
                <Button
                    type="text"
                    danger
                    size="small"
                    icon={<DeleteOutlined />}
                    onClick={() => handleDeleteRole(record)}
                    style={{ padding: '0 8px' }}
                >
                    删除
                </Button>
            ),
        },
    ];

    return (
        <>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12 }}>
                <Space>
                    <SafetyCertificateOutlined style={{ color: textPrimary }} />
                    <Text style={{ color: textPrimary, fontWeight: 600 }}>角色管理</Text>
                </Space>
                <Button type="primary" icon={<PlusOutlined />} onClick={handleOpenModal}>
                    新增角色
                </Button>
            </div>

            <Table
                columns={columns}
                dataSource={roles}
                rowKey="id"
                loading={loading}
                pagination={false}
            />

            <Modal
                title={<span style={{ color: textPrimary, fontWeight: 600 }}>新增角色</span>}
                open={isModalOpen}
                onCancel={() => setIsModalOpen(false)}
                onOk={() => form.submit()}
                okText="确认新增"
                destroyOnClose
            >
                <Form form={form} layout="vertical" onFinish={handleCreateRole} style={{ marginTop: 16 }}>
                    <Form.Item name="roleName" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
                        <Input placeholder="例如：普通用户" />
                    </Form.Item>
                    <Form.Item name="roleKey" label="角色标识" rules={[{ required: true, message: '请输入角色标识' }]}>
                        <Input placeholder="例如：user" />
                    </Form.Item>
                    <Form.Item name="roleSort" label="排序号">
                        <InputNumber min={0} max={9999} style={{ width: '100%' }} />
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
};
