import React from 'react';
import { Row, Col, Typography, Space, Tag, Card, Badge } from 'antd';
import {
    UserOutlined,
    MessageOutlined,
    ClockCircleOutlined,
    GlobalOutlined
} from '@ant-design/icons';

const { Title, Text } = Typography;

// --- 模拟数据 ---
const ACTIVITY_DATA = Array.from({ length: 52 * 7 }, () => Math.floor(Math.random() * 5));
const MODEL_PREFERENCE = [
    { name: 'gpt-4-o', count: 15 },
    { name: 'auto', count: 11 },
];
const HOURS = Array.from({ length: 24 }, (_, i) => i);

export const QuotaDashboard: React.FC = () => {
    // 定义通用样式变量以保证统一
    const cardStyle = { backgroundColor: '#ffffff', padding: 20, borderRadius: 12, border: '1px solid #d0d7de' };
    const textPrimary = '#1f2328';
    const textSecondary = '#656d76';
    const loginUserName = localStorage.getItem('llm_gateway_name') || localStorage.getItem('llm_gateway_username') || '系统用户';

    return (
        <div style={{ backgroundColor: '#f6f8fa', minHeight: '100%', padding: '24px', color: textPrimary }}>
            {/* 1. Header Section */}
            <div style={{ marginBottom: 32 }}>
                <Title level={2} style={{ color: textPrimary, margin: 0 }}>Hello! {loginUserName}</Title>
                <Text style={{ color: textSecondary, fontSize: 16 }}>
                    This is your day <span style={{ color: '#0969da', fontWeight: 'bold' }}>182</span> of using TRAK.
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

            {/* 2. Active Days (GitHub Light Style Heatmap) */}
            <Card bordered={false} bodyStyle={{ ...cardStyle, padding: 24 }} style={{ marginBottom: 24, background: 'transparent' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
                    <Text style={{ color: textPrimary, fontWeight: 'bold' }}>Active Days</Text>
                    <Space size="small">
                        <span style={{ fontSize: 12, color: textSecondary }}>Less</span>
                        {[0, 1, 2, 3, 4].map(v => (
                            <div key={v} style={{ width: 10, height: 10, backgroundColor: getHeatmapColor(v), borderRadius: 2 }} />
                        ))}
                        <span style={{ fontSize: 12, color: textSecondary }}>More</span>
                    </Space>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(52, 1fr)', gap: 3 }}>
                    {ACTIVITY_DATA.map((val, i) => (
                        <div
                            key={i}
                            style={{
                                aspectRatio: '1/1',
                                backgroundColor: getHeatmapColor(val),
                                borderRadius: 2
                            }}
                        />
                    ))}
                </div>
            </Card>

            {/* 3. Small Stats Counters */}
            <Row gutter={24} style={{ marginBottom: 24 }}>
                <Col span={12}>
                    <div style={{ ...cardStyle, height: '100%' }}>
                        <div style={{ color: textSecondary, marginBottom: 8, fontWeight: 500 }}>AI Code Accepted <GlobalOutlined /></div>
                        <div style={{ fontSize: 32, color: textPrimary, marginBottom: 16, fontWeight: 600 }}>41</div>
                        <div style={{ display: 'flex', gap: 4, height: 24 }}>
                            <div style={{ flex: 8, backgroundColor: '#0969da', borderRadius: '4px 0 0 4px', display: 'flex', alignItems: 'center', paddingLeft: 8, fontSize: 12, color: '#fff' }}>markdown</div>
                            <div style={{ flex: 3, backgroundColor: '#54aeff', display: 'flex', alignItems: 'center', paddingLeft: 8, fontSize: 12, color: '#fff' }}>js</div>
                            <div style={{ flex: 29, backgroundColor: '#eaeef2', borderRadius: '0 4px 4px 0', display: 'flex', alignItems: 'center', paddingLeft: 8, fontSize: 12, color: textSecondary }}>others</div>
                        </div>
                    </div>
                </Col>
                <Col span={12}>
                    <Row gutter={16} style={{ height: '100%' }}>
                        <Col span={12}>
                            <div style={cardStyle}>
                                <div style={{ color: textSecondary, fontWeight: 500 }}>Chat Count <MessageOutlined /></div>
                                <div style={{ fontSize: 32, color: textPrimary, fontWeight: 600 }}>26</div>
                            </div>
                        </Col>
                        <Col span={12}>
                            <div style={cardStyle}>
                                <div style={{ color: textSecondary, fontWeight: 500 }}>Agent <UserOutlined /></div>
                                <div style={{ fontSize: 32, color: textPrimary, fontWeight: 600 }}>36</div>
                            </div>
                        </Col>
                    </Row>
                </Col>
            </Row>

            {/* 4. Preference Section */}
            <Row gutter={24} style={{ marginBottom: 24 }}>
                {/* Most Frequent AI Partner */}
                <Col span={12}>
                    <div style={{ ...cardStyle, height: '100%' }}>
                        <Text style={{ color: textSecondary, display: 'block', marginBottom: 20, fontWeight: 500 }}>Most Frequent AI Partner</Text>
                        <div style={{ display: 'flex', alignItems: 'flex-end', gap: 24 }}>
                            <div style={{ textAlign: 'center' }}>
                                <div style={{ width: 80, height: 80, backgroundColor: '#dafbe1', border: '1px solid #4ac26b', borderRadius: 12, display: 'flex', justifyContent: 'center', alignItems: 'center', marginBottom: 12 }}>
                                    <UserOutlined style={{ fontSize: 40, color: '#1a7f37' }} />
                                </div>
                                <div style={{ color: '#1a7f37', fontWeight: 'bold' }}>@SOLO Agent</div>
                            </div>
                            <div style={{ flexGrow: 1 }}>
                                <div style={{ color: textSecondary, fontSize: 12 }}>Number of conversations</div>
                                <div style={{ fontSize: 32, color: textPrimary, marginBottom: 8, fontWeight: 600 }}>15</div>
                                <div style={{ display: 'flex', alignItems: 'flex-end', gap: 4, height: 40 }}>
                                    {[2, 4, 3, 6, 8, 4, 2].map((h, i) => (
                                        <div key={i} style={{ flex: 1, backgroundColor: i === 4 ? '#1a7f37' : '#eaeef2', height: `${h * 10}%`, borderRadius: 2 }} />
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </Col>

                {/* Recent Model Preference */}
                <Col span={12}>
                    <div style={{ ...cardStyle, height: '100%' }}>
                        <Text style={{ color: textSecondary, display: 'block', marginBottom: 20, fontWeight: 500 }}>Recent Model Invocation Preference</Text>
                        <Space direction="vertical" style={{ width: '100%' }} size="large">
                            {MODEL_PREFERENCE.map(model => (
                                <div key={model.name} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                    <Badge color={model.name === 'auto' ? '#8c959f' : '#0969da'} />
                                    <Text style={{ color: textPrimary, width: 100, fontWeight: 500 }}>{model.name}</Text>
                                    <div style={{ flexGrow: 1, height: 8, backgroundColor: '#eaeef2', borderRadius: 4 }}>
                                        <div style={{ width: `${(model.count / 26) * 100}%`, height: '100%', backgroundColor: model.name === 'auto' ? '#8c959f' : '#0969da', borderRadius: 4 }} />
                                    </div>
                                    <Text style={{ color: textSecondary, fontWeight: 600 }}>{model.count}</Text>
                                </div>
                            ))}
                        </Space>
                    </div>
                </Col>
            </Row>

            {/* 5. Coding Activity Periods (SVG Arc) */}
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

// 辅助函数：根据数值获取 GitHub 风格的明亮主题绿色渐变
function getHeatmapColor(value: number) {
    switch (value) {
        case 0: return '#ebedf0'; // 空白浅灰
        case 1: return '#9be9a8'; // 浅绿
        case 2: return '#40c463'; // 中绿
        case 3: return '#30a14e'; // 深绿
        case 4: return '#216e39'; // 极深绿
        default: return '#ebedf0';
    }
}
