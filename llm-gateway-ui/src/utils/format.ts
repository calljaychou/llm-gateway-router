export function formatAmount(value?: number | string | null, fallback = '0.00'): string {
    if (value === null || value === undefined || value === '') return fallback;

    const amount = Number(value);
    if (!Number.isFinite(amount)) return fallback;

    return amount.toLocaleString('zh-CN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    });
}
