import React, {useEffect, useState} from 'react';
import type {MenuProps} from 'antd';
import {App as AntdApp, Button, ConfigProvider, Layout, Menu, Space, Typography} from 'antd';
import {
    ApartmentOutlined,
    AppstoreAddOutlined,
    CloudServerOutlined,
    DashboardOutlined,
    KeyOutlined,
    LogoutOutlined,
    SafetyCertificateOutlined,
    TeamOutlined,
    TransactionOutlined,
    UserOutlined
} from '@ant-design/icons';
import {Login} from './pages/Login';
import {VirtualKeyManage} from './pages/VirtualKeyManage';
import {QuotaDashboard} from './pages/QuotaDashboard';
import {QuotaTransactions} from './pages/QuotaTransactions';
import {DepartmentManage} from './pages/DepartmentManage';
import {UserManage} from './pages/UserManage';
import {ModelManage} from './pages/ModelManage';
import {MasterKeyManage} from './pages/MasterKeyManage';
import {VendorManage} from './pages/VendorManage';


const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;


export const App: React.FC = () => {
    // 挂载响应式状态：读取落盘的鉴权凭证
    const [token, setToken] = useState<string | null>(localStorage.getItem('llm_gateway_token'));
    const [username, setUsername] = useState<string | null>(localStorage.getItem('llm_gateway_username'));
    const [selectedKey, setSelectedKey] = useState<string>('dashboard');

    useEffect(() => {
        // 注册全局核心鉴权拦截监听器
        // 一旦下层的 Axios 收到后端的 401/403 异常，触发此回调，优雅洗去前端状态、回退到登录界面
        const handleUnauthorized = () => {
            setToken(null);
            setUsername(null);
        };

        window.addEventListener('llm-gateway-unauthorized', handleUnauthorized);
        return () => {
            window.removeEventListener('llm-gateway-unauthorized', handleUnauthorized);
        };
    }, []);

    const handleLoginSuccess = (newToken: string, newUsername: string) => {
        setToken(newToken);
        setUsername(newUsername);
        setSelectedKey('dashboard'); // 登录后默认直达大盘
    };

    const handleLogout = () => {
        localStorage.removeItem('llm_gateway_token');
        localStorage.removeItem('llm_gateway_username');
        setToken(null);
        setUsername(null);
    };

    const menuItems: MenuProps['items'] = [
        // 分组必须带 key + type: 'group'
        {
            key: 'group-ops',
            type: 'group',
            label: '运维大盘',
            children: [
                { key: 'dashboard', icon: <DashboardOutlined />, label: '配额度量仪表盘' },
            ],
        },
        {
            key: 'group-admin',
            type: 'group',
            label: '网关管控层 (Admin)',
            children: [
                { key: 'departments', icon: <ApartmentOutlined />, label: '组织架构管理' },
                { key: 'users', icon: <TeamOutlined />, label: '用户管理' },
                { key: 'vendors', icon: <CloudServerOutlined />, label: '供应商管理' },
                { key: 'models', icon: <AppstoreAddOutlined />, label: '大模型路由配置' },
                { key: 'master-keys', icon: <SafetyCertificateOutlined />, label: '主密钥(池)' },
            ],
        },
        {
            key: 'group-user',
            type: 'group',
            label: '用户自助层',
            children: [
                { key: 'keys', icon: <KeyOutlined />, label: '应用虚拟密钥' },
                { key: 'transactions', icon: <TransactionOutlined />, label: '配额流水与转配' },
            ],
        },
    ];

    const renderContent = () => {
        switch (selectedKey) {
            case 'dashboard': return <QuotaDashboard />;
            case 'departments': return <DepartmentManage />;
            case 'users': return <UserManage />;
            case 'vendors': return <VendorManage />;
            case 'models': return <ModelManage />;
            case 'master-keys': return <MasterKeyManage />;
            case 'keys': return <VirtualKeyManage />;
            case 'transactions': return <QuotaTransactions />;
            default: return <QuotaDashboard />;
        }
    };

    // 【核心鉴权守卫拦截点】如果没有合法的 Token，无条件熔断主工程，直接回滚到登录组件
    if (!token) {
        return (
            <ConfigProvider
                theme={{
                    // 删除了 theme.darkAlgorithm，默认使用明亮主题
                    token: {
                        borderRadius: 8,
                        colorPrimary: '#0969da', // GitHub Light 经典蓝
                        colorBgLayout: '#f6f8fa', // 页面底层浅灰背景
                        colorBgContainer: '#ffffff', // 卡片纯白背景
                        colorText: '#1f2328', // 主文本颜色
                        colorTextSecondary: '#656d76', // 次要文本颜色
                        colorBorderSecondary: '#d0d7de', // 边框颜色
                    },
                }}
            >
                <AntdApp>
                    <Login onLoginSuccess={handleLoginSuccess} />
                </AntdApp>
            </ConfigProvider>
        );
    }

    return (
        <ConfigProvider
            theme={{
                // 删除了 theme.darkAlgorithm，默认使用明亮主题
                token: {
                    borderRadius: 8,
                    colorPrimary: '#0969da', // GitHub Light 经典蓝
                    colorBgLayout: '#f6f8fa', // 页面底层浅灰背景
                    colorBgContainer: '#ffffff', // 卡片纯白背景
                    colorText: '#1f2328', // 主文本颜色
                    colorTextSecondary: '#656d76', // 次要文本颜色
                    colorBorderSecondary: '#d0d7de', // 边框颜色
                },
            }}
        >
            <AntdApp>
                <Layout style={{ minHeight: '100vh' }}>
                    <Header style={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        background: '#001529',
                        padding: '0 20px',
                        height: '60px'
                    }}>
                        <Title level={4} style={{ color: '#fff', margin: 0, letterSpacing: '0.5px' }}>
                            QRouter - LLM Gateway
                        </Title>

                        {/* 顶栏右侧：展示在线用户信息与标准化登出操作 */}
                        <Space size="large">
                            <Space style={{ color: 'rgba(255,255,255,0.85)' }}>
                                <UserOutlined />
                                <Text style={{ color: '#fff' }}>{username || '系统用户'}</Text>
                            </Space>
                            <Button
                                type="text"
                                icon={<LogoutOutlined />}
                                style={{ color: 'rgba(255,255,255,0.65)' }}
                                onClick={handleLogout}
                            >
                                退出系统
                            </Button>
                        </Space>
                    </Header>
                    <Layout>
                        <Sider width={240} style={{ background: '#fff', borderRight: '1px solid #f0f0f0' }}>
                            <Menu
                                mode="inline"
                                selectedKeys={[selectedKey]}
                                style={{ height: '100%', borderRight: 0, paddingTop: 10 }}
                                items={menuItems}
                                onClick={({ key }) => setSelectedKey(key)}
                            />
                        </Sider>
                        <Layout style={{ padding: '24px', background: '#f5f7fa' }}>
                            <Content style={{ margin: 0, minHeight: 280 }}>
                                {renderContent()}
                            </Content>
                        </Layout>
                    </Layout>
                </Layout>
            </AntdApp>
        </ConfigProvider>
    );
};