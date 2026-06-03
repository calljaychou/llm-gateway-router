import React from 'react';
import { Row, Col, Typography, Space, Tag, Card, Badge, Tooltip, Progress, Table, DatePicker } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import dayjs from 'dayjs';
import {
    ClockCircleOutlined,
} from '@ant-design/icons';
import {
    quotaApi,
    userUsageApi,
    virtualKeyApi,
    type UserModelUsageCountResult,
    type UserQuotaAccountSnapshot,
    type UserUsageLogListItem,
    type UserUsageHourlyHeatmapResult
} from '../api/llmGatewayApi';
import { formatAmount } from '../utils/format';

const { Title, Text } = Typography;
const HOURS = Array.from({ length: 24 }, (_, i) => i);
const HOUR_ROW_STARTS = [0, 6, 12, 18];
const HOURS_PER_HEATMAP_ROW = 6;
const HEATMAP_CELL_SIZE = 15;
const HEATMAP_CELL_GAP = 3;
const HEATMAP_DAY_WIDTH = HOURS_PER_HEATMAP_ROW * HEATMAP_CELL_SIZE + (HOURS_PER_HEATMAP_ROW - 1) * HEATMAP_CELL_GAP;
const MOCK_RECENT_MONTH_HOURLY_HEATMAP = buildMockRecentMonthHourlyHeatmap();
const MODEL_USAGE_CARD_HEIGHT = 240;
const USAGE_LOG_PAGE_SIZE = 6;
const DEFAULT_USAGE_LOG_END_DATE = formatDate(new Date());
const DEFAULT_USAGE_LOG_START_DATE = formatDate(offsetDate(new Date(), -29));

interface QuotaDashboardProps {
    onNavigate?: (key: string) => void;
}

export const QuotaDashboard: React.FC<QuotaDashboardProps> = ({ onNavigate }) => {
    // 定义通用样式变量以保证统一
    const cardStyle = { backgroundColor: '#ffffff', padding: 20, borderRadius: 12, border: '1px solid #d0d7de' };
    const textPrimary = '#1f2328';
    const textSecondary = '#656d76';
    const loginUserName = localStorage.getItem('llm_gateway_name') || localStorage.getItem('llm_gateway_username') || '系统用户';
    const useTime = localStorage.getItem('use_time');
    const useDays = calculateUseDays(useTime);
    const hourlyHeatmap = MOCK_RECENT_MONTH_HOURLY_HEATMAP;
    const [availableModels, setAvailableModels] = React.useState<string[]>([]);
    const [quotaSnapshot, setQuotaSnapshot] = React.useState<UserQuotaAccountSnapshot | null>(null);
    const [virtualKeySummary, setVirtualKeySummary] = React.useState({ total: 0, active: 0 });
    const [modelUsageCounts, setModelUsageCounts] = React.useState<UserModelUsageCountResult[]>([]);
    const [usageLogs, setUsageLogs] = React.useState<UserUsageLogListItem[]>([]);
    const [usageLogsLoading, setUsageLogsLoading] = React.useState<boolean>(false);
    const [usageLogsPage, setUsageLogsPage] = React.useState<number>(1);
    const [usageLogsTotal, setUsageLogsTotal] = React.useState<number>(0);
    const [usageLogsStartDate, setUsageLogsStartDate] = React.useState<string>(DEFAULT_USAGE_LOG_START_DATE);
    const [usageLogsEndDate, setUsageLogsEndDate] = React.useState<string>(DEFAULT_USAGE_LOG_END_DATE);
    const heatmapScrollRef = React.useRef<HTMLDivElement | null>(null);
    const totalBalance = quotaSnapshot?.currentQuotaAmount || 0;
    const currentBalance = quotaSnapshot?.availableAmount || 0;
    const usedBalance = quotaSnapshot?.usedAmount || 0;
    const usagePercent = totalBalance > 0 ? Math.min(100, Math.max(0, Math.round((usedBalance / totalBalance) * 100))) : 0;
    const maxModelUsageCount = Math.max(1, ...modelUsageCounts.map(model => model.usageCount));
    const gatewayBaseUrl = `${window.location.origin}/v1/chat/completions`;

    React.useEffect(() => {
        const heatmapScroll = heatmapScrollRef.current;
        if (heatmapScroll) heatmapScroll.scrollLeft = heatmapScroll.scrollWidth;
    }, [hourlyHeatmap.days.length]);

    React.useEffect(() => {
        let ignore = false;
        userUsageApi.getUserEffectivePermissions()
            .then(res => {
                if (!ignore && res.success && res.data) {
                    setAvailableModels(res.data.allowedModels || []);
                }
            })
            .catch(() => undefined);

        return () => {
            ignore = true;
        };
    }, []);

    React.useEffect(() => {
        let ignore = false;
        quotaApi.getCurrentSnapshot()
            .then(res => {
                if (!ignore && res.success && res.data) {
                    setQuotaSnapshot(res.data);
                }
            })
            .catch(() => undefined);

        return () => {
            ignore = true;
        };
    }, []);

    React.useEffect(() => {
        let ignore = false;
        virtualKeyApi.listKeys()
            .then(res => {
                if (!ignore && res.success && res.data) {
                    const keys = res.data.keys || [];
                    setVirtualKeySummary({
                        total: keys.length,
                        active: keys.filter(key => key.status === 1).length,
                    });
                }
            })
            .catch(() => undefined);

        return () => {
            ignore = true;
        };
    }, []);

    React.useEffect(() => {
        let ignore = false;
        userUsageApi.listModelUsageCounts()
            .then(res => {
                if (!ignore) {
                    setModelUsageCounts(res.success && res.data ? res.data : []);
                }
            })
            .catch(() => {
                if (!ignore) setModelUsageCounts([]);
            });

        return () => {
            ignore = true;
        };
    }, []);

    React.useEffect(() => {
        let ignore = false;
        setUsageLogsLoading(true);
        userUsageApi.listUsageLogs(usageLogsPage, USAGE_LOG_PAGE_SIZE, usageLogsStartDate, usageLogsEndDate)
            .then(res => {
                if (!ignore) {
                    setUsageLogs(res.success && res.data ? res.data.list || [] : []);
                    setUsageLogsTotal(res.success && res.data ? res.data.total || 0 : 0);
                }
            })
            .catch(() => {
                if (!ignore) {
                    setUsageLogs([]);
                    setUsageLogsTotal(0);
                }
            })
            .finally(() => {
                if (!ignore) setUsageLogsLoading(false);
            });

        return () => {
            ignore = true;
        };
    }, [usageLogsPage, usageLogsStartDate, usageLogsEndDate]);

    const handleUsageLogDateChange = (_dates: any, dateStrings: [string, string]) => {
        if (!dateStrings[0] || !dateStrings[1]) return;
        setUsageLogsStartDate(dateStrings[0]);
        setUsageLogsEndDate(dateStrings[1]);
        setUsageLogsPage(1);
    };

    const usageLogColumns: ColumnsType<UserUsageLogListItem> = [
        {
            title: '请求ID',
            dataIndex: 'requestId',
            key: 'requestId',
            width: 250,
            ellipsis: true,
            render: (requestId: string) => <Text code title={requestId} style={{ color: textSecondary }}>{requestId || '-'}</Text>,
        },
        {
            title: '供应商',
            dataIndex: 'vendorName',
            key: 'vendorName',
            width: 120,
            render: (vendorName: string) => vendorName ? <Tag color="blue" style={{ marginInlineEnd: 0 }}>{vendorName}</Tag> : '-',
        },
        {
            title: '模型',
            dataIndex: 'modelName',
            key: 'modelName',
            ellipsis: true,
            render: (modelName: string) => <Text title={modelName} style={{ color: textPrimary, fontWeight: 600 }}>{modelName || '-'}</Text>,
        },
        { title: '输入Token', dataIndex: 'inputTokens', key: 'inputTokens', width: 86, align: 'right' },
        { title: '输出Token', dataIndex: 'outputTokens', key: 'outputTokens', width: 86, align: 'right' },
        { title: '缓存Token', dataIndex: 'cachedTokens', key: 'cachedTokens', width: 86, align: 'right' },
        { title: '总Token', dataIndex: 'totalTokens', key: 'totalTokens', width: 86, align: 'right' },
        { title: '使用时间', dataIndex: 'usedAt', key: 'usedAt', width: 170, render: (usedAt?: string) => usedAt || '-' },
        { title: '耗时', dataIndex: 'latencyMs', key: 'latencyMs', width: 92, align: 'right', render: (latencyMs: number) => `${latencyMs || 0} ms` },
    ];

    return (
        <div style={{ backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px', color: textPrimary }}>
            {/* 1. Header Section */}
            <div style={{ marginBottom: 32 }}>
                <Title level={2} style={{ color: textPrimary, margin: 0 }}>Hello! {loginUserName}</Title>
                <Text style={{ color: textSecondary, fontSize: 16 }}>
                    This is your day <span style={{ color: '#0969da', fontWeight: 'bold' }}>{useDays}</span> of using TRAK.
                </Text>
                <div style={{ marginTop: 12 }}>
                    <Space>
                        <Tag color="#ddf4ff" style={{ color: '#0969da', border: 'none', fontWeight: 500 }}># Guru</Tag>
                        <Tag color="#dafbe1" style={{ color: '#1a7f37', border: 'none', fontWeight: 500 }}># Agent Crafter</Tag>
                        <Tag color="#fbeaff" style={{ color: '#8250df', border: 'none', fontWeight: 500 }}># Single-Model BFF</Tag>
                        <Tag color="#fff8c5" style={{ color: '#9a6700', border: 'none', fontWeight: 500 }}># Early Bird</Tag>
                    </Space>
                </div>
            </div>

            {/* 2. Recent Month Hourly Usage Heatmap */}
            <Card bordered={false} bodyStyle={{ ...cardStyle, padding: 24 }} style={{ marginBottom: 24, background: 'transparent' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, marginBottom: 18, alignItems: 'center', flexWrap: 'wrap' }}>
                    <div style={{ display: 'flex', alignItems: 'baseline', gap: 12, flexWrap: 'wrap' }}>
                        <Text style={{ color: textPrimary, fontWeight: 'bold' }}>用量情况</Text>
                        <div style={{ color: textSecondary, fontSize: 12 }}>
                            {hourlyHeatmap.startDate} - {hourlyHeatmap.endDate}
                            <span style={{ marginLeft: 12, color: textPrimary, fontWeight: 600 }}>{hourlyHeatmap.totalCount}</span>
                            <span style={{ marginLeft: 4 }}>requests</span>
                            <span style={{ marginLeft: 12 }}>peak</span>
                            <span style={{ marginLeft: 4, color: textPrimary, fontWeight: 600 }}>{hourlyHeatmap.maxHourlyCount}</span>
                        </div>
                    </div>
                    <Space size="small" style={{ minHeight: 22 }}>
                        <span style={{ fontSize: 12, color: textSecondary }}>Less</span>
                        {[0, 1, 2, 3, 4].map(v => (
                            <div key={v} style={{ width: 12, height: 12, backgroundColor: getHourlyHeatmapColor(v, 4), borderRadius: 2, border: '1px solid rgba(31,35,40,0.06)' }} />
                        ))}
                        <span style={{ fontSize: 12, color: textSecondary }}>More</span>
                    </Space>
                </div>
                <div ref={heatmapScrollRef} style={{ overflowX: 'auto', paddingBottom: 2 }}>
                    <div
                        style={{
                            display: 'grid',
                            gridTemplateColumns: `48px repeat(${hourlyHeatmap.days.length}, ${HEATMAP_DAY_WIDTH}px)`,
                            gridTemplateRows: `18px repeat(4, ${HEATMAP_CELL_SIZE}px)`,
                            columnGap: 7,
                            rowGap: HEATMAP_CELL_GAP,
                            alignItems: 'center',
                            minWidth: 48 + hourlyHeatmap.days.length * (HEATMAP_DAY_WIDTH + 7),
                        }}
                    >
                        <div />
                        {hourlyHeatmap.days.map((day, dayIndex) => (
                            <div key={day.date} style={{ color: textSecondary, fontSize: 10, textAlign: 'left', lineHeight: '14px' }}>
                                {dayIndex % 3 === 0 || dayIndex === hourlyHeatmap.days.length - 1 ? formatHeatmapDateLabel(day.date) : ''}
                            </div>
                        ))}
                        {HOUR_ROW_STARTS.map(rowStartHour => (
                            <React.Fragment key={rowStartHour}>
                                <div style={{ color: textSecondary, fontSize: 10, lineHeight: '10px', whiteSpace: 'nowrap' }}>
                                    {formatHourRangeLabel(rowStartHour)}
                                </div>
                                {hourlyHeatmap.days.map(day => (
                                    <div
                                        key={`${day.date}-${rowStartHour}`}
                                        style={{
                                            display: 'grid',
                                            gridTemplateColumns: `repeat(${HOURS_PER_HEATMAP_ROW}, ${HEATMAP_CELL_SIZE}px)`,
                                            gap: HEATMAP_CELL_GAP,
                                        }}
                                    >
                                        {day.hours
                                            .slice(rowStartHour, rowStartHour + HOURS_PER_HEATMAP_ROW)
                                            .map(hour => (
                                                <Tooltip
                                                    key={`${day.date}-${hour.hour}`}
                                                    title={`${day.date} ${formatHourLabel(hour.hour)} · ${hour.requestCount} requests`}
                                                >
                                                    <div
                                                        aria-label={`${day.date} ${formatHourLabel(hour.hour)} ${hour.requestCount} requests`}
                                                        style={{
                                                            width: HEATMAP_CELL_SIZE,
                                                            height: HEATMAP_CELL_SIZE,
                                                            backgroundColor: getHourlyHeatmapColor(hour.requestCount, hourlyHeatmap.maxHourlyCount),
                                                            borderRadius: 2,
                                                            border: '1px solid rgba(31,35,40,0.06)',
                                                        }}
                                                    />
                                                </Tooltip>
                                            ))}
                                    </div>
                                ))}
                            </React.Fragment>
                        ))}
                    </div>
                </div>
            </Card>

            {/* 3. Balance Overview */}
            <div style={{ ...cardStyle, marginBottom: 24 }}>
                <Text style={{ color: textSecondary, display: 'block', marginBottom: 20, fontWeight: 500 }}>用户余额概览</Text>
                <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, marginBottom: 18, flexWrap: 'wrap' }}>
                    <div>
                        <div style={{ color: textPrimary, fontSize: 15, marginBottom: 4 }}>当前余额¥ {formatAmount(currentBalance)}</div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                        <div style={{ color: textPrimary, fontSize: 15, marginBottom: 4 }}>总余额¥ {formatAmount(totalBalance)}</div>
                    </div>
                </div>
                <Progress
                    percent={usagePercent}
                    showInfo={false}
                    strokeColor="#0969da"
                    trailColor="#eaeef2"
                    strokeLinecap="round"
                />
                <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 8, color: textSecondary, fontSize: 12 }}>
                    <span>使用比例</span>
                    <span style={{ color: textPrimary, fontWeight: 600 }}>{usagePercent}%</span>
                </div>
            </div>

            {/* 4. Available Models */}
            <div style={{ ...cardStyle, marginBottom: 24 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, gap: 12, flexWrap: 'wrap' }}>
                    <Text style={{ color: textPrimary, fontWeight: 700 }}>可用模型</Text>
                    <Text style={{ color: textSecondary, fontSize: 12 }}>{availableModels.length} models</Text>
                </div>
                <Row gutter={[12, 12]}>
                    {availableModels.map(modelName => (
                        <Col key={modelName} xs={24} sm={12} md={8} lg={6} xl={4}>
                            <div
                                style={{
                                    height: 76,
                                    border: '1px solid #d0d7de',
                                    borderRadius: 8,
                                    backgroundColor: '#f6f8fa',
                                    padding: '12px 14px',
                                    display: 'flex',
                                    flexDirection: 'column',
                                    alignItems: 'flex-start',
                                    justifyContent: 'center',
                                    gap: 8,
                                }}
                            >
                                <Text
                                    title={modelName}
                                    style={{
                                        color: textPrimary,
                                        fontWeight: 600,
                                        width: '100%',
                                        textAlign: 'left',
                                        minWidth: 0,
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis',
                                        whiteSpace: 'nowrap',
                                    }}
                                >
                                    {modelName}
                                </Text>
                                <Tag color="success" style={{ marginInlineEnd: 0 }}>可用</Tag>
                            </div>
                        </Col>
                    ))}
                    {availableModels.length === 0 && (
                        <Col span={24}>
                            <div style={{ color: textSecondary, fontSize: 13, padding: '12px 0' }}>暂无可用模型</div>
                        </Col>
                    )}
                </Row>
            </div>

            {/* 5. Preference Section */}
            <Row gutter={24} style={{ marginBottom: 24 }}>
                <Col span={12}>
                    <div style={{ ...cardStyle, height: '100%' }}>
                        <Text style={{ color: textSecondary, display: 'block', marginBottom: 20, fontWeight: 500 }}>用户虚拟密钥</Text>
                        <div style={{ marginBottom: 18 }}>
                            <div style={{ color: textSecondary, fontSize: 12, marginBottom: 6 }}>BaseURL</div>
                            <div
                                title={gatewayBaseUrl}
                                style={{
                                    color: textPrimary,
                                    backgroundColor: '#f6f8fa',
                                    border: '1px solid #d0d7de',
                                    borderRadius: 6,
                                    padding: '8px 10px',
                                    fontSize: 13,
                                    fontFamily: 'ui-monospace, SFMono-Regular, SFMono-Regular, Consolas, monospace',
                                    overflow: 'hidden',
                                    textOverflow: 'ellipsis',
                                    whiteSpace: 'nowrap',
                                }}
                            >
                                {gatewayBaseUrl}
                            </div>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 16, alignItems: 'flex-end' }}>
                            <div>
                                <div style={{ color: textSecondary, fontSize: 18, marginBottom: 4 }}>
                                    当前密钥数量&nbsp;&nbsp;
                                    <button
                                        type="button"
                                        onClick={() => onNavigate?.('keys')}
                                        style={{
                                            border: 'none',
                                            background: 'transparent',
                                            padding: 0,
                                            color: '#0969da',
                                            fontSize: 20,
                                            lineHeight: 1.1,
                                            fontWeight: 700,
                                            cursor: 'pointer',
                                        }}
                                    >{virtualKeySummary.total}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </Col>

                {/* Recent Model Preference */}
                <Col span={12}>
                    <div style={{ ...cardStyle, height: MODEL_USAGE_CARD_HEIGHT, display: 'flex', flexDirection: 'column' }}>
                        <Text style={{ color: textSecondary, display: 'block', marginBottom: 20, fontWeight: 500 }}>使用模型分布</Text>
                        <Space
                            direction="vertical"
                            style={{ width: '100%', flex: 1, minHeight: 0, overflowY: 'auto', paddingRight: 4 }}
                            size="large"
                        >
                            {modelUsageCounts.map(model => (
                                <div key={`${model.modelId}-${model.vendorId}`} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                    <Badge color="#0969da" />
                                    <Text
                                        title={model.modelName}
                                        style={{
                                            color: textPrimary,
                                            width: 120,
                                            fontWeight: 500,
                                            overflow: 'hidden',
                                            textOverflow: 'ellipsis',
                                            whiteSpace: 'nowrap',
                                        }}
                                    >
                                        {model.modelName}
                                    </Text>
                                    <div style={{ flexGrow: 1, height: 8, backgroundColor: '#eaeef2', borderRadius: 4 }}>
                                        <div style={{ width: `${(model.usageCount / maxModelUsageCount) * 100}%`, height: '100%', backgroundColor: '#0969da', borderRadius: 4 }} />
                                    </div>
                                    <Text style={{ color: textSecondary, fontWeight: 600 }}>{model.usageCount}</Text>
                                </div>
                            ))}
                            {modelUsageCounts.length === 0 && (
                                <div style={{ color: textSecondary, fontSize: 13, padding: '4px 0' }}>暂无模型调用记录</div>
                            )}
                        </Space>
                    </div>
                </Col>
            </Row>

            {/* 6. Usage Token Logs */}
            <div style={{ ...cardStyle, marginBottom: 24 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, gap: 12, flexWrap: 'wrap' }}>
                    <Text style={{ color: textPrimary, fontWeight: 700 }}>使用Token日志</Text>
                    <DatePicker.RangePicker
                        allowClear={false}
                        defaultValue={[dayjs(DEFAULT_USAGE_LOG_START_DATE), dayjs(DEFAULT_USAGE_LOG_END_DATE)]}
                        format="YYYY-MM-DD"
                        onChange={handleUsageLogDateChange}
                        placeholder={['开始日期', '结束日期']}
                        style={{ width: 260 }}
                    />
                </div>
                <Table<UserUsageLogListItem>
                    columns={usageLogColumns}
                    dataSource={usageLogs}
                    rowKey="usageLogId"
                    loading={usageLogsLoading}
                    size="small"
                    scroll={{ x: 1080 }}
                    pagination={{
                        current: usageLogsPage,
                        pageSize: USAGE_LOG_PAGE_SIZE,
                        total: usageLogsTotal,
                        showSizeChanger: false,
                        showTotal: (total) => `${total} records`,
                        onChange: (page) => setUsageLogsPage(page),
                    }}
                />
            </div>

            {/* 6. Coding Activity Periods (SVG Arc) */}
            <Card bordered={false} bodyStyle={{ ...cardStyle, textAlign: 'center' }} style={{ background: 'transparent' }}>
                <Text style={{ color: textSecondary, display: 'block', textAlign: 'left', marginBottom: 12, fontWeight: 500 }}>Coding Activity Periods <ClockCircleOutlined /></Text>
                <div style={{ position: 'relative', height: 200, display: 'flex', justifyContent: 'center', alignItems: 'flex-end' }}>
                    <svg width="600" height="200" viewBox="0 0 600 200">
                        {/* 弧线轨迹 */}
                        <path
                            d="M 50 180 A 250 150 0 0 1 550 180"
                            fill="none"
                            stroke="#d0d7de"
                            strokeWidth="2"
                            strokeDasharray="5,5"
                        />
                        {/* 刻度点 */}
                        {HOURS.map(h => {
                            const angle = Math.PI + (h / 23) * Math.PI;
                            const rx = 250; const ry = 150;
                            const cx = 300 + rx * Math.cos(angle);
                            const cy = 180 + ry * Math.sin(angle);
                            const isActive = h > 10 && h < 18;
                            return (
                                <g key={h}>
                                    <circle
                                        cx={cx} cy={cy}
                                        r={isActive ? 6 : 4}
                                        fill={isActive ? '#0969da' : '#ffffff'}
                                        stroke={isActive ? '#0969da' : '#d0d7de'}
                                        strokeWidth="2"
                                        opacity={isActive ? 1 : 0.8}
                                    />
                                    {h % 6 === 0 && (
                                        <text x={cx} y={cy + 20} fill="#656d76" fontSize="12" textAnchor="middle" fontWeight="500">
                                            {h.toString().padStart(2, '0')}:00
                                        </text>
                                    )}
                                </g>
                            );
                        })}
                    </svg>
                </div>
            </Card>

            <div style={{ textAlign: 'left', marginTop: 16, color: '#656d76', fontSize: 12 }}>
                The page updates daily at 00:00 (UTC+8). Last updated: 23:59:59.
            </div>
        </div>
    );
};

function buildMockRecentMonthHourlyHeatmap(): UserUsageHourlyHeatmapResult {
    const today = new Date();
    const startDate = new Date(today);
    startDate.setDate(today.getDate() - 29);

    const days = Array.from({ length: 30 }, (_, dayIndex) => {
        const currentDate = new Date(startDate);
        currentDate.setDate(startDate.getDate() + dayIndex);
        const date = formatDate(currentDate);
        const hours = HOURS.map(hour => ({
            hour,
            requestCount: getMockHourlyRequestCount(dayIndex, hour, currentDate),
        }));
        return {
            date,
            totalCount: hours.reduce((sum, item) => sum + item.requestCount, 0),
            hours,
        };
    });
    const totalCount = days.reduce((sum, day) => sum + day.totalCount, 0);
    const maxHourlyCount = days.reduce(
        (max, day) => Math.max(max, ...day.hours.map(hour => hour.requestCount)),
        0
    );

    return {
        startDate: formatDate(startDate),
        endDate: formatDate(today),
        totalCount,
        maxHourlyCount,
        days,
    };
}

function getMockHourlyRequestCount(dayIndex: number, hour: number, date: Date): number {
    const weekday = date.getDay();
    const weekendFactor = weekday === 0 || weekday === 6 ? 0.45 : 1;
    const morningPeak = hour >= 9 && hour <= 11 ? 5 : 0;
    const afternoonPeak = hour >= 14 && hour <= 18 ? 7 : 0;
    const eveningWork = hour >= 20 && hour <= 22 ? 2 : 0;
    const base = hour >= 8 && hour <= 23 ? 1 : 0;
    const rhythm = (dayIndex * 3 + hour * 2) % 5;
    const requestCount = Math.round((base + morningPeak + afternoonPeak + eveningWork + rhythm) * weekendFactor);
    return Math.max(0, requestCount);
}

function getHourlyHeatmapColor(value: number, maxValue: number) {
    if (value <= 0 || maxValue <= 0) return '#ebedf0';
    const ratio = value / maxValue;
    if (ratio < 0.25) return '#dbeafe';
    if (ratio < 0.5) return '#93c5fd';
    if (ratio < 0.75) return '#3b82f6';
    return '#1d4ed8';
}

function formatHeatmapDateLabel(date: string): string {
    const [, month, day] = date.split('-');
    return `${month}/${day}`;
}

function formatHourLabel(hour: number): string {
    return `${hour.toString().padStart(2, '0')}:00`;
}

function formatHourRangeLabel(startHour: number): string {
    const endHour = startHour + HOURS_PER_HEATMAP_ROW - 1;
    return `${startHour.toString().padStart(2, '0')}-${endHour.toString().padStart(2, '0')}`;
}

function formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = `${date.getMonth() + 1}`.padStart(2, '0');
    const day = `${date.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
}

function offsetDate(date: Date, offsetDays: number): Date {
    const nextDate = new Date(date);
    nextDate.setDate(date.getDate() + offsetDays);
    return nextDate;
}

function calculateUseDays(useTime: string | null): number {
    if (!useTime) return 0;
    const datePart = useTime.split(/[ T]/)[0];
    const [year, month, day] = datePart.split('-').map(value => Number(value));
    if (!year || !month || !day) return 0;

    const startDate = new Date(year, month - 1, day);
    const today = new Date();
    const todayDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const diffDays = Math.floor((todayDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000));
    return Math.max(0, diffDays);
}
