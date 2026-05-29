import React, {useEffect, useMemo, useState} from 'react';
import {
    App,
    Breadcrumb,
    Button,
    Card,
    Col,
    DatePicker,
    Descriptions,
    Empty,
    Form,
    Input,
    InputNumber,
    Modal,
    Row,
    Select,
    Skeleton,
    Space,
    Statistic,
    Table,
    Tag,
    TreeSelect,
    Typography
} from 'antd';
import {
    ArrowLeftOutlined,
    FilterOutlined,
    PlusOutlined,
    SearchOutlined,
    SafetyCertificateOutlined,
    TeamOutlined
} from '@ant-design/icons';
import {
    adminDeptApi,
    adminRoleApi,
    adminUserApi,
    AdminUserDetail,
    AdminUserQuotaAdjustmentParams,
    AdminUserListItem,
    DepartmentTreeItem,
    RoleListItem
} from '../api/llmGatewayApi';
import {RoleManage} from './RoleManage';

const {Option} = Select;
const {Text, Title} = Typography;
type UserManageView = 'users' | 'roles' | 'detail';
type QuotaAdjustmentDirection = 'increase' | 'recycle';

export const UserManage: React.FC = () => {
    const {message} = App.useApp();
    const [users, setUsers] = useState<AdminUserListItem[]>([]);
    const [departments, setDepartments] = useState<DepartmentTreeItem[]>([]);
    const [roles, setRoles] = useState<RoleListItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);
    const [detailLoading, setDetailLoading] = useState<boolean>(false);
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [activeView, setActiveView] = useState<UserManageView>('users');
    const [selectedUserDetail, setSelectedUserDetail] = useState<AdminUserDetail | null>(null);
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

    const cardStyle = {backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #d0d7de'};
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
            gender: 3,
            status: 1,
            forcePasswordChange: true,
        });
        setIsModalOpen(true);
    };

    const handleViewDetail = async (userId: number) => {
        setDetailLoading(true);
        setActiveView('detail');
        try {
            const res = await adminUserApi.getUserDetail(userId);
            if (res.success && res.data) {
                setSelectedUserDetail(res.data);
            } else {
                setActiveView('users');
                message.error(res.message || '加载用户详情失败');
            }
        } catch {
            setActiveView('users');
            message.error('加载用户详情异常');
        } finally {
            setDetailLoading(false);
        }
    };

    const handleBackToUsers = () => {
        setActiveView('users');
        setSelectedUserDetail(null);
    };

    const handleCreateUser = async (values: any) => {
        try {
            const res = await adminUserApi.createUser({
                name: values.name.trim(),
                username: values.username.trim(),
                email: values.email.trim(),
                mobile: values.mobile?.trim() || undefined,
                gender: values.gender,
                status: values.status,
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
        {title: '用户ID', dataIndex: 'userId', key: 'userId', width: 90},
        {
            title: '姓名/用户名',
            key: 'identity',
            render: (_: any, record: AdminUserListItem) => (
                <Space direction="vertical" size={0}>
                    <Text style={{color: textPrimary, fontWeight: 600}}>{record.name || record.username}</Text>
                    <Text style={{color: textSecondary, fontSize: 12}}>{record.username}</Text>
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
            render: (deptName?: string) => deptName ? <Tag color="blue" style={{border: 'none'}}>{deptName}</Tag> :
                <Text style={{color: textSecondary}}>未绑定</Text>,
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
            render: (gender: number) => <Tag style={{border: 'none'}}>{formatGender(gender)}</Tag>,
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 90,
            render: (status: number) => status === 1
                ? <Tag color="success" style={{border: 'none'}}>正常</Tag>
                : <Tag color="error" style={{border: 'none'}}>已离职</Tag>,
        },
        {
            title: '创建时间',
            dataIndex: 'createdTime',
            key: 'createdTime',
            render: (createdTime?: string) => <Text style={{color: textSecondary}}>{createdTime || '-'}</Text>,
        },
        {
            title: '操作',
            key: 'action',
            width: 110,
            render: (_: any, record: AdminUserListItem) => (
                <Button type="text" size="small" onClick={() => handleViewDetail(record.userId)}
                        style={{color: '#0969da'}}>
                    详情
                </Button>
            ),
        },
    ];

    return (
        <div style={{backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px'}}>
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
                                        style={{
                                            cursor: 'pointer',
                                            color: activeView === 'users' ? '#0969da' : textPrimary
                                        }}
                                        onClick={handleBackToUsers}
                                    >
                                        <TeamOutlined/>
                                        <span style={{fontWeight: 600}}>全局用户管理</span>
                                    </Space>
                                ),
                            },
                            {
                                title: (
                                    <Space
                                        size={6}
                                        style={{
                                            cursor: 'pointer',
                                            color: activeView === 'roles' ? '#0969da' : textPrimary
                                        }}
                                        onClick={() => setActiveView('roles')}
                                    >
                                        <SafetyCertificateOutlined/>
                                        <span style={{fontWeight: 600}}>角色管理</span>
                                    </Space>
                                ),
                            },
                        ]}
                    />
                }
                extra={activeView === 'users' ?
                    <Button type="primary" icon={<PlusOutlined/>} onClick={handleOpenModal}>开通新用户</Button> : null}
            >
                {activeView === 'roles' ? (
                    <RoleManage onRolesChanged={fetchRoles}/>
                ) : activeView === 'detail' ? (
                    <UserDetailView
                        detail={selectedUserDetail}
                        departments={departments}
                        roles={roles}
                        loading={detailLoading}
                        onBack={handleBackToUsers}
                        onReload={handleViewDetail}
                        onUserUpdated={fetchUsers}
                        textPrimary={textPrimary}
                        textSecondary={textSecondary}
                    />
                ) : (
                    <>
                        <div style={{
                            marginBottom: 16,
                            display: 'flex',
                            alignItems: 'center',
                            gap: 12,
                            flexWrap: 'wrap'
                        }}>
                            <Space>
                                <FilterOutlined style={{color: textSecondary}}/>
                                <Text style={{color: textSecondary}}>复合筛选：</Text>
                            </Space>
                            <TreeSelect
                                allowClear
                                showSearch
                                placeholder="所属部门"
                                style={{width: 220}}
                                value={draftDeptId}
                                onChange={setDraftDeptId}
                                treeData={departmentTreeData}
                                treeDefaultExpandAll
                                treeNodeFilterProp="title"
                            />
                            <Input
                                allowClear
                                placeholder="手机号精确查询"
                                style={{width: 180}}
                                value={draftMobile}
                                onChange={(event) => setDraftMobile(event.target.value)}
                                onPressEnter={handleSearch}
                            />
                            <Input
                                allowClear
                                placeholder="邮箱精确查询"
                                style={{width: 240}}
                                value={draftEmail}
                                onChange={(event) => setDraftEmail(event.target.value)}
                                onPressEnter={handleSearch}
                            />
                            <Button type="primary" icon={<SearchOutlined/>} onClick={handleSearch}>
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
                            title={<span style={{color: textPrimary, fontWeight: 600}}>开通新用户</span>}
                            open={isModalOpen}
                            onCancel={() => setIsModalOpen(false)}
                            onOk={() => form.submit()}
                            okText="确认开通"
                            width={760}
                            destroyOnClose
                        >
                            <Form form={form} layout="vertical" onFinish={handleCreateUser} style={{marginTop: 16}}>
                                <Row gutter={16}>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="name" label="姓名" rules={[{required: true, message: '请输入姓名'}]}>
                                            <Input placeholder="例如：Alice Chen"/>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="username" label="用户名"
                                                   rules={[{required: true, message: '请输入用户名'}]}>
                                            <Input placeholder="例如：alice"/>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="email" label="邮箱"
                                                   rules={[{required: true, type: 'email', message: '请输入有效邮箱'}]}>
                                            <Input placeholder="alice@company.com"/>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="mobile" label="手机号">
                                            <Input placeholder="13800138000"/>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="gender" label="性别" rules={[{required: true, message: '请选择性别'}]}>
                                            <Select placeholder="选择性别">
                                                <Option value={1}>男</Option>
                                                <Option value={2}>女</Option>
                                                <Option value={3}>未知</Option>
                                            </Select>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="status" label="用户状态" rules={[{required: true, message: '请选择用户状态'}]}>
                                            <Select placeholder="选择用户状态">
                                                <Option value={1}>正常</Option>
                                                <Option value={0}>已离职</Option>
                                            </Select>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="deptId" label="所属部门"
                                                   rules={[{required: true, message: '请选择所属部门'}]}>
                                            <TreeSelect
                                                showSearch
                                                placeholder="选择部门"
                                                treeData={departmentTreeData}
                                                treeDefaultExpandAll
                                                treeNodeFilterProp="title"
                                            />
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="roleKeys" label="系统角色"
                                                   rules={[{required: true, message: '请选择角色'}]}>
                                            <Select mode="multiple" placeholder="选择角色">
                                                {roles.map((role) => (
                                                    <Option key={role.id} value={role.roleKey}>
                                                        {role.roleName} ({role.roleKey})
                                                    </Option>
                                                ))}
                                            </Select>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="password" label="初始密码"
                                                   rules={[{required: true, message: '请输入初始密码'}]}>
                                            <Input.Password placeholder="Temp@123456"/>
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} md={12}>
                                        <Form.Item name="forcePasswordChange" label="首登改密" initialValue={true}>
                                            <Select>
                                                <Option value={true}>强制首次登录修改密码</Option>
                                                <Option value={false}>不强制修改</Option>
                                            </Select>
                                        </Form.Item>
                                    </Col>
                                </Row>
                            </Form>
                        </Modal>
                    </>
                )}
            </Card>
        </div>
    );
};

function buildDepartmentTreeSelectData(departments: DepartmentTreeItem[]): Array<{
    title: string;
    value: number;
    children?: Array<{ title: string; value: number }>
}> {
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

interface UserDetailViewProps {
    detail: AdminUserDetail | null;
    departments: DepartmentTreeItem[];
    roles: RoleListItem[];
    loading: boolean;
    onBack: () => void;
    onReload: (userId: number) => void;
    onUserUpdated: () => void;
    textPrimary: string;
    textSecondary: string;
}

const UserDetailView: React.FC<UserDetailViewProps> = ({
    detail,
    departments,
    roles,
    loading,
    onBack,
    onReload,
    onUserUpdated,
    textPrimary,
    textSecondary
}) => {
    const {message} = App.useApp();
    const [passwordForm] = Form.useForm();
    const [userForm] = Form.useForm();
    const [quotaForm] = Form.useForm();
    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState<boolean>(false);
    const [isUserModalOpen, setIsUserModalOpen] = useState<boolean>(false);
    const [isQuotaModalOpen, setIsQuotaModalOpen] = useState<boolean>(false);
    const [quotaAdjusting, setQuotaAdjusting] = useState<boolean>(false);
    const quotaAdjustmentDirection = Form.useWatch('direction', quotaForm);
    const departmentTreeData = useMemo(() => buildDepartmentTreeSelectData(departments), [departments]);

    const handleOpenPasswordModal = () => {
        passwordForm.resetFields();
        setIsPasswordModalOpen(true);
    };

    const handleOpenUserModal = () => {
        if (!detail) return;
        userForm.resetFields();
        userForm.setFieldsValue({
            name: detail.user.name,
            username: detail.user.username,
            email: detail.user.email,
            mobile: detail.user.mobile,
            gender: detail.user.gender ?? 3,
            status: detail.user.status ?? 1,
            deptId: detail.user.deptId,
            roleKeys: detail.roles.map((role) => role.roleKey),
        });
        setIsUserModalOpen(true);
    };

    const handleOpenQuotaModal = () => {
        quotaForm.resetFields();
        quotaForm.setFieldsValue({direction: 'increase'});
        setIsQuotaModalOpen(true);
    };

    const handleUpdateUser = async (values: any) => {
        const userId = detail?.user.userId;
        if (!userId) return;

        try {
            const res = await adminUserApi.updateUser(userId, {
                name: values.name.trim(),
                username: values.username.trim(),
                email: values.email.trim(),
                mobile: values.mobile?.trim() || undefined,
                gender: values.gender,
                status: values.status,
                deptId: values.deptId,
                roleKeys: values.roleKeys,
            });
            if (!res.success) {
                throw new Error(res.message || '保存用户信息失败');
            }
            message.success('用户信息已保存');
            setIsUserModalOpen(false);
            userForm.resetFields();
            onReload(userId);
            onUserUpdated();
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : '保存用户信息失败';
            message.error(errorMessage);
        }
    };

    const handleChangePassword = async (values: { password: string }) => {
        const userId = detail?.user.userId;
        if (!userId) return;

        try {
            const res = await adminUserApi.changeUserPassword(userId, {
                password: values.password,
            });
            if (!res.success) {
                throw new Error(res.message || '修改用户密码失败');
            }
            message.success('用户密码已修改，用户下次登录需重新完成改密');
            setIsPasswordModalOpen(false);
            passwordForm.resetFields();
            onReload(userId);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : '修改用户密码失败';
            message.error(errorMessage);
        }
    };

    const handleAdjustUserQuota = async (values: {
        direction: QuotaAdjustmentDirection;
        tokens: number;
        expiresAt?: { format: (template: string) => string };
        remark?: string;
    }) => {
        const userId = detail?.user.userId;
        if (!userId) return;

        const params: AdminUserQuotaAdjustmentParams = {
            adjustTokens: values.direction === 'increase' ? values.tokens : -values.tokens,
            expiresAt: values.direction === 'increase' ? values.expiresAt?.format('YYYY-MM-DD HH:mm:ss') : undefined,
            remark: values.remark?.trim() || undefined,
        };

        setQuotaAdjusting(true);
        try {
            const res = await adminUserApi.adjustUserQuota(userId, params);
            if (!res.success) {
                throw new Error(res.message || '调整用户配额失败');
            }
            message.success('用户配额已调整');
            setIsQuotaModalOpen(false);
            quotaForm.resetFields();
            onReload(userId);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : '调整用户配额失败';
            message.error(errorMessage);
        } finally {
            setQuotaAdjusting(false);
        }
    };

    const modelColumns = [
        {
            title: '模型别名',
            dataIndex: 'modelAlias',
            key: 'modelAlias',
            render: (modelAlias: string) => <Text style={{color: '#0969da', fontWeight: 600}}>{modelAlias}</Text>,
        },
        {
            title: '真实模型',
            dataIndex: 'realModelName',
            key: 'realModelName',
            render: (realModelName: string) => <Text code>{realModelName}</Text>,
        },
        {
            title: '服务商',
            dataIndex: 'vendorName',
            key: 'vendorName',
            width: 110,
        },
        {
            title: '计费类型',
            dataIndex: 'billingType',
            key: 'billingType',
            width: 120,
            render: (billingType: string) => <Tag color={billingType === 'PAID' ? 'gold' : 'green'}
                                                  style={{border: 'none'}}>{billingType}</Tag>,
        },
        {
            title: '授权来源部门',
            dataIndex: 'sourceDeptName',
            key: 'sourceDeptName',
            width: 130,
            render: (sourceDeptName: string, record: AdminUserDetail['modelPermissions'][number]) => (
                <Text>{sourceDeptName || record.sourceDeptId}</Text>
            ),
        },
        {
            title: '作用域',
            dataIndex: 'scope',
            key: 'scope',
            width: 110,
            render: (scope: string) => <Tag color="blue" style={{border: 'none'}}>{scope}</Tag>,
        },
    ];

    return (
        <Space direction="vertical" size={18} style={{width: '100%'}}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12}}>
                <Button icon={<ArrowLeftOutlined/>} onClick={onBack}>
                    返回用户列表
                </Button>
                {detail ? (
                    <Button type="primary" onClick={handleOpenUserModal}>
                        修改用户信息
                    </Button>
                ) : null}
            </div>

            <section style={detailSectionStyle}>
                <SectionTitle title="用户基础信息" textPrimary={textPrimary}/>
                {loading ? (
                    <Skeleton active paragraph={{rows: 8}}/>
                ) : detail ? (
                    <>
                        <Descriptions column={3} size="small" bordered>
                            <Descriptions.Item label="用户ID">{detail.user.userId}</Descriptions.Item>
                            <Descriptions.Item label="姓名">{detail.user.name || '-'}</Descriptions.Item>
                            <Descriptions.Item label="用户名">{detail.user.username || '-'}</Descriptions.Item>
                            <Descriptions.Item label="邮箱">{detail.user.email || '-'}</Descriptions.Item>
                            <Descriptions.Item label="手机号">{detail.user.mobile || '-'}</Descriptions.Item>
                            <Descriptions.Item label="性别">{formatGender(detail.user.gender || 0)}</Descriptions.Item>
                            <Descriptions.Item label="状态">{formatUserStatus(detail.user.status)}</Descriptions.Item>
                            <Descriptions.Item label="首登改密">
                                <Space>
                                    {detail.user.passwordChanged ? '已完成' : '待修改'}
                                    {detail.user.passwordChanged ? (
                                        <Button size="small" type="link" onClick={handleOpenPasswordModal}>
                                            修改密码
                                        </Button>
                                    ) : null}
                                </Space>
                            </Descriptions.Item>
                            <Descriptions.Item
                                label="删除标记">{formatDelFlagStatus(detail.user.delFlag)}</Descriptions.Item>
                            <Descriptions.Item label="头像地址"
                                               span={2}>{detail.user.avatarUrl || '-'}</Descriptions.Item>
                            <Descriptions.Item label="备注">{detail.user.remark || '-'}</Descriptions.Item>
                            <Descriptions.Item label="创建时间">{detail.user.createdTime || '-'}</Descriptions.Item>
                            <Descriptions.Item label="更新时间">{detail.user.updatedTime || '-'}</Descriptions.Item>
                        </Descriptions>

                        <Row gutter={16} style={{marginTop: 16}}>
                            <Col span={12}>
                                <div
                                    style={{border: '1px solid #d0d7de', borderRadius: 8, padding: 16, minHeight: 142}}>
                                    <Text style={{
                                        color: textSecondary,
                                        display: 'block',
                                        marginBottom: 12
                                    }}>关联部门</Text>
                                    {detail.department ? (
                                        <Descriptions column={1} size="small">
                                            <Descriptions.Item
                                                label="部门">{detail.department.deptName}</Descriptions.Item>
                                            <Descriptions.Item
                                                label="部门ID">{detail.department.deptId}</Descriptions.Item>
                                            <Descriptions.Item
                                                label="负责人">{detail.department.leaderName || '-'}</Descriptions.Item>
                                            <Descriptions.Item
                                                label="电话">{detail.department.tel || '-'}</Descriptions.Item>
                                        </Descriptions>
                                    ) : (
                                        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="未绑定部门"/>
                                    )}
                                </div>
                            </Col>
                            <Col span={12}>
                                <div
                                    style={{border: '1px solid #d0d7de', borderRadius: 8, padding: 16, minHeight: 142}}>
                                    <Text style={{
                                        color: textSecondary,
                                        display: 'block',
                                        marginBottom: 12
                                    }}>关联角色</Text>
                                    {detail.roles.length ? (
                                        <Space wrap>
                                            {detail.roles.map((role) => (
                                                <Tag key={role.id} color="purple" style={{border: 'none'}}>
                                                    {role.roleName} ({role.roleKey})
                                                </Tag>
                                            ))}
                                        </Space>
                                    ) : (
                                        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="未绑定角色"/>
                                    )}
                                </div>
                            </Col>
                        </Row>
                    </>
                ) : (
                    <Empty description="暂无用户详情"/>
                )}
            </section>

            <section style={detailSectionStyle}>
                <SectionTitle title="用户可用模型权限列表" textPrimary={textPrimary}/>
                {loading ? (
                    <Skeleton active paragraph={{rows: 4}}/>
                ) : (
                    <Table
                        columns={modelColumns}
                        dataSource={detail?.modelPermissions || []}
                        rowKey={(record) => `${record.modelAlias}-${record.sourceDeptId}-${record.scope}`}
                        pagination={false}
                        locale={{emptyText: '暂无可用模型权限'}}
                    />
                )}
            </section>

            <section style={detailSectionStyle}>
                <SectionTitle title="用户配额详情" textPrimary={textPrimary}/>
                {loading ? (
                    <Skeleton active paragraph={{rows: 3}}/>
                ) : detail?.quota ? (
                    <>
                        <Row gutter={16}>
                            <Col span={6}>
                                <Statistic title="总配额" value={detail.quota.currentQuotaTokens}/>
                            </Col>
                            <Col span={6}>
                                <div
                                    onClick={handleOpenQuotaModal}
                                    style={{cursor: 'pointer', display: 'inline-block'}}
                                    title="点击调整用户配额"
                                >
                                    <Statistic
                                        title="当前可用配额"
                                        value={detail.quota.availableTokens}
                                        valueStyle={{color: '#0969da', textDecoration: 'underline'}}
                                    />
                                </div>
                            </Col>
                            <Col span={6}>
                                <Statistic title="已使用配额" value={detail.quota.usedTokens}/>
                            </Col>
                            <Col span={6}>
                                <Statistic title="已过期配额" value={detail.quota.expiredTokens}/>
                            </Col>
                        </Row>
                        <Descriptions column={3} size="small" bordered style={{marginTop: 16}}>
                            <Descriptions.Item label="累计转入">{detail.quota.transferredInTokens}</Descriptions.Item>
                            <Descriptions.Item label="累计转出">{detail.quota.transferredOutTokens}</Descriptions.Item>
                            <Descriptions.Item
                                label="允许转配">{detail.quota.allowTransferOut ? '是' : '否'}</Descriptions.Item>
                            <Descriptions.Item
                                label="最早过期时间">{detail.quota.earliestExpireAt || '-'}</Descriptions.Item>
                            <Descriptions.Item label="更新时间"
                                               span={2}>{detail.quota.updatedAt || '-'}</Descriptions.Item>
                        </Descriptions>
                    </>
                ) : (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="该用户暂未配置配额账户"/>
                )}
            </section>

            <Modal
                title="修改用户密码"
                open={isPasswordModalOpen}
                onCancel={() => setIsPasswordModalOpen(false)}
                onOk={() => passwordForm.submit()}
                okText="确认修改"
                destroyOnClose
            >
                <Form form={passwordForm} layout="vertical" onFinish={handleChangePassword} style={{marginTop: 16}}>
                    <Form.Item
                        name="password"
                        label="新密码"
                        rules={[{required: true, message: '请输入新密码'}]}
                    >
                        <Input.Password placeholder="Temp@123456"/>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="调整用户配额"
                open={isQuotaModalOpen}
                onCancel={() => setIsQuotaModalOpen(false)}
                onOk={() => quotaForm.submit()}
                okText="确认调整"
                confirmLoading={quotaAdjusting}
                destroyOnClose
            >
                <Form
                    form={quotaForm}
                    layout="vertical"
                    onFinish={handleAdjustUserQuota}
                    style={{marginTop: 16}}
                >
                    <Form.Item name="direction" label="调整方向" rules={[{required: true, message: '请选择调整方向'}]}>
                        <Select>
                            <Option value="increase">新增配额</Option>
                            <Option value="recycle">回收配额</Option>
                        </Select>
                    </Form.Item>
                    <Form.Item
                        name="tokens"
                        label="调整Token数量"
                        rules={[{required: true, message: '请输入调整Token数量'}]}
                    >
                        <InputNumber min={1} precision={0} style={{width: '100%'}} placeholder="例如：100000"/>
                    </Form.Item>
                    <Form.Item
                        name="expiresAt"
                        label="新增额度过期时间"
                        rules={[
                            {
                                required: quotaAdjustmentDirection === 'increase',
                                message: '请输入新增额度过期时间',
                            },
                        ]}
                    >
                        <DatePicker
                            showTime
                            format="YYYY-MM-DD HH:mm:ss"
                            disabled={quotaAdjustmentDirection === 'recycle'}
                            style={{width: '100%'}}
                            placeholder="请选择过期时间"
                        />
                    </Form.Item>
                    <Form.Item
                        name="remark"
                        label="备注"
                        rules={[{max: 255, message: '备注不能超过255个字符'}]}
                    >
                        <Input.TextArea rows={3} maxLength={255} showCount placeholder="请输入调整原因"/>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="修改用户信息"
                open={isUserModalOpen}
                onCancel={() => setIsUserModalOpen(false)}
                onOk={() => userForm.submit()}
                okText="保存"
                width={760}
                destroyOnClose
            >
                <Form form={userForm} layout="vertical" onFinish={handleUpdateUser} style={{marginTop: 16}}>
                    <Row gutter={16}>
                        <Col xs={24} md={12}>
                            <Form.Item name="name" label="姓名" rules={[{required: true, message: '请输入姓名'}]}>
                                <Input placeholder="例如：Alice Chen"/>
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="username" label="用户名" rules={[{required: true, message: '请输入用户名'}]}>
                                <Input placeholder="例如：alice"/>
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="email" label="邮箱"
                                       rules={[{required: true, type: 'email', message: '请输入有效邮箱'}]}>
                                <Input placeholder="alice@company.com"/>
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="mobile" label="手机号">
                                <Input placeholder="13800138000"/>
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="gender" label="性别" rules={[{required: true, message: '请选择性别'}]}>
                                <Select placeholder="选择性别">
                                    <Option value={1}>男</Option>
                                    <Option value={2}>女</Option>
                                    <Option value={3}>未知</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="status" label="用户状态" rules={[{required: true, message: '请选择用户状态'}]}>
                                <Select placeholder="选择用户状态">
                                    <Option value={1}>正常</Option>
                                    <Option value={0}>已离职</Option>
                                </Select>
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="deptId" label="所属部门" rules={[{required: true, message: '请选择所属部门'}]}>
                                <TreeSelect
                                    showSearch
                                    placeholder="选择部门"
                                    treeData={departmentTreeData}
                                    treeDefaultExpandAll
                                    treeNodeFilterProp="title"
                                />
                            </Form.Item>
                        </Col>
                        <Col xs={24} md={12}>
                            <Form.Item name="roleKeys" label="系统角色" rules={[{required: true, message: '请选择角色'}]}>
                                <Select mode="multiple" placeholder="选择角色">
                                    {roles.map((role) => (
                                        <Option key={role.id} value={role.roleKey}>
                                            {role.roleName} ({role.roleKey})
                                        </Option>
                                    ))}
                                </Select>
                            </Form.Item>
                        </Col>
                    </Row>
                </Form>
            </Modal>
        </Space>
    );
};

function SectionTitle({title, textPrimary}: { title: string; textPrimary: string }) {
    return <Title level={5} style={{margin: '0 0 16px', color: textPrimary}}>{title}</Title>;
}

function formatUserStatus(status?: number): React.ReactNode {
    return status === 1
        ? <Tag color="success" style={{border: 'none'}}>正常</Tag>
        : <Tag color="error" style={{border: 'none'}}>已离职</Tag>;
}

function formatDelFlagStatus(delFlag?: boolean): React.ReactNode {
    return !delFlag ? "正常"
        : <Tag color="error" style={{border: 'none'}}>停用</Tag>;
}

const detailSectionStyle: React.CSSProperties = {
    border: '1px solid #d0d7de',
    borderRadius: 8,
    padding: 20,
    backgroundColor: '#fff',
};
