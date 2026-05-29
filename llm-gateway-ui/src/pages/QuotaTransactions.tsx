import React, { useEffect, useState } from 'react';
import {
    App,
    Button,
    Card,
    Col,
    Descriptions,
    Empty,
    Form,
    Input,
    InputNumber,
    Row,
    Select,
    Space,
    Statistic,
    Table,
    Tabs,
    Tag,
    Typography
} from 'antd';
import { AuditOutlined, ArrowRightOutlined, SearchOutlined } from '@ant-design/icons';
import { quotaApi, QuotaTransactionItem, UserQuotaAccountSnapshot } from '../api/llmGatewayApi';

const { Option } = Select;
const { Text } = Typography;

const quotaChangeTypeTextMap: Record<string, string> = {
    ADMIN_GRANT: '管理员发放',
    ADMIN_RECLAIM: '管理员回收',
    TRANSFER_OUT: '转出',
    TRANSFER_IN: '转入',
    USAGE_RESERVE: 'LLM消费',
    USAGE_SETTLE: 'LLM结算',
    USAGE_REFUND: 'LLM退回',
    QUOTA_EXPIRE: '配额过期',
};

function getQuotaChangeTypeColor(type: string): string {
    if (type === 'ADMIN_RECLAIM' || type === 'TRANSFER_OUT' || type === 'USAGE_RESERVE' || type === 'QUOTA_EXPIRE') {
        return 'orange';
    }
    if (type === 'USAGE_REFUND' || type === 'TRANSFER_IN' || type === 'ADMIN_GRANT') {
        return 'green';
    }
    return 'blue';
}

export const QuotaTransactions: React.FC = () => {
    const { message } = App.useApp();
    const [activeTab, setActiveTab] = useState<string>('ledger');
    const [loading, setLoading] = useState<boolean>(false);
    const [transactions, setTransactions] = useState<QuotaTransactionItem[]>([]);
    const [total, setTotal] = useState<number>(0);
    const [page, setPage] = useState<number>(1);
    const [filterType, setFilterType] = useState<string | undefined>(undefined);

    const [allowTransfer, setAllowTransfer] = useState<boolean>(false);
    const [quotaSnapshot, setQuotaSnapshot] = useState<UserQuotaAccountSnapshot | null>(null);
    const [quotaLoading, setQuotaLoading] = useState<boolean>(false);
    const [transferLoading, setTransferLoading] = useState<boolean>(false);
    const [form] = Form.useForm();

    const fetchQuotaSnapshot = async () => {
        setQuotaLoading(true);
        try {
            const res = await quotaApi.getCurrentSnapshot();
            if (res.success) {
                setQuotaSnapshot(res.data);
                setAllowTransfer(res.data.allowTransferOut);
            }
        } catch {
            message.error('用户配额详情读取错误');
        } finally {
            setQuotaLoading(false);
        }
    };

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

    useEffect(() => {
        if (activeTab === 'ledger') {
            fetchLedger(page, filterType);
        }
    }, [page, filterType, activeTab]);

    useEffect(() => {
        fetchQuotaSnapshot();
    }, []);

    const handleTransferSubmit = async (values: any) => {
        setTransferLoading(true);
        try {
            const res = await quotaApi.transferQuota({
                targetUser: values.targetUser.trim(),
                transferAmount: values.transferAmount,
                remark: values.remark
            });
            if (res.success) {
                message.success(`划转成功！顺利向目标用户 [${values.targetUser}] 共享划配 ${values.transferAmount.toLocaleString()} 元`);
                form.resetFields();
                setActiveTab('ledger');
                fetchQuotaSnapshot();
                fetchLedger(1, filterType);
                setPage(1);
            }
        } catch {
            message.error('配额转配底层链路异常，请核对额度余额');
        } finally {
            setTransferLoading(false);
        }
    };

    const ledgerColumns = [
        { title: '发生时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
        { title: '业务单号/批次', dataIndex: 'bizNo', key: 'bizNo', ellipsis: true },
        {
            title: '变更类别',
            dataIndex: 'changeType',
            key: 'changeType',
            render: (type: string) => <Tag color={getQuotaChangeTypeColor(type)}>{quotaChangeTypeTextMap[type] || type}</Tag>
        },
        {
            title: '额度变动 (Delta)',
            dataIndex: 'deltaAmount',
            key: 'deltaAmount',
            render: (delta: number) => {
                const isNegative = delta < 0;
                return (
                    <Text strong style={{ color: isNegative ? '#ff4d4f' : '#52c41a' }}>
                        {isNegative ? '' : '+'}{delta.toLocaleString()}
                    </Text>
                );
            }
        },
        {
            title: '变动额度',
            key: 'availableBalance',
            render: (_: any, record: QuotaTransactionItem) => (
                <span>{record.availableBeforeAmount.toLocaleString()} <ArrowRightOutlined style={{ fontSize: 11 }} /> {record.availableAfterAmount.toLocaleString()}</span>
            )
        },
        { title: '流水备注', dataIndex: 'remark', key: 'remark', ellipsis: true }
    ];

    return (
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Card title="用户配额详情" style={{ width: '100%' }} loading={quotaLoading}>
                {quotaSnapshot ? (
                    <>
                        <Row gutter={16}>
                            <Col span={6}>
                                <Statistic title="总配额" value={quotaSnapshot.currentQuotaAmount} />
                            </Col>
                            <Col span={6}>
                                <Statistic title="当前可用配额" value={quotaSnapshot.availableAmount} valueStyle={{ color: '#0969da' }} />
                            </Col>
                            <Col span={6}>
                                <Statistic title="已使用配额" value={quotaSnapshot.usedAmount} />
                            </Col>
                            <Col span={6}>
                                <Statistic title="已过期配额" value={quotaSnapshot.expiredAmount} />
                            </Col>
                        </Row>
                        <Descriptions column={3} size="small" bordered style={{ marginTop: 16 }}>
                            <Descriptions.Item label="累计转入">{quotaSnapshot.transferredInAmount}</Descriptions.Item>
                            <Descriptions.Item label="累计转出">{quotaSnapshot.transferredOutAmount}</Descriptions.Item>
                            <Descriptions.Item label="允许转配">{quotaSnapshot.allowTransferOut ? '是' : '否'}</Descriptions.Item>
                            <Descriptions.Item label="最早过期时间">{quotaSnapshot.earliestExpireAt || '-'}</Descriptions.Item>
                            <Descriptions.Item label="更新时间" span={2}>{quotaSnapshot.updatedAt || '-'}</Descriptions.Item>
                        </Descriptions>
                    </>
                ) : (
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description="暂无用户配额详情" />
                )}
            </Card>

            <Card style={{ width: '100%' }}>
                <Tabs activeKey={activeTab} onChange={setActiveTab}>
                    <Tabs.TabPane tab={<span><AuditOutlined />核算流水账本</span>} key="ledger">
                        <Space style={{ marginBottom: 16 }}>
                            <Select placeholder="按变更类别过滤" allowClear style={{ width: 220 }} onChange={(val) => { setFilterType(val); setPage(1); }}>
                                {Object.entries(quotaChangeTypeTextMap).map(([value, label]) => (
                                    <Option key={value} value={value}>{label}</Option>
                                ))}
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
                                    <Form.Item name="targetUser" label="划转目标用户账号" rules={[{ required: true, message: '请输入合法的转入方账号' }]}>
                                        <Input placeholder="请输入目标账号（用户名、手机号或邮箱）" />
                                    </Form.Item>
                                    <Form.Item name="transferAmount" label="调配划转金额" rules={[{ required: true, message: '请输入划配金额' }]}>
                                        <InputNumber style={{ width: '100%' }} min={0.000001} placeholder="请输入金额" precision={6} />
                                    </Form.Item>
                                    <Form.Item name="remark" label="转配事由备注">
                                        <Input.TextArea rows={4} placeholder="例如: 支援xx用户本月大项目开发额度" maxLength={200} />
                                    </Form.Item>
                                    <Form.Item>
                                        <Button type="primary" htmlType="submit" loading={transferLoading}>确认并提交</Button>
                                    </Form.Item>
                                </Form>
                            </div>
                        )}
                    </Tabs.TabPane>
                </Tabs>
            </Card>
        </Space>
    );
};
