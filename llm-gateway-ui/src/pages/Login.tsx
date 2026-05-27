import React, { useState } from 'react';
import { Card, Form, Input, Button, Typography, Space, App } from 'antd';
import { UserOutlined, LockOutlined, CloudServerOutlined } from '@ant-design/icons';
import { authApi, LoginRequest } from '../api/llmGatewayApi';

const { Title, Text } = Typography;

interface LoginProps {
    onLoginSuccess: (token: string, username: string) => void;
}

export const Login: React.FC<LoginProps> = ({ onLoginSuccess }) => {
    const { message } = App.useApp();
    const [loading, setLoading] = useState<boolean>(false);

    const onFinish = async (values: LoginRequest) => {
        setLoading(true);
        try {
            const res = await authApi.login(values);
            if (res.success && res.data?.token) {
                message.success('身份验证成功，欢迎回来！');

                // 将凭证落盘存储
                localStorage.setItem('llm_gateway_token', res.data.token);
                localStorage.setItem('llm_gateway_username', res.data.username);

                // 触发父级状态刷新，切入管理后台主界面
                onLoginSuccess(res.data.token, res.data.username);
            } else {
                message.error(res.message || '登录验证失败，请检查凭证');
            }
        } catch (error: any) {
            const errorMsg = error.response?.data?.message || '无法连接到大模型网关鉴权中心';
            message.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '100vh',
            background: 'linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%)'
        }}>
            <Card bordered={false} style={{ width: 400, borderRadius: 12, boxShadow: '0 8px 24px rgba(0,0,0,0.2)' }}>
                <div style={{ textAlign: 'center', marginBottom: 32 }}>
                    <Space size="middle" style={{ marginBottom: 12 }}>
                        <CloudServerOutlined style={{ fontSize: 32, color: '#0052cc' }} />
                        <Title level={3} style={{ margin: 0, fontWeight: 600 }}>LLM Gateway</Title>
                    </Space>
                    <div>
                        <Text type="secondary">多看，多做，多思考 · 统一算力网关控制台</Text>
                    </div>
                </div>

                <Form name="login_form" layout="vertical" onFinish={onFinish} size="large">
                    <Form.Item name="username" rules={[{ required: true, message: '请输入您的用户名或注册邮箱！' }]}>
                        <Input prefix={<UserOutlined style={{ color: '#bfbfbf' }} />} placeholder="用户名或邮箱" />
                    </Form.Item>

                    <Form.Item name="password" rules={[{ required: true, message: '请输入您的登录安全密码！' }]}>
                        <Input.Password prefix={<LockOutlined style={{ color: '#bfbfbf' }} />} placeholder="安全密码" />
                    </Form.Item>

                    <Form.Item style={{ marginTop: 24 }}>
                        <Button type="primary" htmlType="submit" loading={loading} block style={{ height: 44, fontWeight: 'bold' }}>
                            账户安全登录
                        </Button>
                    </Form.Item>
                </Form>
            </Card>
        </div>
    );
};