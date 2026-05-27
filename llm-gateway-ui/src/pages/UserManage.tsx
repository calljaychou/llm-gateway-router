import React from 'react';
import { Card, Table, Button, Space, Tag, Input } from 'antd';
import { TeamOutlined, PlusOutlined } from '@ant-design/icons';

const { Search } = Input;

export const UserManage: React.FC = () => {
    const columns = [
        { title: '用户ID', dataIndex: 'userId', key: 'userId' },
        { title: '用户名', dataIndex: 'username', key: 'username' },
        { title: '所属部门', dataIndex: 'deptName', key: 'deptName' },
        { title: '系统角色', dataIndex: 'roles', key: 'roles', render: (roles: string[]) => roles.map(r => <Tag key={r} color="blue">{r}</Tag>) },
        {
            title: '操作',
            key: 'action',
            render: () => (
                <Space size="small">
                    <Button type="link" size="small">调额</Button>
                    <Button type="link" size="small" danger>停用</Button>
                </Space>
            ),
        },
    ];

    const mockUsers = [
        { userId: 1001, username: 'dev_alice', deptName: '研发中心', roles: ['user', 'dept_admin'] },
        { userId: 1002, username: 'data_bob', deptName: '数据分析部', roles: ['user'] },
    ];

    return (
        <Card title={<Space><TeamOutlined />全局用户管理</Space>}>
            <Space style={{ marginBottom: 16, justifyContent: 'space-between', width: '100%' }}>
                <Search placeholder="搜索用户名或邮箱" style={{ width: 300 }} />
                <Button type="primary" icon={<PlusOutlined />}>开通新用户</Button>
            </Space>
            <Table columns={columns} dataSource={mockUsers} rowKey="userId" />
        </Card>
    );
};