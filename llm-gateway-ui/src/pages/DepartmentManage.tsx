import React, { useEffect, useState } from 'react';
import { Card, Table, Button, Space, Modal, Form, Input, InputNumber, Drawer, Select, Tag, App, Typography } from 'antd';
import { ApartmentOutlined, PlusOutlined, SafetyCertificateOutlined, DeleteOutlined } from '@ant-design/icons';
import { adminDeptApi, DepartmentTreeItem } from '../api/llmGatewayApi';
import {Simulate} from "react-dom/test-utils";
import error = Simulate.error;

const { Option } = Select;
const { Text } = Typography;

export const DepartmentManage: React.FC = () => {
    const { message, modal } = App.useApp();
    const [treeData, setTreeData] = useState<DepartmentTreeItem[]>([]);
    const [loading, setLoading] = useState<boolean>(false);

    // 部门表单状态
    const [isDeptModalOpen, setIsDeptModalOpen] = useState<boolean>(false);
    const [deptForm] = Form.useForm();
    const [currentParentId, setCurrentParentId] = useState<number>(0); // 0 表示创建一级部门

    // 权限抽屉状态
    const [isPermissionDrawerOpen, setIsPermissionDrawerOpen] = useState<boolean>(false);
    const [permissionForm] = Form.useForm();
    const [activeDept, setActiveDept] = useState<DepartmentTreeItem | null>(null);

    // 通用样式变量 (适配明亮主题)
    const cardStyle = { backgroundColor: '#ffffff', borderRadius: 12, border: '1px solid #d0d7de' };
    const textPrimary = '#1f2328';
    const textSecondary = '#656d76';

    const fetchDeptTree = async () => {
        setLoading(true);
        try {
            const res = await adminDeptApi.getDeptTree();
            if (res.success) setTreeData(res.data);
        } catch {
            message.error('无法加载组织架构数据');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDeptTree();
    }, []);

    // --- 处理部门创建 ---
    const handleOpenDeptModal = (parentId: number = 0) => {
        setCurrentParentId(parentId);
        deptForm.resetFields();
        deptForm.setFieldsValue({ orderNum: 10 }); // 默认排序
        setIsDeptModalOpen(true);
    };

    const handleDeptSubmit = async (values: any) => {
        try {
            if (currentParentId === 0) {
                const res = await adminDeptApi.createRootDept(values);
                if (res.code !== 200) {
                    // 手动抛出错误，让 message.error 显示
                    throw new Error(res.message || '部门操作失败');
                }
                message.success('一级部门创建成功');
            } else {
                const res = await adminDeptApi.createChildDept(currentParentId, values);
                console.log(res)
                if (res.code !== 200) {
                    // 手动抛出错误，让 message.error 显示
                    throw new Error(res.message || '部门操作失败');
                }
                message.success('子部门添加成功');
            }
            setIsDeptModalOpen(false);
            fetchDeptTree();
        } catch (error) {
            message.error(error.message);
        }
    };

    // --- 处理部门删除 ---
    const handleDeleteDept = (id: number, name: string) => {
        modal.confirm({
            title: '高危操作确认',
            content: `您确定要彻底删除部门 [${name}] 吗？此操作不可逆。如果该部门下存在子部门或已绑定用户，可能会被系统拒绝。`,
            okText: '确认删除',
            okType: 'danger',
            cancelText: '取消',
            onOk: async () => {
                try {
                    const res = await adminDeptApi.deleteDept(id);
                    if (res.success) {
                        message.success(`部门 [${name}] 已安全删除`);
                        fetchDeptTree(); // 刷新树形表格
                    } else {
                        message.error(res.message || '删除失败，请检查是否存在关联数据');
                    }
                } catch (error: any) {
                    message.error(error.response?.data?.message || '删除请求异常');
                }
            },
        });
    };

    // --- 处理权限分配 ---
    const handleOpenPermissionDrawer = async (record: DepartmentTreeItem) => {
        setActiveDept(record);
        setIsPermissionDrawerOpen(true);
        permissionForm.resetFields();

        // 异步加载该部门当前的直接权限
        try {
            const res = await adminDeptApi.getDeptPermissions(record.id, 'direct');
            if (res.success && res.data?.models) {
                // 将后端结构映射为前端动态表单所需的格式
                const items = res.data.models.map((m: any) => ({ modelAlias: m.modelAlias, scope: m.scope }));
                permissionForm.setFieldsValue({ items });
            }
        } catch {
            message.warning('读取当前权限策略失败，您可直接覆盖配置');
        }
    };

    const handlePermissionSubmit = async (values: { items: any[] }) => {
        if (!activeDept) return;
        try {
            // 如果没有配置项，则传递空数组进行清空
            const payloadItems = values.items || [];
            await adminDeptApi.replaceDeptPermissions(activeDept.id, { items: payloadItems });
            message.success(`部门 [${activeDept.name}] 的模型权限已更新`);
            setIsPermissionDrawerOpen(false);
        } catch {
            message.error('授权策略下发失败');
        }
    };

    // --- 表格列定义 ---
    const columns = [
        {
            title: '组织层级名称',
            dataIndex: 'name',
            key: 'name',
            width: '28%',
            render: (text: string) => <Text style={{ color: textPrimary, fontWeight: 500 }}>{text}</Text>
        },
        {
            title: '排序优先度',
            dataIndex: 'orderNum',
            key: 'orderNum',
            width: '12%',
            render: (num: number) => <Text style={{ color: textSecondary }}>{num}</Text>
        },
        {
            title: '负责人ID (Leader)',
            dataIndex: 'leaderUserId',
            key: 'leaderUserId',
            width: '15%',
            render: (id?: number) => id ? <Tag color="blue" style={{ border: 'none' }}>UID: {id}</Tag> : <Text style={{ color: textSecondary }}>未绑定</Text>
        },
        {
            title: '运转状态',
            dataIndex: 'status',
            key: 'status',
            width: '10%',
            render: (s: number) => s === 1 ? <Tag color="success" style={{ border: 'none' }}>运行中</Tag> : <Tag color="error" style={{ border: 'none' }}>已停用</Tag>
        },
        {
            title: '管控操作',
            key: 'action',
            render: (_: any, record: DepartmentTreeItem) => (
                <Space size="middle">
                    <Button type="link" size="small" onClick={() => handleOpenDeptModal(record.id)} style={{ padding: 0 }}>
                        添加下级架构
                    </Button>
                    <Button
                        type="text"
                        size="small"
                        icon={<SafetyCertificateOutlined />}
                        onClick={() => handleOpenPermissionDrawer(record)}
                        style={{ color: '#8250df', backgroundColor: '#fbeaff', padding: '0 8px' }}
                    >
                        分配模型
                    </Button>
                    <Button
                        type="text"
                        danger
                        size="small"
                        icon={<DeleteOutlined />}
                        onClick={() => handleDeleteDept(record.id, record.name)}
                        style={{ padding: '0 8px' }}
                    >
                        删除
                    </Button>
                </Space>
            ),
        },
    ];

    return (
        <div style={{ backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px' }}>
            <Card
                bordered={false}
                style={{ ...cardStyle, minHeight: 600 }}
                title={
                    <Space>
                        <ApartmentOutlined style={{ color: textPrimary }} />
                        <span style={{ color: textPrimary, fontWeight: 600, fontSize: 16 }}>企业级组织架构管控台</span>
                    </Space>
                }
                extra={
                    <Button type="primary" icon={<PlusOutlined />} onClick={() => handleOpenDeptModal(0)}>
                        新建一级部门
                    </Button>
                }
            >
                <Table
                    columns={columns}
                    dataSource={treeData}
                    rowKey="id"
                    loading={loading}
                    pagination={false}
                    defaultExpandAllRows
                    size="middle"
                />

                {/* 部门创建弹窗 */}
                <Modal
                    title={<span style={{ color: textPrimary, fontWeight: 600 }}>{currentParentId === 0 ? "建立全新一级管控单元" : "添加下设子部门"}</span>}
                    open={isDeptModalOpen}
                    onCancel={() => setIsDeptModalOpen(false)}
                    onOk={() => deptForm.submit()}
                    destroyOnClose
                >
                    <Form form={deptForm} layout="vertical" onFinish={handleDeptSubmit} style={{ marginTop: 24 }}>
                        <Form.Item name="deptName" label="部门名称" rules={[{ required: true, message: '必须输入部门或团队名称' }]}>
                            <Input placeholder="例如：华南区数据科学组 / 核心计费研发部" />
                        </Form.Item>
                        <Form.Item name="orderNum" label="展示优先级次序 (数值越小越优先)">
                            <InputNumber min={1} max={999} style={{ width: '100%' }} />
                        </Form.Item>
                        <Form.Item name="leaderUserId" label="绑定负责人用户UID">
                            <InputNumber style={{ width: '100%' }} placeholder="输入内部用户流水号 (选填)" />
                        </Form.Item>
                        <Form.Item name="tel" label="部门联系方式">
                            <Input placeholder="输入固话或紧急联系方式 (选填)" />
                        </Form.Item>
                    </Form>
                </Modal>

                {/* 权限下发配置抽屉 */}
                <Drawer
                    title={
                        <Space>
                            <SafetyCertificateOutlined style={{ color: '#8250df' }} />
                            <span style={{ color: textPrimary, fontWeight: 600 }}>大模型网关路由鉴权控制</span>
                        </Space>
                    }
                    width={500}
                    onClose={() => setIsPermissionDrawerOpen(false)}
                    open={isPermissionDrawerOpen}
                    extra={
                        <Button type="primary" onClick={() => permissionForm.submit()}>
                            保存配置
                        </Button>
                    }
                >
                    <div style={{ marginBottom: 24, padding: '16px', backgroundColor: '#f6f8fa', borderRadius: 8, border: '1px solid #d0d7de' }}>
                        <Text style={{ color: textPrimary, fontWeight: 500 }}>当前目标控制节点：</Text>
                        <Tag color="#fbeaff" style={{ color: '#8250df', marginLeft: 8, border: 'none', fontWeight: 500, fontSize: 14 }}>
                            {activeDept?.name}
                        </Tag>
                        <div style={{ marginTop: 12, fontSize: 13, color: textSecondary, lineHeight: 1.5 }}>
                            提示：授权控制策略默认采用<b>继承下发机制</b>。若将作用域配置为 <Text code>SUBTREE</Text>，则该组别节点及其下设的所有子孙组别均自动继承此模型路由调用许可。
                        </div>
                    </div>

                    <Form form={permissionForm} layout="vertical" onFinish={handlePermissionSubmit}>
                        <Form.List name="items">
                            {(fields, { add, remove }) => (
                                <>
                                    {fields.map(({ key, name, ...restField }) => (
                                        <Card
                                            size="small"
                                            key={key}
                                            style={{ marginBottom: 16, border: '1px dashed #d0d7de', borderRadius: 8 }}
                                            extra={<Button type="link" onClick={() => remove(name)} style={{ color: '#cf222e', padding: 0 }}>移除此项规则</Button>}
                                        >
                                            <Space style={{ display: 'flex', width: '100%' }} align="baseline">
                                                <Form.Item
                                                    {...restField}
                                                    name={[name, 'modelAlias']}
                                                    rules={[{ required: true, message: '请指定模型路由' }]}
                                                    style={{ width: 220 }}
                                                >
                                                    <Input placeholder="输入网关路由别名 (如 gpt-4o)" />
                                                </Form.Item>

                                                <Form.Item
                                                    {...restField}
                                                    name={[name, 'scope']}
                                                    rules={[{ required: true, message: '必须选择作用域' }]}
                                                    style={{ width: 160 }}
                                                >
                                                    <Select placeholder="控制策略作用域">
                                                        <Option value="SELF">仅限本组调用</Option>
                                                        <Option value="SUBTREE">级联渗透全部分支</Option>
                                                    </Select>
                                                </Form.Item>
                                            </Space>
                                        </Card>
                                    ))}
                                    <Form.Item>
                                        <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />} style={{ height: 40, borderColor: '#0969da', color: '#0969da' }}>
                                            添加新网关路由映射规则项
                                        </Button>
                                    </Form.Item>
                                </>
                            )}
                        </Form.List>
                    </Form>
                </Drawer>
            </Card>
        </div>
    );
};