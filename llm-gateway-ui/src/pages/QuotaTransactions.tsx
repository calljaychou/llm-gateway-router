import React, { useEffect, useState } from 'react';
import { Card, Tabs, Table, Select, Form, InputNumber, Input, Button, Space, App, Tag, Typography } from 'antd';
import { AuditOutlined, ArrowRightOutlined, SearchOutlined } from '@ant-design/icons';
import { quotaApi, QuotaTransactionItem } from '../api/llmGatewayApi';

const { Option } = Select;
const { Text } = Typography;

export const QuotaTransactions: React.FC = () => {
    const { message } = App.useApp();
    const [activeTab, setActiveTab] = useState<string>('ledger');
    const [loading, setLoading] = useState<boolean>(false);
    const [transactions, setTransactions] = useState<QuotaTransactionItem[]>([]);
    const [total, setTotal] = useState<number>(0);
    const [page, setPage] = useState<number>(1);
    const [filterType, setFilterType] = useState<string | undefined>(undefined);

    const [allowTransfer, setAllowTransfer] = useState<boolean>(false);
    const [transferLoading, setTransferLoading] = useState<boolean>(false);
    const [form] = Form.useForm();

    const fetchLedger = async (currentPage: number, type?: string) => {
        setLoading(true);
        try {
            const res = await quotaApi.listTransactions(currentPage, 10, type);
            if (res.success) {
                setTransactions(res.data.list);
                setTotal(res.data.total);
            }
        } catch {
            message.error('流水平账日志读取错误');
        } finally {
            setLoading(false);
        }
    };

    const checkTransferPermission = async () => {
        try {
            const res = await quotaApi.getCurrentSnapshot();
            if (res.success) setAllowTransfer(res.data.allowTransferOut);
        } catch {}
    };

    useEffect(() => {
        if (activeTab === 'ledger') {
            fetchLedger(page, filterType);
        } else {
            checkTransferPermission();
        }
    }, [page, filterType, activeTab]);

    const handleTransferSubmit = async (values: any) => {
        setTransferLoading(true);
        try {
            const res = await quotaApi.transferQuota({
                targetUserId: values.targetUserId,
                transferTokens: values.transferTokens,
                remark: values.remark
            });
            if (res.success) {
                message.success(`划转成功！顺利向目标用户 [${values.targetUserId}] 共享划配 ${values.transferTokens.toLocaleString()} Tokens`);
                form.resetFields();
                setActiveTab('ledger');
            }
        } catch {
            message.error('配额转配底层链路异常，请核对额度余额');
        } finally {
            setTransferLoading(false);
        }
    };

    const ledgerColumns = [
        { title: '时间轴', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
        { title: '业务单号/批次', dataIndex: 'bizNo', key: 'bizNo', ellipsis: true },
        {
            title: '审计变更类别',
            dataIndex: 'changeType',
            key: 'changeType',
            render: (type: string) => <Tag color={type.includes('CONSUME') ? 'orange' : 'green'}>{type}</Tag>
        },
        {
            title: '额度变动 (Delta)',
            dataIndex: 'deltaTokens',
            key: 'deltaTokens',
            render: (delta: number) => {
                const isNegative = delta < 0;
                return (
                    <Text strong style={{ color: isNegative ? '#ff4d4f' : '#52c41a' }}>
                        {isNegative ? '' : '+'}{delta.toLocaleString()} Tokens
                    </Text>
                );
            }
        },
        {
            title: '可用额度平账演变',
            key: 'availableBalance',
            render: (_: any, record: QuotaTransactionItem) => (
                <span>{record.availableBefore.toLocaleString()} <ArrowRightOutlined style={{ fontSize: 11 }} /> {record.availableAfter.toLocaleString()}</span>
            )
        },
        { title: '流水备注', dataIndex: 'remark', key: 'remark', ellipsis: true }
    ];

    return (
        <Card style={{ width: '100%' }}>
            <Tabs activeKey={activeTab} onChange={setActiveTab}>
                <Tabs.TabPane tab={<span><AuditOutlined />核算流水账本</span>} key="ledger">
                    <Space style={{ marginBottom: 16 }}>
                        <Select placeholder="按变更类别过滤" allowClear style={{ width: 220 }} onChange={(val) => { setFilterType(val); setPage(1); }}>
                            <Option value="MODEL_CONSUME">模型算力消费扣减</Option>
                            <Option value="ADMIN_ADJUST">管理员手动调额</Option>
                            <Option value="QUOTA_TRANSFER">账户自主转配</Option>
                            <Option value="EXPIRED_RECOVERY">过期失效回收</Option>
                        </Select>
                        <Button type="primary" icon={<SearchOutlined />} onClick={() => fetchLedger(page, filterType)}>刷新账单</Button>
                    </Space>

                    <Table
                        columns={ledgerColumns}
                        dataSource={transactions}
                        rowKey="transactionId"
                        loading={loading}
                        pagination={{
                            current: page,
                            pageSize: 10,
                            total: total,
                            onChange: (p) => setPage(p)
                        }}
                    />
                </Tabs.TabPane>

                <Tabs.TabPane tab={<span><ArrowRightOutlined />配额自主转配共享</span>} key="transfer">
                    {!allowTransfer ? (
                        <div style={{ padding: '24px 0', textAlign: 'center', color: '#8c8c8c' }}>
                            根据安全合规审计要求，您当前的专属组织账户未开启 [allowTransferOut] 向外划转特权。如需变更，请联系部门 IT 网关管理员。
                        </div>
                    ) : (
                        <div style={{ maxWidth: 600, margin: '20px 0' }}>
                            <Form form={form} layout="vertical" onFinish={handleTransferSubmit}>
                                <Form.Item name="targetUserId" label="划转目标用户唯一识别码 (Target User ID)" rules={[{ required: true, message: '请输入合法的转入方用户ID' }]}>
                                    <InputNumber style={{ width: '100%' }} placeholder="请输入目标系统的数字 User ID" precision={0} />
                                </Form.Item>
                                <Form.Item name="transferTokens" label="调配划转 Token 资产总量" rules={[{ required: true, message: '请输入划配总量' }]}>
                                    <InputNumber style={{ width: '100%' }} min={1} placeholder="请输入大模型 Token 资源划配数" precision={0} />
                                </Form.Item>
                                <Form.Item name="remark" label="转配审批事由/备注留痕">
                                    <Input.TextArea rows={4} placeholder="例如: 支援数据分析团队第二季度大模型微调推理额度" maxLength={200} />
                                </Form.Item>
                                <Form.Item>
                                    <Button type="primary" htmlType="submit" loading={transferLoading}>立即执行跨境安全划转</Button>
                                </Form.Item>
                            </Form>
                        </div>
                    )}
                </Tabs.TabPane>
            </Tabs>
        </Card>
    );
};