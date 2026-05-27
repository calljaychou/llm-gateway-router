import axios from 'axios';

// 创建 Axios 统一网关实例
const api = axios.create({
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 1. 请求拦截器：自动注入 JWT 认证 Token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('llm_gateway_token');
        if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// 2. 响应拦截器：集中处理鉴权失败，实现未登录状态拦截
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // 当后端抛出 401 未认证或 Token 过期，以及 403 无权限状态码时
        if (error.response && (error.response.status === 401 || error.response.status === 403)) {
            localStorage.removeItem('llm_gateway_token');
            localStorage.removeItem('llm_gateway_username');

            // 触发全局自定义事件，通知最顶层的 App 组件切换回登录视图
            window.dispatchEvent(new Event('llm-gateway-unauthorized'));
        }
        return Promise.reject(error);
    }
);

// 通用后端响应体包装模型
export interface ApiResult<T> {
    code: number;
    data: T;
    message: string;
    success: boolean;
    timestamp: number;
}

// 通用分页数据包装模型
export interface PageResult<T> {
    list: T[];
    pageNum: number;
    pageSize: number;
    total: number;
}

/** ==================== 0. 认证相关类型与接口 ==================== */
export interface LoginRequest {
    username: string; // 用户名或邮箱
    password: string; // 密码
}

export interface LoginResult {
    email?: string;
    expiresInSeconds: number;
    passwordChanged: boolean;
    roles: string[];
    token: string;
    tokenType: string; // 例如: "Bearer"
    userId: number;
    username: string;
}

export const authApi = {
    // 登录并获取 JWT Token
    login: async (params: LoginRequest) => {
        const res = await api.post<ApiResult<LoginResult>>('/api/auth/login', params);
        return res.data;
    },
};

/** ==================== 1. 用户端-虚拟密钥管理类型与接口 ==================== */
export interface CreateVirtualKeyParams {
    name: string;
}

export interface CreateVirtualKeyResult {
    apiKey: string;
    keyId: number;
    keyPrefix: string;
    name: string;
}

export interface VirtualKeyListItem {
    expiresTime?: string;
    keyId: number;
    keyPrefix: string;
    name: string;
    status: number;
}

export interface VirtualKeyListResult {
    keys: VirtualKeyListItem[];
}

export interface RevokeVirtualKeyResult {
    keyId: number;
    revoked: boolean;
}

export const virtualKeyApi = {
    listKeys: async () => {
        const res = await api.get<ApiResult<VirtualKeyListResult>>('/admin/user/keys');
        return res.data;
    },
    createKey: async (params: CreateVirtualKeyParams) => {
        const res = await api.post<ApiResult<CreateVirtualKeyResult>>('/admin/user/keys', params);
        return res.data;
    },
    revokeKey: async (id: number) => {
        const res = await api.delete<ApiResult<RevokeVirtualKeyResult>>(`/admin/user/keys/${id}`);
        return res.data;
    }
};

/** ==================== 2. 用户端-配额仪表盘与流水类型 ==================== */
export interface UserQuotaBatch {
    consumedTokens: number;
    createdAt?: string;
    expiredTokens: number;
    expiresAt: string;
    grantId: number;
    grantedBy?: number;
    grantedTokens: number;
    remainingTokens: number;
    remark?: string;
    sourceGrantId?: number;
    sourceType: string;
    sourceUserId?: number;
    status: string;
    updatedAt?: string;
    userId: number;
}

export interface UserQuotaAccountSnapshot {
    activeGrants: UserQuotaBatch[];
    allowTransferOut: boolean;
    availableTokens: number;
    currentQuotaTokens: number;
    earliestExpireAt?: string;
    expiredTokens: number;
    transferredInTokens: number;
    transferredOutTokens: number;
    updatedAt?: string;
    usedTokens: number;
    userId: number;
}

export interface QuotaTransactionItem {
    availableAfter: number;
    availableBefore: number;
    bizNo: string;
    changeType: string;
    counterpartyUserId?: number;
    createdAt: string;
    deltaTokens: number;
    grantId?: number;
    operatorUserId?: number;
    quotaAfter: number;
    quotaBefore: number;
    remark?: string;
    requestId?: string;
    transactionId: number;
    userId: number;
}

export interface QuotaTransferParams {
    targetUserId: number;
    transferTokens: number;
    remark?: string;
}

export interface QuotaTransferResult {
    fromAccount: UserQuotaAccountSnapshot;
    fromUserId: number;
    targetAccount: UserQuotaAccountSnapshot;
    targetUserId: number;
    transferTokens: number;
}

export const quotaApi = {
    getCurrentSnapshot: async () => {
        const res = await api.get<ApiResult<UserQuotaAccountSnapshot>>('/admin/user/quotas/current');
        return res.data;
    },
    listTransactions: async (pageNum: number, pageSize: number, changeType?: string) => {
        const res = await api.get<ApiResult<PageResult<QuotaTransactionItem>>>('/admin/user/quotas/transactions', {
            params: { pageNum, pageSize, changeType }
        });
        return res.data;
    },
    transferQuota: async (params: QuotaTransferParams) => {
        const res = await api.post<ApiResult<QuotaTransferResult>>('/admin/user/quotas/transfer', params);
        return res.data;
    }
};

/** ==================== 3. 管理端-组织架构与用户管理 ==================== */
export interface CreateDepartmentParams {
    deptName: string;
    leaderUserId?: number;
    orderNum?: number;
    tel?: string;
}

export interface DepartmentTreeItem {
    id: number;
    name: string;
    orderNum: number;
    parentId: number;
    status: number;
    children?: DepartmentTreeItem[];
    leaderUserId?: number;
    tel?: string;
}

export interface DepartmentPermissionItem {
    modelAlias: string;
    scope: 'SELF' | 'SUBTREE';
}

export interface ReplaceDepartmentPermissionsParams {
    items: DepartmentPermissionItem[];
}

export const adminDeptApi = {
    // 获取整个组织的部门层级树
    getDeptTree: async () => {
        const res = await api.get<ApiResult<DepartmentTreeItem[]>>('/admin/departments/tree');
        return res.data;
    },
    // 创建根部门（一级部门）
    createRootDept: async (params: CreateDepartmentParams) => {
        const res = await api.post<ApiResult<any>>('/admin/departments', params);
        return res.data;
    },
    // 在指定父级下创建子部门
    createChildDept: async (parentId: number, params: CreateDepartmentParams) => {
        const res = await api.post<ApiResult<any>>(`/admin/departments/${parentId}/children`, params);
        return res.data;
    },
    // 获取部门的直接/有效模型使用权限
    getDeptPermissions: async (deptId: number, view: 'direct' | 'effective' = 'direct') => {
        const res = await api.get<ApiResult<any>>(`/admin/departments/${deptId}/permissions`, { params: { view } });
        return res.data;
    },
    // 覆盖更新该部门的模型权限策略
    replaceDeptPermissions: async (deptId: number, params: ReplaceDepartmentPermissionsParams) => {
        const res = await api.put<ApiResult<any>>(`/admin/departments/${deptId}/permissions`, params);
        return res.data;
    },
    // 删除部门
    deleteDept: async (id: number) => {
        const res = await api.delete<ApiResult<any>>(`/admin/departments/${id}`);
        return res.data;
    }
};

/** ==================== 4. 管理端-大模型与主密钥管理 ==================== */
export interface CreateVendorParams {
    baseUrl: string;
    name: string;
    status: number;
}

export interface VendorListItem {
    id: number;
    name: string;
    baseUrl: string;
    status: number;
    createdTime: string; // 假设后端返回创建时间
}

export interface CreateModelParams {
    active: boolean;
    billingType: 'FREE' | 'PAID';
    modelAlias: string;
    realModelName: string;
    vendorId: number;
}

export interface ModelVendorListItem {
    id: number;
    modelAlias: string;
    realModelName: string;
    billingType: string;
    active: boolean;
    vendorId: number;
    vendorName: string; // 关联的供应商名称
}

export interface CreateMasterKeyParams {
    apiKey: string;
    status: number;
    vendorId: number;
    weight: number;
}

export interface MasterKeyPageParams {
    vendorId?: number;
    status?: number;
    pageNum: number;
    pageSize: number;
}

export interface MasterKeyListItem {
    vendorId: number;
    vendorName: string;
    keyInfo: string;
    weight: number;
    status: number;
    lastCheckedAt?: string;
    createdTime?: string;
}

export interface UpdateModelParams {
    active: boolean;
    billingType: 'FREE' | 'PAID';
    modelAlias: string;
    realModelName: string;
    vendorId: number;
}

export const adminGatewayApi = {
    // --- 供应商 (Vendor) 相关 ---
    listVendors: async () => {
        // 对应新补充的查询供应商列表 API
        const res = await api.get<ApiResult<VendorListItem[]>>('/admin/vendors');
        return res.data;
    },
    createVendor: async (params: CreateVendorParams) => {
        const res = await api.post<ApiResult<any>>('/admin/vendors', params);
        return res.data;
    },

    // --- 模型路由映射 (Model) 相关 ---
    listModelVendors: async () => {
        // 对应新补充的查询模型供应商列表 API
        const res = await api.get<ApiResult<ModelVendorListItem[]>>('/admin/models');
        return res.data;
    },
    createModel: async (params: CreateModelParams) => {
        const res = await api.post<ApiResult<any>>('/admin/models', params);
        return res.data;
    },

    // --- 主密钥 (Master Key) 相关 ---
    createMasterKey: async (params: CreateMasterKeyParams) => {
        const res = await api.post<ApiResult<any>>('/admin/master-keys', params);
        return res.data;
    },
    // 主密钥列表
    listMasterKeys: async (params: MasterKeyPageParams) => {
        const res = await api.get<ApiResult<PageResult<MasterKeyListItem>>>('/admin/master-keys', { params });
        return res.data;
    },
    // 编辑已有模型映射
    updateModel: async (modelId: number, params: UpdateModelParams) => {
        const res = await api.put<ApiResult<any>>(`/admin/models/${modelId}`, params);
        return res.data;
    },

    // 删除模型映射
    deleteModel: async (modelId: number) => {
        const res = await api.delete<ApiResult<any>>(`/admin/models/${modelId}`);
        return res.data;
    },
};
