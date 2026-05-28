import React, { useEffect, useMemo, useState } from 'react';
import { App, Breadcrumb, Button, Card, Form, Input, Modal, Select, Space, Table, Tag, TreeSelect, Typography } from 'antd';
import { FilterOutlined, PlusOutlined, SearchOutlined, SafetyCertificateOutlined, TeamOutlined } from '@ant-design/icons';
import { adminDeptApi, adminRoleApi, adminUserApi, AdminUserListItem, DepartmentTreeItem, RoleListItem } from '../api/llmGatewayApi';
import { RoleManage } from './RoleManage';

const { Option } = Select;
const { Text } = Typography;
type UserManageView = 'users' | 'roles';

export const UserManage: React.FC = () => {
    const { message } = App.useApp();
    const [users, setUsers] = useState<AdminUserListItem[]>([]);
    const [departments, setDepartments] = useState<DepartmentTreeItem[]>([]);
    const [roles, setRoles] = useState<RoleListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [activeView, setActiveView] = useState<UserManageView>('users');
    const [pageNum, setPageNum] = useState<number>(1);
    const [pageSize, setPageSize] = useState<number>(10);
    const [total, setTotal] = useState<number>(0);
    const [draftDeptId, setDraftDeptId] = useState<number | undefined>(undefined);
    const [draftMobile, setDraftMobile] = useState<string>('');
    const [draftEmail, setDraftEmail] = useState<string>('');
    const [filterDeptId, setFilterDeptId] = useState<number | undefined>(undefined);
    const [filterMobile, setFilterMobile] = useState<string>('');
    const [filterEmail, setFilterEmail] = useState<string>('');
    const [form] = Form.useForm();

    const cardStyle = { backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #d0d7de' };
    const textPrimary = '#1f2328';
    const textSecondary = '#656d76';

    const departmentTreeData = useMemo(() => buildDepartmentTreeSelectData(departments), [departments]);
    const defaultRoleKeys = useMemo(() => roles.some((role) => role.roleKey === 'user') ? ['user'] : [], [roles]);

    const fetchDepartments = async () => {
        try {
            const res = await adminDeptApi.getDeptTree();
            if (res.success && res.data) {
                setDepartments(res.data);
            } else {
                message.error(res.message || '加载部门列表失败');
            }
        } catch {
            message.error('加载部门列表异常');
        }
    };

    const fetchRoles = async () => {
        try {
            const res = await adminRoleApi.listRoles();
            if (res.success && res.data) {
                setRoles(res.data);
            } else {
                message.error(res.message || '加载角色列表失败');
            }
        } catch {
            message.error('加载角色列表异常');
        }
    };

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const res = await adminUserApi.listUsers({
                pageNum,
                pageSize,
                deptId: filterDeptId,
                mobile: filterMobile.trim() || undefined,
                email: filterEmail.trim() || undefined,
            });
            if (res.success && res.data) {
                setUsers(res.data.list || []);
                setTotal(res.data.total || 0);
            } else {
                message.error(res.message || '加载用户列表失败');
            }
        } catch {
            message.error('加载用户列表异常');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDepartments();
        fetchRoles();
    }, []);

    useEffect(() => {
        fetchUsers();
    }, [pageNum, pageSize, filterDeptId, filterMobile, filterEmail]);

    const handleTableChange = (pagination: any) => {
        setPageNum(pagination.current);
        setPageSize(pagination.pageSize);
    };

    const handleSearch = () => {
        setFilterDeptId(draftDeptId);
        setFilterMobile(draftMobile);
        setFilterEmail(draftEmail);
        setPageNum(1);
    };

    const handleOpenModal = () => {
        form.resetFields();
        form.setFieldsValue({
            roleKeys: defaultRoleKeys,
            forcePasswordChange: true,
        });
        setIsModalOpen(true);
    };

    const handleCreateUser = async (values: any) => {
        try {
            const res = await adminUserApi.createUser({
                name: values.name.trim(),
                username: values.username.trim(),
                email: values.email.trim(),
                mobile: values.mobile?.trim() || undefined,
                deptId: values.deptId,
                roleKeys: values.roleKeys,
                password: values.password,
                forcePasswordChange: values.forcePasswordChange,
            });
            if (!res.success) {
                throw new Error(res.message || '开通用户失败');
            }
            message.success(`用户开通成功，UID: ${res.data.userId}`);
            setIsModalOpen(false);
            form.resetFields();
            fetchUsers();
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : '开通用户失败';
            message.error(errorMessage);
        }
    };

    const columns = [
        { title: '用户ID', dataIndex: 'userId', key: 'userId', width: 90 },
        {
            title: '姓名/用户名',
            key: 'identity',
            render: (_: any, record: AdminUserListItem) => (
                <Space direction="vertical" size={0}>
                    <Text style={{ color: textPrimary, fontWeight: 600 }}>{record.name || record.username}</Text>
                    <Text style={{ color: textSecondary, fontSize: 12 }}>{record.username}</Text>
                </Space>
            ),
        },
        {
            title: '手机号',
            dataIndex: 'mobile',
            key: 'mobile',
            render: (mobile?: string) => <Text>{mobile || '-'}</Text>,
        },
        {
            title: '所属部门',
            dataIndex: 'deptName',
            key: 'deptName',
            render: (deptName?: string) => deptName ? <Tag color="blue" style={{ border: 'none' }}>{deptName}</Tag> : <Text style={{ color: textSecondary }}>未绑定</Text>,
        },
        {
            title: '邮箱',
            dataIndex: 'email',
            key: 'email',
            render: (email?: string) => <Text>{email || '-'}</Text>,
        },
        {
            title: '性别',
            dataIndex: 'gender',
            key: 'gender',
            width: 80,
            render: (gender: number) => <Tag style={{ border: 'none' }}>{formatGender(gender)}</Tag>,
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 90,
            render: (status: number) => status === 1
                ? <Tag color="success" style={{ border: 'none' }}>正常</Tag>
                : <Tag color="error" style={{ border: 'none' }}>停用</Tag>,
        },
        {
            title: '创建时间',
            dataIndex: 'createdTime',
            key: 'createdTime',
            render: (createdTime?: string) => <Text style={{ color: textSecondary }}>{createdTime || '-'}</Text>,
        },
    ];

    return (
        <div style={{ backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px' }}>
            <Card
                bordered={false}
                style={cardStyle}
                title={
                    <Breadcrumb
                        items={[
                            {
                                title: (
                                    <Space
                                        size={6}
                                        style={{ cursor: 'pointer', color: activeView === 'users' ? textPrimary : '#0969da' }}
                                        onClick={() => setActiveView('users')}
                                    >
                                        <TeamOutlined />
                                        <span style={{ fontWeight: 600 }}>全局用户管理</span>
                                    </Space>
                                ),
                            },
                            {
                                title: (
                                    <Space
                                        size={6}
                                        style={{ cursor: 'pointer', color: activeView === 'roles' ? textPrimary : '#0969da' }}
                                        onClick={() => setActiveView('roles')}
                                    >
                                        <SafetyCertificateOutlined />
                                        <span style={{ fontWeight: 600 }}>角色管理</span>
                                    </Space>
                                ),
                            },
                        ]}
                    />
                }
                extra={activeView === 'users' ? <Button type="primary" icon={<PlusOutlined />} onClick={handleOpenModal}>开通新用户</Button> : null}
            >
                {activeView === 'roles' ? (
                    <RoleManage onRolesChanged={fetchRoles} />
                ) : (
                    <>
                <div style={{ marginBottom: 16, display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
                    <Space>
                        <FilterOutlined style={{ color: textSecondary }} />
                        <Text style={{ color: textSecondary }}>复合筛选：</Text>
                    </Space>
                    <TreeSelect
                        allowClear
                        showSearch
                        placeholder="所属部门"
                        style={{ width: 220 }}
                        value={draftDeptId}
                        onChange={setDraftDeptId}
                        treeData={departmentTreeData}
                        treeDefaultExpandAll
                        treeNodeFilterProp="title"
                    />
                    <Input
                        allowClear
                        placeholder="手机号精确查询"
                        style={{ width: 180 }}
                        value={draftMobile}
                        onChange={(event) => setDraftMobile(event.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Input
                        allowClear
                        placeholder="邮箱精确查询"
                        style={{ width: 240 }}
                        value={draftEmail}
                        onChange={(event) => setDraftEmail(event.target.value)}
                        onPressEnter={handleSearch}
                    />
                    <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                        搜索
                    </Button>
                </div>

                <Table
                    columns={columns}
                    dataSource={users}
                    rowKey="userId"
                    loading={loading}
                    onChange={handleTableChange}
                    pagination={{
                        current: pageNum,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showTotal: (value) => `共 ${value} 个用户`,
                        pageSizeOptions: ['10', '20', '50', '100'],
                    }}
                />

                <Modal
                    title={<span style={{ color: textPrimary, fontWeight: 600 }}>开通新用户</span>}
                    open={isModalOpen}
                    onCancel={() => setIsModalOpen(false)}
                    onOk={() => form.submit()}
                    okText="确认开通"
                    destroyOnClose
                >
                    <Form form={form} layout="vertical" onFinish={handleCreateUser} style={{ marginTop: 16 }}>
                        <Form.Item name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
                            <Input placeholder="例如：Alice Chen" />
                        </Form.Item>
                        <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
                            <Input placeholder="例如：alice" />
                        </Form.Item>
                        <Form.Item name="email" label="邮箱" rules={[{ required: true, type: 'email', message: '请输入有效邮箱' }]}>
                            <Input placeholder="alice@company.com" />
                        </Form.Item>
                        <Form.Item name="mobile" label="手机号">
                            <Input placeholder="13800138000" />
                        </Form.Item>
                        <Form.Item name="deptId" label="所属部门" rules={[{ required: true, message: '请选择所属部门' }]}>
                            <TreeSelect
                                showSearch
                                placeholder="选择部门"
                                treeData={departmentTreeData}
                                treeDefaultExpandAll
                                treeNodeFilterProp="title"
                            />
                        </Form.Item>
                        <Form.Item name="roleKeys" label="系统角色" rules={[{ required: true, message: '请选择角色' }]}>
                            <Select mode="multiple" placeholder="选择角色">
                                {roles.map((role) => (
                                    <Option key={role.id} value={role.roleKey}>
                                        {role.roleName} ({role.roleKey})
                                    </Option>
                                ))}
                            </Select>
                        </Form.Item>
                        <Form.Item name="password" label="初始密码" rules={[{ required: true, message: '请输入初始密码' }]}>
                            <Input.Password placeholder="Temp@123456" />
                        </Form.Item>
                        <Form.Item name="forcePasswordChange" label="首登改密" initialValue={true}>
                            <Select>
                                <Option value={true}>强制首次登录修改密码</Option>
                                <Option value={false}>不强制修改</Option>
                            </Select>
                        </Form.Item>
                    </Form>
                </Modal>
                    </>
                )}
            </Card>
        </div>
    );
};

function buildDepartmentTreeSelectData(departments: DepartmentTreeItem[]): Array<{ title: string; value: number; children?: Array<{ title: string; value: number }> }> {
    return departments.map((department) => ({
        title: department.name,
        value: department.id,
        children: department.children?.length ? buildDepartmentTreeSelectData(department.children) : undefined,
    }));
}

function formatGender(gender: number): string {
    if (gender === 1) return '男';
    if (gender === 2) return '女';
    return '未知';
}
